package com.github.novicezk.midjourney.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.novicezk.midjourney.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountDTO {
    private Mj mj;

    @Data
    public static class Mj {
        private List<Account> accounts;
    }

    @Data
    public static class Account {
        private String guildId;
        private String channelId;
        private String userToken;
        private String userAgent;
        private int coreSize;
        private int queueSize;
    }
}
