package com.github.novicezk.midjourney.wss.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.ConcurrentSessionController;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;

public class CustomSessionController extends ConcurrentSessionController {
	private final String gateway;

	public CustomSessionController(String gateway) {
		this.gateway = gateway;
	}

	@Override
	public String getGateway() {
		return this.gateway;
	}

	@Override
	public ShardedGateway getShardedGateway(JDA api) {
		return new RestActionImpl<ShardedGateway>(api, Route.Misc.GATEWAY_BOT.compile()) {
			@Override
			public void handleResponse(Response response, Request<ShardedGateway> request) {
				if (response.isOk()) {
					DataObject object = response.getObject();
					String url = getGateway();
					int shards = object.getInt("shards");
					int concurrency = object.getObject("session_start_limit").getInt("max_concurrency", 1);

					request.onSuccess(new ShardedGateway(url, shards, concurrency));
				} else if (response.code == 401) {
					this.api.shutdownNow();
					request.onFailure(new InvalidTokenException("The provided token is invalid!"));
				} else {
					request.onFailure(response);
				}
			}
		}.priority().complete();
	}
}
