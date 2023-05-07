## discord 添加机器人

https://discord.com/developers/applications

![img_1.png](img_1.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)

刷新token后显示，即机器人Token，后续配置到 `mj.discord.bot-token`

![img_5.png](img_5.png)

如图勾选后，打开url进行授权

![img_6.png](img_6.png)

勾选这两个选项

进入频道，打开network发送消息，这里的 authorization 即用户Token，后续设置到 `mj.discord.user-token`
![img_7.png](img_7.png)

![img_8.png](img_8.png)

频道的url里取出 服务器ID、频道ID

把 MidJourney Bot 邀请到要让它作图的频道，
把自己的 bot 也拉到 MidJourney Bot 所在的频道(Edit Channel -> Permissions -> Add Members or roles；Member下面选中bot名字，按 Done 按钮)