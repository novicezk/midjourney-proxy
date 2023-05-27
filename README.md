# midjourney-proxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图

## 现有功能
- [x] 支持 Imagine、U、V 指令，绘图完成后回调
- [x] 支持 Describe 指令，根据图片生成 prompt
- [x] 支持中文 prompt 翻译，需配置百度翻译或 gpt
- [x] prompt 敏感词判断，支持覆盖调整
- [x] 任务队列，默认队列10，并发3。可参考 [MidJourney订阅级别](https://docs.midjourney.com/docs/plans) 调整mj.queue
- [x] 支持图片生成进度
- [x] 可选 user-token 连接 wss，以获取错误信息和完整功能

## 后续计划
- [ ] 支持 Blend 指令，多个图片混合
- [ ] 支持配置账号池，分发绘图任务
- [ ] 支持mysql存储，优化任务的查询方式
- [ ] Imagine 时支持上传图片，作为垫图
- [ ] 修复已知bug，[Wiki / 现有问题列表](https://github.com/novicezk/midjourney-proxy/wiki/%E7%8E%B0%E6%9C%89%E9%97%AE%E9%A2%98%E5%88%97%E8%A1%A8)

## 使用前提
1. 科学上网
2. docker环境
3. 注册 MidJourney，创建自己的频道，参考 https://docs.midjourney.com/docs/quick-start
4. 添加自己的机器人（使用user-wss方式时，可跳过该流程）: [流程说明](./docs/discord-bot.md)

## 风险须知
1. 作图频繁等行为，触发midjourney验证码后，需尽快人工验证
2. user-wss方式可以获取midjourney的错误信息、支持图片变换进度，但可能会增加账号风险

## 快速启动

1. 下载镜像
```shell
docker pull novicezk/midjourney-proxy:2.0
```
2. 启动容器，并设置参数
```shell
# /xxx/xxx/config目录下创建 application.yml(mj配置项)、banned-words.txt(可选，覆盖默认的敏感词文件)
# 参考src/main/resources下的文件
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 -v /xxx/xxx/config:/home/spring/config \
 --restart=always \
 novicezk/midjourney-proxy:2.0

# 或者直接在启动命令中设置参数
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 -e mj.discord.guild-id=xxx \
 -e mj.discord.channel-id=xxx \
 -e mj.discord.user-token=xxx \
 -e mj.discord.bot-token=xxx \
 --restart=always \
 novicezk/midjourney-proxy:2.0
```
3. 访问 `http://ip:port/mj` 查看API文档

## 注意事项
1. 常见问题及解决办法见 [Wiki / 常见问题及解决](https://github.com/novicezk/midjourney-proxy/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98) 
2. 在 [Issues](https://github.com/novicezk/midjourney-proxy/issues) 中提出其他问题或建议
3. 感兴趣的朋友也欢迎加入交流群讨论一下，扫码进群名额已满，加管理员微信邀请进群

 <img src="https://raw.githubusercontent.com/novicezk/midjourney-proxy/main/docs/manager-qrcode.png" width = "320" height = "320" alt="微信二维码" align=center />

## 配置项

| 变量名 | 非空 | 描述 |
| :-----| :----: | :---- |
| mj.discord.guild-id | 是 | discord服务器ID |
| mj.discord.channel-id | 是 | discord频道ID |
| mj.discord.user-token | 是 | discord用户Token |
| mj.discord.user-wss | 否 | 是否使用user-token连接wss，默认false(使用bot-token) |
| mj.discord.user-agent | 否 | 调用discord接口、连接wss时的user-agent，建议从浏览器network复制 |
| mj.discord.bot-token | 否 | 自定义机器人Token，user-wss=false时必填 |
| mj.discord.mj-bot-name | 否 | midjourney官方机器人名称，默认 "Midjourney Bot" |
| mj.notify-hook | 否 | 任务状态变更回调地址 |
| mj.task-store.type | 否 | 任务存储方式，默认in_memory(内存\重启后丢失)，可选redis |
| mj.task-store.timeout | 否 | 任务过期时间，过期后删除，默认30天 |
| mj.proxy.host | 否 | 代理host，全局代理不生效时设置 |
| mj.proxy.port | 否 | 代理port，全局代理不生效时设置 |
| mj.queue.core-size | 否 | 并发数，默认为3 |
| mj.queue.queue-size | 否 | 等待队列，默认长度10 |
| mj.queue.timeout-minutes | 否 | 任务超时时间，默认为5分钟 |
| mj.translate-way | 否 | 中文prompt翻译方式，可选null(默认)、baidu、gpt |
| mj.baidu-translate.appid | 否 | 百度翻译的appid |
| mj.baidu-translate.app-secret | 否 | 百度翻译的app-secret |
| mj.openai.gpt-api-key | 否 | gpt的api-key |
| mj.openai.timeout | 否 | openai调用的超时时间，默认30秒 |
| mj.openai.model | 否 | openai的模型，默认gpt-3.5-turbo |
| mj.openai.max-tokens | 否 | 返回结果的最大分词数，默认2048 |
| mj.openai.temperature | 否 | 相似度(0-2.0)，默认0 |
| spring.redis | 否 | 任务存储方式设置为redis，需配置redis相关属性 |

spring.redis配置参考
```yaml
spring:
  redis:
    host: 10.107.xxx.xxx
    port: 6379
    password: xxx
```

## API接口说明

### 1. `/mj/submit/imagine` 提交imagine任务
POST  application/json
```json
{
    // 绘图参数: IMAGINE时必传
    "prompt": "Cat",
    // 自定义字符串: 非必传，供回调到业务系统里使用
    "state": "test:22",
    // 支持每个任务配置不同回调地址，非必传
    "notifyHook": "http://localhost:8113/notify"
}
```
返回 `Message` 描述
- code=1: 提交成功，result为任务ID
    ```json
    {
      "code": 1,
      "description": "成功",
      "result": "8498455807619990"
    }
    ```
- code=21: 任务已存在，UV时可能发生
    ```json
    {
        "code": 21,
        "description": "任务已存在",
        "result": "0741798445574458",
        "properties": {
            "status": "SUCCESS",
            "imageUrl": "https://xxxx"
         }
    }
    ```
- code=22: 提交成功，进入队列等待
    ```json
    {
        "code": 22,
        "description": "排队中，前面还有1个任务",
        "result": "0741798445574458",
        "properties": {
            "numberOfQueues": 1
         }
    }
    ```
- other: 提交错误，description为错误描述

### 2. `/mj/submit/simple-change` 绘图变化-simple
POST  application/json
```json
{
    // 自定义参数，非必传
    "state": "test:22",
    // 任务描述: 选中ID为1320098173412546的第2张图片放大
    // 放大 U1～U4 ，变换 V1～V4
    "content": "1320098173412546 U2",
    // 支持每个任务配置不同回调地址，非必传
    "notifyHook": "http://localhost:8113/notify"
}
```
返回结果同 `/mj/submit/imagine`

### 3. `/mj/submit/describe` 提交describe任务
POST  application/json
```json
{
    // 自定义参数，非必传
    "state": "test:22",
    // 图片的base64字符串
    "base64": "data:image/png;base64,xxx",
    // 支持每个任务配置不同回调地址，非必传
    "notifyHook": "http://localhost:8113/notify"
}
```
返回结果同 `/mj/submit/imagine`

后续任务完成后，task中prompt即为图片生成的prompt
```json
{
  "action":"DESCRIBE",
  "id":"3856553004865376",
  "prompt":"1️⃣ xxx1 --ar 5:4\n\n2️⃣ xxx2 --ar 5:4\n\n3️⃣ xxx3 --ar 5:4\n\n4️⃣ xxx4 --ar 5:4",
  "promptEn":"1️⃣ xxx1 --ar 5:4\n\n2️⃣ xxx2 --ar 5:4\n\n3️⃣ xxx3 --ar 5:4\n\n4️⃣ xxx4 --ar 5:4",
  "description":"/describe 3856553004865376.png",
  "state":"test:22",
  "submitTime":1683779732983,
  "startTime":1683779737321,
  "finishTime":1683779741711,
  "imageUrl":"https://cdn.discordapp.com/ephemeral-attachments/xxxx/xxxx/3856553004865376.png",
  "status":"SUCCESS"
}
```

### 4. `/mj/task/{id}/fetch` GET 查询单个任务
```json
{
    // 动作: IMAGINE（绘图）、UPSCALE（选中放大）、VARIATION（选中变换）
    "action":"IMAGINE",
    // 任务ID
    "id":"8498455807628990",
    // 绘图参数
    "prompt":"猫猫",
    // 翻译后的绘图参数
    "promptEn": "Cat",
    // 执行的命令
    "description":"/imagine 猫猫",
    // 自定义参数
    "state":"test:22",
    // 提交时间
    "submitTime":1682473784826,
    // 开始处理时间
    "startTime":1682473785130,
    // 结束时间
    "finishTime":1682473935151,
    // 生成图片的url, 成功或执行中时有值，可能为png或webp
    "imageUrl":"https://cdn.discordapp.com/attachments/xxx/xxx/xxxx_xxxx.png",
    // 任务状态: NOT_START（未启动）、SUBMITTED（已提交处理）、IN_PROGRESS（执行中）、FAILURE（失败）、SUCCESS（成功）
    "status":"SUCCESS",
    // 进度，可能为空字符或百分比
    "progress":"100%",
    // 失败原因, 失败时有值
    "failReason":""
}
```

### 5. `/mj/task/list` GET 查询所有任务

```json
[
  {
    "action":"IMAGINE",
    "id":"8498455807628990",
    "prompt":"猫猫",
    "promptEn": "Cat",
    "description":"/imagine 猫猫",
    "state":"test:22",
    "submitTime":1682473784826,
    "startTime":1682473785130,
    "finishTime":null,
    "imageUrl":null,
    "status":"IN_PROGRESS",
    "progress":"0%",
    "failReason":""
  }
]
```

## `mj.notify-hook` 任务变更回调
POST  application/json
```json
{
    "action":"IMAGINE",
    "id":"8498455807628990",
    "prompt":"猫猫",
    "promptEn": "Cat",
    "description":"/imagine 猫猫",
    "state":"test:22",
    "submitTime":1682473784826,
    "startTime":1682473785130,
    "finishTime":null,
    "imageUrl":null,
    "status":"IN_PROGRESS",
    "progress":"0%",
    "failReason":""
}
```

## 应用项目

- [wechat-midjourney](https://github.com/novicezk/wechat-midjourney) : 代理微信客户端，接入MidJourney，仅示例应用场景，不再维护
