package com.github.novicezk.midjourney.wss.user;

import cn.hutool.core.thread.ThreadUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.exception.ConnectionManuallyClosedException;
import com.github.novicezk.midjourney.exception.ConnectionResumableException;
import com.github.novicezk.midjourney.exception.InvalidSessionException;
import com.github.novicezk.midjourney.exception.NeedToReconnectException;
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
import java.util.Set;

@Slf4j
public class UserWebSocketStarter extends WebSocketAdapter implements WebSocketStarter {
	private static final String GATEWAY_URL = "wss://gateway-us-east1-d.discord.gg/?encoding=json&v=9&compress=zlib-stream";
	private final Decompressor decompressor = new ZlibDecompressor(2048);

	private final DataObject auth;
	private boolean connected = false;
	private boolean resumable = false;
	private int sequence = 0;
	private long interval = 0L;
	private long lastAck = 0L;
	private long latency = 0L;
	private Throwable lastException = null;

	private String sessionId;
	private final String userToken;
	private final String userAgent;
	private WebSocket socket = null;

	@Resource
	private UserMessageListener userMessageListener;

	public UserWebSocketStarter(ProxyProperties properties) {
		this.userToken = properties.getDiscord().getUserToken();
		this.userAgent = properties.getDiscord().getUserAgent();
		UserAgent agent = UserAgent.parseUserAgentString(userAgent);
		DataObject connectionProperties = DataObject.empty()
				.put("os", agent.getOperatingSystem().getName())
				.put("browser", agent.getBrowser().getGroup().getName())
				.put("device", "")
				.put("system_locale", "zh-CN")
				.put("browser_version", agent.getBrowserVersion().toString())
				.put("browser_user_agent", userAgent)
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
	public void start() throws Exception {
		WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
		this.socket = factory.createSocket(GATEWAY_URL);
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
		if (!this.resumable) {
			DataObject data = DataObject.empty()
					.put("op", WebSocketCode.IDENTIFY)
					.put("d", this.auth);
			send(data);
		} else {
			this.resumable = false;
			DataObject data = DataObject.empty()
					.put("op", WebSocketCode.RESUME)
					.put("d", DataObject.empty()
							.put("token", this.userToken)
							.put("session_id", this.sessionId)
							.put("seq", Math.max(this.sequence - 1, 0))
					);
			send(data);
		}
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
			this.sequence += 1;
		}
		if (opCode == WebSocketCode.HELLO) {
			this.interval = data.getObject("d").getLong("heartbeat_interval");
			ThreadUtil.execute(this::heartbeat);
		} else if (opCode == WebSocketCode.HEARTBEAT_ACK) {
			if (this.lastAck != 0) {
				this.latency = System.currentTimeMillis() - this.lastAck;
			}
		} else if (opCode == WebSocketCode.HEARTBEAT) {
			send(DataObject.empty().put("op", WebSocketCode.HEARTBEAT).put("d", this.sequence));
		} else if (opCode == WebSocketCode.INVALIDATE_SESSION) {
			log.debug("[gateway] Invalid session.");
			this.lastException = new InvalidSessionException("Invalid Session Error.");
			if (this.resumable) {
				this.resumable = false;
			}
			this.sequence = 0;
			close();
		} else if (opCode == WebSocketCode.RECONNECT) {
			log.debug("[gateway] Received opcode 7 (reconnect).");
			this.lastException = new NeedToReconnectException("Discord sent an opcode 7 (reconnect).");
			close();
		} else if (opCode == WebSocketCode.DISPATCH) {
			onDispatch(data);
		}
	}

	@Override
	public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
		log.error("[gateway] There was some websocket error", cause);
		this.lastException = cause;
	}

	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
		this.connected = false;
		if (clientCloseFrame != null) {
			int code = clientCloseFrame.getCloseCode();
			log.debug("[gateway] websocket client closed. status code: {}, reason: {}", code, clientCloseFrame.getCloseReason());
			if (code <= 4000 || code > 4010) {
				this.resumable = true;
				this.lastException = new ConnectionResumableException("Connection is resumable.");
			} else if (Set.of(1000, 1001, 1006).contains(code)) {
				this.lastException = new ConnectionManuallyClosedException("Disconnection initiated by client using close function.");
			}
		} else if (serverCloseFrame != null) {
			log.debug("[gateway] websocket server closed. status code: {}, reason: {}", serverCloseFrame.getCloseCode(), serverCloseFrame.getCloseReason());
		}
	}

	private void close() {
		this.connected = false;
		if (!(this.lastException instanceof InvalidSessionException || this.lastException instanceof NeedToReconnectException)) {
			this.lastException = new ConnectionManuallyClosedException("Disconnection initiated by client using close function.");
		}
		log.debug("[gateway] websocket closed");
		this.socket.disconnect();
	}

	private void heartbeat() {
		while (this.connected) {
			if (this.interval == 0) {
				this.interval = 41250L;
			}
			try {
				Thread.sleep(this.interval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (!this.connected) {
				break;
			}
			send(DataObject.empty().put("op", WebSocketCode.HEARTBEAT).put("d", this.sequence));
			this.lastAck = System.currentTimeMillis();
		}
	}

	private void onDispatch(DataObject raw) {
		if (!raw.isType("d", DataType.OBJECT)) {
			return;
		}
		DataObject content = raw.getObject("d");
		String t = raw.getString("t", null);
		if ("READY".equals(t)) {
			this.lastException = null;
			this.sessionId = content.getString("session_id");
			return;
		}
		try {
			this.userMessageListener.handle(raw);
		} catch (Exception e) {
			log.warn("handle message error: {}", e.getMessage());
		}
	}

	protected void send(DataObject message) {
		log.trace("[gateway] > {}", message);
		this.socket.sendText(message.toString());
	}

}
