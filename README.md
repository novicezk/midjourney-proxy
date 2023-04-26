# midjourney-proxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图

## 使用前提
1. 科学上网
2. 注册 MidJourney，创建自己的频道，参考 https://docs.midjourney.com/docs/quick-start
3. 添加自己的机器人: [流程说明](./docs/discord-bot.md)
4. 安装 jdk17

## 快速启动
1. 下载项目
2. 复制 `application.yml.example` 到 `application.yml`
3. 更改配置文件
4. 启动 `ProxyApplication` main 方法

## docker方式启动

1. 下载项目
```shell
git clone https://github.com/novicezk/midjourney-proxy
```
2. 复制 `application.yml.example` 到 `application.yml`
3. 更改配置文件
4. 构建镜像
```shell
cd midjourney-proxy
./build-image.sh
```
5. 启动容器示例
```shell
docker run -itd --name midjourney-proxy \
 -p 8080:8080 \
 --restart=always \
 midjourney-proxy:1.0-SNAPSHOT
```

## 配置项
- `mj-proxy.notify-hook` 任务变更回调地址（可空），配置之后可以主动通知
- `mj-proxy.discord.user-token` 用户Token
- `mj-proxy.discord.bot-token` 自定义机器人Token
- `mj-proxy.discord.guild-id` 服务器ID
- `mj-proxy.discord.channel-id` 频道ID
- `mj-proxy.discord.mj-bot-name` Midjourney机器人的名称，默认 "Midjourney Bot"
- `mj-proxy.openai.gpt-api-key` gpt的api-key，不设置时不支持使用中文描述来绘图

## 注意事项
1. 启动失败请检查科学上网策略，全局代理或HTTP代理
2. docker方式启动，若回调通知接口失败，请检查网络设置，容器中的宿主机IP通常为172.17.0.1
3. 欢迎在 [Issues](https://github.com/novicezk/midjourney-proxy/issues) 中提出其他问题或意见

## API接口说明

### 1. `/trigger/submit` 提交任务
POST  application/json
```json
{
    // 动作: 必传，IMAGINE（绘图）、UPSCALE（选中放大）、VARIATION（选中变换）
    "action":"IMAGINE",
    // 绘图参数: IMAGINE时必传
    "prompt": "猫猫",
    // 任务ID: UPSCALE、VARIATION时必传
    "taskId": "1320098173412546",
    // 图序号: 1～4，UPSCALE、VARIATION时必传，表示第几张图
    "index": 3,
    // 自定义字符串: 任务中保留
    "state": "test:22"
}
```
返回 `Message`，code=1表示提交成功，其他时description为错误描述
```json
{
  "code": 1,
  "description": "成功",
  "result": "8498455807619990"
}
```
result: 任务ID，用于后续查询任务或提交变换任务

### 2. `/trigger/submit-uv` 提交选中放大或变换任务
POST  application/json
```json
{
    // 自定义参数: 任务中保留
    "state": "test:22",
    // 任务描述: 选中ID为1320098173412546的第2张图片放大
    // 放大 U1～U4 ，变换 V1～V4
    "content": "1320098173412546 U2"
}
```
返回结果同 `/trigger/submit`

### 3. `/task/{id}/fetch` GET 查询单个任务
```json
{
    // 动作: IMAGINE（绘图）、UPSCALE（选中放大）、VARIATION（选中变换）
    "action":"IMAGINE",
    // 任务ID
    "id":"8498455807628990",
    // 绘图参数
    "prompt":"猫猫",
    // 绘图参数英文
    "promptEn":"cat",
    // 执行的命令
    "description":"/imagine cat",
    // 自定义参数
    "state":"test:22",
    // 提交时间
    "submitTime":1682473784826,
    // 结束时间
    "finishTime":null,
    // 生成图片的url, 成功时有值
    "imageUrl":"https://cdn.discordapp.com/attachments/xxx/xxx/xxxx__xxxx.png",
    // 任务状态: NOT_START（未启动）、IN_PROGRESS（执行中）、FAILURE（失败）、SUCCESS（成功）
    "status":"IN_PROGRESS"
}
```

### 4. `/task/list` GET 查询所有任务
***任务缓存1天后删除***
```json
[
  {
    "action":"IMAGINE",
    "id":"8498455807628990",
    "prompt":"猫猫",
    "promptEn":"cat",
    "description":"/imagine cat",
    "state":"test:22",
    "submitTime":1682473784826,
    "finishTime":null,
    "imageUrl":null,
    "status":"IN_PROGRESS"
  }
]
```

## `mj-proxy.notify-hook` 任务变更回调
POST  application/json
```json
{
    "action":"IMAGINE",
    "id":"8498455807628990",
    "prompt":"猫猫",
    "promptEn":"cat",
    "description":"/imagine cat",
    "state":"test:22",
    "submitTime":1682473784826,
    "finishTime":null,
    "imageUrl":null,
    "status":"IN_PROGRESS"
}
```