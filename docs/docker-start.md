## Docker 部署教程

1. Create application.yml (mj configuration item) and banned-words.txt (optional, overwrite the default sensitive word file) in the /xxx/xxx/config directory; refer to the files under src/main/resources
2. Start the container and map the config directory
```shell
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 -v /xxx/xxx/config:/home/spring/config \
 novicezk/midjourney-proxy:2.6.1
```
3. Visit `http://ip:port/mj` to view the API documentation

Attachment: Without mapping the config directory, set parameters directly in the startup command
```shell
docker run -d --name midjourney-proxy \
 -p 8080:8080 \
 -e mj.discord.guild-id=xxx \
 -e mj.discord.channel-id=xxx \
 -e mj.discord.user-token=xxx \
 novicezk/midjourney-proxy:2.6.1
```
