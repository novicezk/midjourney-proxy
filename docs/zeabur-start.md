## Zeabur 部署教程

### Zeabur 优势
1. 新注册的 `Github` 账号可能无法使用 `Railway`，但是能用 `Zeabur`
2. 通过 `Railway` 部署的项目会自动生成一个域名，然而因为某些原因，形如 `*.up.railway.app` 的域名在国内无法访问
3. `Zeabur` 服务器运行在国外，但是其生成的域名 `*.zeabur.app` 没有被污染,国内可直接访问

### 开始部署

1. 打开网址 https://zeabur.com/zh-CN 
2. 点击现在开始
3. 点击 `Sign in with GitHub`
4. 登陆你的 `Github` 账号
5. 点击 `Authorize zeabur` 授权
6. 点击 `创建项目` 并输入一个项目名称，点击 `创建`
7. 点击 `+` 添加服务，选择 `Git-Deploy service from source code in GitHub repository.`
8. 点击 `Configure GitHub` 根据需要选择 `All repositories` 或者 `Only select repositories`
9. 点击 `install`，之后自动跳转，最好再刷新一下页面
10. 点击 你 fork 的 `midjourney-proxy` 项目
11. 点击环境变量，点击编辑原始环境变量，添加你需要的环境变量
12. 关于环境变量，与 `Railway` 稍有不同，需要把 `.` 和 `-` 全部换成 `_`，例如如下格式
    ```properties
    PORT=8080
    mj_discord_guild_id=xxx
    mj_discord_channel_id=xxx
    mj_discord_user_token=xxx
    mj_api_secret=***
    ```
    此处配置项参考 [Wiki / 配置项](https://github.com/novicezk/midjourney-proxy/wiki/%E9%85%8D%E7%BD%AE%E9%A1%B9) ，建议配置api密钥启用鉴权，接口调用时需添加请求头 `mj-api-secret`
13. 然后取消 `Building`，点击 `Redeploy` (此做法是为了让环境变量生效)
14. 部署 `midjourney-proxy` 大概需要 `2` 分钟，此时你可以做的是：配置域名
15. 点击下方的域名，点击生成域名，输入前缀，例如 `midjourney-proxy-demo`，点击保存；或者添加自定义域名，之后加上 `CNAME` 解析
16. 等待部署成功，访问 `https://midjourney-proxy-demo.zeabur.app/mj`