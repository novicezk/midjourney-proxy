FROM openjdk:17.0

ARG user=spring
ARG group=spring

ENV SPRING_HOME=/home/spring
ENV APP_HOME=$SPRING_HOME/app

ENV JAVA_OPTS -XX:MaxRAMPercentage=85 -Djava.awt.headless=true -XX:+HeapDumpOnOutOfMemoryError \
 -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -Xlog:gc:file=/home/spring/logs/gc.log \
 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9876 -Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.authenticate=false -Dlogging.file.path=/home/spring/logs \
 -Dserver.port=8080 -Duser.timezone=Asia/Shanghai

RUN groupadd -g 1000 ${group} \
	&& useradd -d "$SPRING_HOME" -u 1000 -g 1000 -m -s /bin/bash ${user} \
	&& mkdir -p $SPRING_HOME/config \
	&& mkdir -p $SPRING_HOME/logs \
	&& mkdir -p $APP_HOME \
	&& chown -R ${user}:${group} $SPRING_HOME/config $SPRING_HOME/logs $APP_HOME

VOLUME ["$SPRING_HOME/config", "$SPRING_HOME/logs"]

USER ${user}

WORKDIR $SPRING_HOME

EXPOSE 8080 9876

ENTRYPOINT ["bash","-c","java $JAVA_OPTS -cp ./app org.springframework.boot.loader.JarLauncher"]

COPY --chown=${user}:${group} dependencies $APP_HOME/
COPY --chown=${user}:${group} spring-boot-loader $APP_HOME/
COPY --chown=${user}:${group} snapshot-dependencies $APP_HOME/
COPY --chown=${user}:${group} application $APP_HOME/
