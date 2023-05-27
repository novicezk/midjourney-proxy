package com.github.novicezk.midjourney.wss.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.requests.WebSocketCode;
import net.dv8tion.jda.internal.utils.compress.Decompressor;
import net.dv8tion.jda.internal.utils.compress.ZlibDecompressor;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class UserWebSocketStarter extends WebSocketAdapter implements WebSocketStarter {
	private static final String GATEWAY_URL = "wss://gateway.discord.gg/?encoding=json&v=9&compress=zlib-stream";
	private final ScheduledExecutorService heartExecutor = Executors.newSingleThreadScheduledExecutor();

	private final String userToken;
	private final String userAgent;
	private final DataObject auth;

	private WebSocket socket = null;
	private String sessionId;
	private Future<?> heartbeatTask;
	private Decompressor decompressor;

	private boolean connected = false;
	private final AtomicInteger sequence = new AtomicInteger(0);

	@Resource
	private UserMessageListener userMessageListener;

	public UserWebSocketStarter(ProxyProperties properties) {
		this.userToken = properties.getDiscord().getUserToken();
		this.userAgent = properties.getDiscord().getUserAgent();
		UserAgent agent = UserAgent.parseUserAgentString(this.userAgent);
		DataObject connectionProperties = DataObject.empty()
				.put("os", agent.getOperatingSystem().getName())
				.put("browser", agent.getBrowser().getGroup().getName())
				.put("device", "")
				.put("system_locale", "zh-CN")
				.put("browser_version", agent.getBrowserVersion().toString())
				.put("browser_user_agent", this.userAgent)
				.put("referer", "")
				.put("referring_domain", "")
				.put("referrer_current", "")
				.put("referring_domain_current", "")
				.put("release_channel", "stable")
				.put("client_build_number", 117300)
				.put("client_event_source", null);
		DataObject presence = DataObject.empty()
				.put("status", "online")
				.put("since", 0)
				.put("activities", DataArray.empty())
				.put("afk", false);
		DataObject clientState = DataObject.empty()
				.put("guild_hashes", DataArray.empty())
				.put("highest_last_message_id", "0")
				.put("read_state_version", 0)
				.put("user_guild_settings_version", -1)
				.put("user_settings_version", -1);
		this.auth = DataObject.empty()
				.put("token", this.userToken)
				.put("capabilities", 4093)
				.put("properties", connectionProperties)
				.put("presence", presence)
				.put("compress", false)
				.put("client_state", clientState);
	}

	@Override
	public synchronized void start() throws Exception {
		if (this.socket != null) {
			throw new IllegalStateException("Websocket already started");
		}
		this.decompressor = new ZlibDecompressor(2048);
		this.socket = new WebSocketFactory().setConnectionTimeout(5000).createSocket(GATEWAY_URL);
		this.socket.addListener(this);
		this.socket.addHeader("Accept-Encoding", "gzip, deflate, br")
				.addHeader("Accept-Language", "en-US,en;q=0.9")
				.addHeader("Cache-Control", "no-cache")
				.addHeader("Pragma", "no-cache")
				.addHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits")
				.addHeader("User-Agent", this.userAgent);
		this.socket.connect();
	}

	@Override
	public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
		log.debug("[gateway] Connected to websocket.");
		this.connected = true;
	}

	private void sayHello() {
		DataObject data;
		if (CharSequenceUtil.isBlank(this.sessionId)) {
			data = DataObject.empty()
					.put("op", WebSocketCode.IDENTIFY)
					.put("d", this.auth);
		} else {
			data = DataObject.empty()
					.put("op", WebSocketCode.RESUME)
					.put("d", DataObject.empty()
							.put("token", this.userToken)
							.put("session_id", this.sessionId)
							.put("seq", Math.max(this.sequence.get() - 1, 0))
					);
		}
		send(data);
	}

	@Override
	public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
		byte[] decompressBinary = this.decompressor.decompress(binary);
		if (decompressBinary == null) {
			return;
		}
		String json = new String(decompressBinary, StandardCharsets.UTF_8);
		DataObject data = DataObject.fromJson(json);
		int opCode = data.getInt("op");
		if (opCode != WebSocketCode.HEARTBEAT_ACK) {
			this.sequence.incrementAndGet();
		}
		if (opCode == WebSocketCode.HELLO) {
			long interval = data.getObject("d").getLong("heartbeat_interval");
			this.heartbeatTask = this.heartExecutor.scheduleAtFixedRate(this::heartbeat, interval, interval, TimeUnit.MILLISECONDS);
			sayHello();
		} else if (opCode == WebSocketCode.HEARTBEAT_ACK) {
			log.trace("[gateway] Heartbeat ack.");
		} else if (opCode == WebSocketCode.HEARTBEAT) {
			send(DataObject.empty().put("op", WebSocketCode.HEARTBEAT).put("d", this.sequence));
		} else if (opCode == WebSocketCode.INVALIDATE_SESSION) {
			log.debug("[gateway] Invalid session.");
			close("session invalid");
		} else if (opCode == WebSocketCode.RECONNECT) {
			log.debug("[gateway] Received opcode 7 (reconnect).");
			close("reconnect");
		} else if (opCode == WebSocketCode.DISPATCH) {
			onDispatch(data);
		}
	}

	@Override
	public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
		log.error("[gateway] There was some websocket error", cause);
	}

	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
		clearHeartbeat();
		this.connected = false;
		this.sequence.set(0);
		this.socket = null;
		this.decompressor = null;
		if (clientCloseFrame != null) {
			int code = clientCloseFrame.getCloseCode();
			log.debug("[gateway] Websocket client closed. status code: {}, reason: {}", code, clientCloseFrame.getCloseReason());
		} else if (serverCloseFrame != null) {
			log.debug("[gateway] Websocket server closed. status code: {}, reason: {}", serverCloseFrame.getCloseCode(), serverCloseFrame.getCloseReason());
		}
		try {
			log.debug("[gateway] Websocket reconnect...");
			start();
		} catch (Exception e) {
			log.error("[gateway] Websocket reconnect error", e);
			Thread.currentThread().interrupt();
		}
	}

	private void clearHeartbeat() {
		if (this.heartbeatTask != null) {
			this.heartbeatTask.cancel(true);
			this.heartbeatTask = null;
		}
	}

	private void close(String reason) {
		this.connected = false;
		clearHeartbeat();
		this.socket.disconnect(1000, reason);
	}

	private void heartbeat() {
		if (!this.connected) {
			return;
		}
		send(DataObject.empty().put("op", WebSocketCode.HEARTBEAT).put("d", this.sequence));
	}

	private void onDispatch(DataObject raw) {
		if (!raw.isType("d", DataType.OBJECT)) {
			return;
		}
		DataObject content = raw.getObject("d");
		String t = raw.getString("t", null);
		if ("READY".equals(t)) {
			this.sessionId = content.getString("session_id");
			return;
		}
		try {
			this.userMessageListener.onMessage(raw);
		} catch (Exception e) {
			log.error("user-wss handle message error", e);
		}
	}

	protected void send(DataObject message) {
		log.trace("[gateway] > {}", message);
		this.socket.sendText(message.toString());
	}

}
