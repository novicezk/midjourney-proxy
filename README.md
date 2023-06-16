# midjourney-proxy

代理 MidJourney 的discord频道，实现api形式调用AI绘图

[![GitHub release](https://img.shields.io/static/v1?label=release&message=v2.2.3&color=blue)](https://www.github.com/novicezk/midjourney-proxy)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 现有功能
- [x] 支持 Imagine 指令和相关U、V操作
- [x] Imagine 时支持添加图片base64，作为垫图
- [x] 支持 Describe 指令，根据图片生成 prompt
- [x] 支持 Blend 指令，多个图片混合
- [x] 支持 Imagine、V、Blend 图片生成进度
- [x] 支持中文 prompt 翻译，需配置百度翻译或 gpt
- [x] prompt 敏感词判断，支持覆盖调整
- [x] 任务队列，默认队列10，并发3。可参考 [MidJourney订阅级别](https://docs.midjourney.com/docs/plans) 调整mj.queue
- [x] user-token 连接 wss，可以获取错误信息和完整功能
- [x] 支持 discord域名(server、cdn、wss)反代，配置 mj.ng-discord

## 后续计划
- [ ] 支持 Blend 指令后续的U、V操作
- [ ] 支持 Reroll 操作
- [ ] 支持配置账号池，分发绘图任务
- [ ] 修复相关Bug，[Wiki / 已知问题](https://github.com/novicezk/midjourney-proxy/wiki/%E5%B7%B2%E7%9F%A5%E9%97%AE%E9%A2%98)

## 使用前提
1. 注册 MidJourney，创建自己的频道，参考 https://docs.midjourney.com/docs/quick-start
2. 获取用户Token、服务器ID、频道ID：[获取方式](./docs/discord-params.md)

## 风险须知
1. 作图频繁等行为，可能会触发midjourney账号警告，请谨慎使用
2. 为减少风险，请设置`mj.discord.user-agent`，也可以自定义prompt中任务ID的前后字符，参考 [配置项](https://github.com/novicezk/midjourney-proxy/wiki/%E9%85%8D%E7%BD%AE%E9%A1%B9)
3. 默认使用user-wss方式，可以获取midjourney的错误信息、图片变换进度等，但可能会增加账号风险
4. 支持设置mj.discord.user-wss为false，使用bot-token连接wss，需添加自定义机器人：[流程说明](./docs/discord-bot.md)

## Railway 部署
基于Railway平台部署，不需要自己的服务器: [部署方式](./docs/railway-start.md)；若Railway不能使用，可用下方的Zeabur部署

## Zeabur 部署
基于Zeabur平台部署，不需要自己的服务器: [部署方式](./docs/zeabur-start.md)

## Docker 部署
1. /xxx/xxx/config目录下创建 application.yml(mj配置项)、banned-words.txt(可选，覆盖默认的敏感词文件)；参考src/main/resources下的文件
2. 启动容器，映射config目录
```shell
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 -v /xxx/xxx/config:/home/spring/config \
 --restart=always \
 novicezk/midjourney-proxy:2.2.3
```
3. 访问 `http://ip:port/mj` 查看API文档

附: 不映射config目录方式，直接在启动命令中设置参数
```shell
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 -e mj.discord.guild-id=xxx \
 -e mj.discord.channel-id=xxx \
 -e mj.discord.user-token=xxx \
 --restart=always \
 novicezk/midjourney-proxy:2.2.3
```
## 配置项
- mj.discord.guild-id：discord服务器ID
- mj.discord.channel-id：discord频道ID
- mj.discord.user-token：discord用户Token
- mj.discord.user-wss：是否使用user-token连接wss，默认true
- mj.discord.user-agent：调用discord接口、连接wss时的user-agent，默认使用作者的，建议从浏览器network复制替换掉
- mj.discord.bot-token：自定义机器人Token，user-wss=false时必填
- 更多配置查看 [Wiki / 配置项](https://github.com/novicezk/midjourney-proxy/wiki/%E9%85%8D%E7%BD%AE%E9%A1%B9)

## Wiki链接
1. [Wiki / API接口说明](https://github.com/novicezk/midjourney-proxy/wiki/API%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E)
2. [Wiki / 任务变更回调](https://github.com/novicezk/midjourney-proxy/wiki/%E4%BB%BB%E5%8A%A1%E5%8F%98%E6%9B%B4%E5%9B%9E%E8%B0%83)
2. [Wiki / 更新记录](https://github.com/novicezk/midjourney-proxy/wiki/%E6%9B%B4%E6%96%B0%E8%AE%B0%E5%BD%95)

## 注意事项
1. 常见问题及解决办法见 [Wiki / FAQ](https://github.com/novicezk/midjourney-proxy/wiki/FAQ) 
2. 在 [Issues](https://github.com/novicezk/midjourney-proxy/issues) 中提出其他问题或建议
3. 感兴趣的朋友也欢迎加入交流群讨论一下，扫码进群名额已满，加管理员微信邀请进群

 <img src="https://raw.githubusercontent.com/novicezk/midjourney-proxy/main/docs/manager-qrcode.png" width="320" alt="微信二维码"/>

## 本地开发
- 依赖java17和maven
- 更改配置项: 修改src/main/application.yml
- 项目运行: 启动ProxyApplication的main函数
- 更改代码后，构建镜像: Dockerfile取消VOLUME的注释，执行 `docker build . -t midjourney-proxy`

## 应用项目
- [wechat-midjourney](https://github.com/novicezk/wechat-midjourney) : 代理微信客户端，接入MidJourney，仅示例应用场景，不再更新
- [stable-diffusion-mobileui](https://github.com/yuanyuekeji/stable-diffusion-mobileui) : SDUI，基于本接口和SD，可一键打包生成H5和小程序
- [ChatGPT-Midjourney](https://github.com/Licoy/ChatGPT-Midjourney) : 一键拥有你自己的 ChatGPT+Midjourney 网页服务
- 依赖此项目且开源的，欢迎联系作者，加到此处展示

## 其它
如果觉得这个项目对你有所帮助，请帮忙点个star；也可以请作者喝杯茶～

 <img src="https://raw.githubusercontent.com/novicezk/midjourney-proxy/main/docs/receipt-code.png" width="220" alt="二维码"/>

![Star History Chart](https://api.star-history.com/svg?repos=novicezk/midjourney-proxy&type=Date)
