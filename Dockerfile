FROM maven:3.9-eclipse-temurin-17-alpine AS builder

ENV SPRING_HOME=/home/spring

RUN mkdir -p $SPRING_HOME/config \
	&& mkdir -p $SPRING_HOME/logs

# Railway 不支持使用 VOLUME, 本地需要构建时，取消下一行的注释
# VOLUME ["$SPRING_HOME/config", "$SPRING_HOME/logs"]

WORKDIR $SPRING_HOME

COPY . .

RUN mvn clean package \
    && mv target/midjourney-proxy-*.jar ./app.jar \
    && rm -rf target

FROM amazoncorretto:17-alpine
COPY --from=builder /home/spring/app.jar /home/spring/app.jar
EXPOSE 8080 9876

ENV JAVA_OPTS -XX:MaxRAMPercentage=85 -Djava.awt.headless=true -XX:+HeapDumpOnOutOfMemoryError \
 -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -Xlog:gc:file=/home/spring/logs/gc.log \
 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9876 -Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.authenticate=false -Dlogging.file.path=/home/spring/logs \
 -Dserver.port=8080 -Duser.timezone=Asia/Shanghai

ENTRYPOINT ["bash","-c","java $JAVA_OPTS -jar app.jar"]