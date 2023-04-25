# midjourney-proxy

代理 MidJourney 的 [discord](https://discord.com) 频道，实现api形式调用AI绘图

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

## API接口说明
见 `/trigger/submit` 接口