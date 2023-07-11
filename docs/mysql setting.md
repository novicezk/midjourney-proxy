# 修改config/application.yml
## 修改数据储存类型
``` yaml
mj:
  task-store:
    type: mysql
    timeout: 30d
```

## 修改添加数据库配置

### 选择 mariadb 或者mysql

### mariadb

``` yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/database
    username: username
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDB103Dialect
```

### MYSQL
ps: 以下MYSQL，没有测试

MYSQL 8+
``` yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/database
    username: username
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL5Dialect
```
MYSQL 5

``` yaml
dialect: org.hibernate.dialect.MySQL5Dialect
```

MYSQL 8
``` yaml
dialect: org.hibernate.dialect.MySQL8Dialect
```


附上我在使用的完成mariadb的application.yml 和docker-compose.yaml 配置
``` yaml
mj:
  discord: xxxx
    guild-id: xxx
    channel-id: xxx
    user-token: xxx
    session-id: xxx
    user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36
    botToken: xxx
    user-wss: false
  task-store:
    type: mysql
    timeout: 30d
  translate-way: null
  queue:
    timeout-minutes: 5
    core-size: 3
    queue-size: 10
spring:
  redis:
    host: midjourney-redis
    port: 6379
    db: 2 ## no work,need fix TODO
  datasource:
    url: jdbc:mariadb://midjourney-mariadb:3306/midjourney-proxy
    username: username
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDB103Dialect
```


``` yaml

version: '3.7'

services:
  midjourney-redis:
    image: redis:alpine
    container_name: midjourney-redis
    hostname: midjourney-redis
    ports:
      - "6379:6379"
    expose:
      - "6379"
    networks:
      - default
  midjourney-proxy:
    build: 
      context: .
      dockerfile: Dockerfile-dev
    volumes:
      - ./config:/home/spring/config
      - ./logs:/home/spring/logs
    ports:
      - 8080:8080
      - 9876:9876
    environment:
      - JAVA_OPTS=-XX:MaxRAMPercentage=85 -Djava.awt.headless=true -XX:+HeapDumpOnOutOfMemoryError -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -Xlog:gc:file=/home/spring/logs/gc.log -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9876 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dlogging.file.path=/home/spring/logs -Dserver.port=8080 -Duser.timezone=Asia/Shanghai
    networks:
      - default
  midjourney-mariadb:
    image: mariadb:latest
    container_name: midjourney-mariadb
    hostname: midjourney-mariadb
    networks:
      - default
    volumes:
      - ./mariadb_data:/var/lib/mysql
    ports:
      - "3306:3306"
    expose:
      - "3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=midjourney-proxy
      - MYSQL_USER=midjourney-proxy
      - MYSQL_PASSWORD=midjourney-proxy
```