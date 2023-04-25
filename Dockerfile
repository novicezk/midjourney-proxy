FROM openjdk:17.0

ARG user=spring
ARG group=spring

ENV SPRING_HOME=/home/spring

ENV JAVA_OPTS -XX:MaxRAMPercentage=85 -Djava.awt.headless=true -XX:+HeapDumpOnOutOfMemoryError \
 -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -Xlog:gc:file=/home/spring/logs/gc.log \
 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9876 -Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.authenticate=false -Dlogging.file.path=/home/spring/logs \
 -Dserver.port=8080 -Duser.timezone=Asia/Shanghai

RUN groupadd -g 1000 ${group} \
	&& useradd -d "$SPRING_HOME" -u 1000 -g 1000 -m -s /bin/bash ${user} \
	&& mkdir -p $SPRING_HOME/config \
	&& mkdir -p $SPRING_HOME/logs \
	&& chown -R ${user}:${group} $SPRING_HOME/config $SPRING_HOME/logs

VOLUME ["$SPRING_HOME/config", "$SPRING_HOME/logs"]

USER ${user}

WORKDIR $SPRING_HOME

EXPOSE 8080 9876

ENTRYPOINT ["bash","-c","java $JAVA_OPTS -jar app.jar"]

COPY --chown=${user}:${group} target/*.jar $SPRING_HOME/app.jar