## 获取discord配置参数

### 1. 获取用户Token
进入频道，打开network，刷新页面，找到 `messages` 的请求，这里的 authorization 即用户Token，后续设置到 `mj.discord.user-token`

![User Token](img_8.png)

### 2. 获取服务器ID、频道ID

频道的url里取出 服务器ID、频道ID，后续设置到配置项
![Guild Channel ID](img_9.png)
