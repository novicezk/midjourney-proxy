## Configuration items

| Name                             | Required | Description                                                                                               |
|:------------------------------|:--------:|:----------------------------------------------------------------------------------------------------------|
| mj.accounts                   |    Yes     | [Account pool configuration](./config.md#%E8%B4%A6%E5%8F%B7%E6%B1%A0%E9%85%8D%E7%BD%AE%E5%8F%82%E8%80%83)，no additional settings are required after configuration mj.discord |
| mj.discord.guild-id           |    Yes     | discord server ID                                                                                              |
| mj.discord.channel-id         |    Yes     | discord channel ID                                                                                               |
| mj.discord.user-token         |    Yes     | discord userToken                                                                                            |
| mj.discord.user-agent         |    No     | User-agent when calling the discord interface and connecting to wss. It is recommended to copy it from the browser network                                                             |
| mj.discord.core-size          |    No     | Concurrency number, default is 3                                                                                                  |
| mj.discord.queue-size         |    No     | Waiting queue, default length 10                                                                                               |
| mj.discord.timeout-minutes    |    No     | Task timeout, default is 5 minutes                                                                                             |
| mj.api-secret                 |    No     | Interface secret, if empty, authentication will not be enabled; request headers need to be added when calling the interface mj-api-secret                                                                    |
| mj.notify-hook                |    No     | Global task status change callback address                                                                                             |
| mj.notify-notify-pool-size    |    No     | Notification callback thread pool size, default 10                                                                                            |
| mj.task-store.type            |    No     | Task storage method, default in_memory (memory\lost after restart), optional redis                                                                      |
| mj.task-store.timeout         |    No     | Task expiration time, deleted after expiration, default 30 days                                                                                        |
| mj.proxy.host                 |    No     | Proxy host, set when the global proxy does not take effect                                                                                         |
| mj.proxy.port                 |    No     | Proxy port, set when the global proxy does not take effect                                                                                         |
| mj.ng-discord.server          |    No     | https://discord.com anti-generation address                                                                                  |
| mj.ng-discord.cdn             |    No     | https://cdn.discordapp.com Anti-generation address                                                                           |
| mj.ng-discord.wss             |    No     | wss://gateway.discord.gg Anti-generation address                                                                             |
| mj.ng-discord.resume-wss      |    No     | wss://gateway-us-east1-b.discord.gg Anti-generation address                                                                  |
| mj.ng-discord.upload-server   |    No     | https://discord-attachments-uploads-prd.storage.googleapis.com Anti-generation address                                       |
| mj.translate-way              |    No     | The way to translate Chinese prompt into English, optional null (default), baidu, gpt                                                                     |
| mj.baidu-translate.appid      |    No     | Baidu Translate’s appid                                                                                                |
| mj.baidu-translate.app-secret |    No     | Baidu Translate app-secret                                                                                           |
| mj.openai.gpt-api-url         |    No     | Customize the interface address of gpt, no configuration is required by default                                                                                       |
| mj.openai.gpt-api-key         |    No     | gpt's api-key                                                                                               |
| mj.openai.timeout             |    No     | Timeout for openai call, default 30 seconds                                                                                       |
| mj.openai.model               |    No     | openai's model, default gpt-3.5-turbo                                                                                 |
| mj.openai.max-tokens          |    No     | The maximum number of tokens returned in the result, default 2048                                                                                         |
| mj.openai.temperature         |    No     | Similarity (0-2.0), default 0                                                                                            |
| spring.redis                  |    No     | The task storage mode is set to redis, and redis related attributes need to be configured                                                                               |

### Account pool configuration reference
```yaml
mj:
  accounts:
    - guild-id: xxx
      channel-id: xxx
      user-token: xxxx
      user-agent: xxxx
    - guild-id: xxx
      channel-id: xxx
      user-token: xxxx
      user-agent: xxxx
```

Account field description

| Name            | Required | Description                                   |
|:----------------|:--------:|:----------------------------------------------|
| guild-id        |   Yes    | discord server ID                                  |
| channel-id      |   Yes    | discord channel ID                                   |
| user-token      |   Yes    | discord userToken                                |
| user-agent      |    No    | User-agent when calling the discord interface and connecting to wss, it is recommended to copy it from the browser network |
| enable          |    No    | Whether it is available, default true                                   |
| core-size       |    No    | Concurrency number, default 3                                       |
| queue-size      |    No    | Waiting queue length, default 10                                   |
| timeout-minutes |    No    | Task timeout (minutes), default 5                                |

### spring.redis configuration reference
```yaml
spring:
  redis:
    host: 10.107.xxx.xxx
    port: 6379
    password: xxx
```