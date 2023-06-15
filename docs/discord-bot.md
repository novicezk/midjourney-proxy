## discord 添加机器人

### 1. 创建应用

https://discord.com/developers/applications

![New Application](img_1.png)

![Create](img_2.png)

### 2. 获取机器人Token
![Reset Token](img_3.png)

刷新token后显示，即机器人Token，后续配置到 `mj.discord.bot-token`

### 3. 机器人授权
![Url Generator](img_4.png)

如图勾选后，打开url进行授权

![Authorize](img_5.png)

选择Midjourney Bot所在的服务器

![Confirm](img_6.png)

![Tick And Save Changes](img_7.png)

勾上这两个选项，点击 `Save Changes`

### 4. 检查机器人
在频道中确认是否存在mj机器人和新创建的机器人

![Check Bot](img_10.png)

若不存在，把 MidJourney Bot 邀请到要让它作图的频道， 把自己的 bot 也拉到 MidJourney Bot 所在的频道

Edit Channel -> Permissions -> Add Members or roles；Member下面选中bot名字，按 Done 按钮