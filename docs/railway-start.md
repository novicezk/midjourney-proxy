## Railway 部署教程

### 1. Railway是什么
Railway是一个提供了弹性部署方案的平台，支持免费部署。而且服务就架设在海外，方便了Midjourney的调用。

### 2. Fork本仓库
### 3. Railway使用github账号登录
进入 [railway官网](https://railway.app) 选择 `Login` -> `Github`，登录github账号

### 4. [New Project](https://railway.app/new) 添加对fork仓库的授权
![railway_img_1](./railway_img_1.png)
![railway_img_2](./railway_img_2.png)
![railway_img_3](./railway_img_3.png)

### 5. 选择该fork仓库，新建项目，设置环境变量
![railway_img_4](./railway_img_4.png)
![railway_img_5](./railway_img_5.png)
![railway_img_6](./railway_img_6.png)
![railway_img_7](./railway_img_7.png)
此处配置项参考 [Wiki / 配置项](https://github.com/novicezk/midjourney-proxy/wiki/%E9%85%8D%E7%BD%AE%E9%A1%B9) ，建议配置api密钥启用鉴权，接口调用时需添加请求头 `mj-api-secret`

### 6. 启动服务
进入刚才的Project，它应该已经在自动部署了。后续更新配置之后会自动重新部署
![railway_img_8](./railway_img_8.png)

### 7. 开始使用
等待部署成功后，配置网址
![railway_img_9](./railway_img_9.png)

访问 `https://midjourney-proxy-***.app/mj`
