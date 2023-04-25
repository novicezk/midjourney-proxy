# midjourney-proxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图

## 使用前提
1. 注册 MidJourney，创建自己的频道，参考 https://docs.midjourney.com/docs/quick-start
2. [添加自己的机器人](./docs/discord-bot.md)
3. 安装jdk17

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
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 midjourney-proxy:1.0-SNAPSHOT
```

## 配置项
- `mj-proxy.notify-hook` mj结果通知地址，配置了可以主动回调
- `mj-proxy.discord.user-token` 用户Token
- `mj-proxy.discord.bot-token` 自定义机器人Token
- `mj-proxy.discord.guild-id` 服务器ID
- `mj-proxy.discord.channel-id` 频道ID
- `mj-proxy.discord.mj-bot-name` Midjourney机器人的名称，默认 Midjourney Bot

## API接口说明

### 1. `/trigger/submit` 提交任务
POST  application/json
```json
{
    // 动作: 必传 IMAGINE（绘图）、UPSCALE（选中放大）、VARIATION（变换）
    "action":"IMAGINE",
    // 绘图参数: IMAGINE时必传
    "prompt": "大狗子",
    // 自定义字符串:
    "state": "test:22"
}
```



