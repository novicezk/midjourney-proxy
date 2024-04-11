package com.github.novicezk.midjourney.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.novicezk.midjourney.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("Discord account")
public class DiscordAccount extends DomainObject {

	@ApiModelProperty("Server_ID")
	private String guildId;
	@ApiModelProperty("Channel_ID")
	private String channelId;
	@ApiModelProperty("UserToken")
	private String userToken;
	@ApiModelProperty("UserAgent")
	private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;

	@ApiModelProperty("its_usable_or_not")
	private boolean enable = true;

	@ApiModelProperty("Number_of_concurrencies")
	private int coreSize = 1;
	@ApiModelProperty("Waiting_queue_length")
	private int queueSize = 10;
	@ApiModelProperty("Task_timeout_(minutes)")
	private int timeoutMinutes = 5;

	@JsonIgnore
	public String getDisplay() {
		return this.channelId;
	}
}
