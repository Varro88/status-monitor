FROM azul/zulu-openjdk:17-latest
VOLUME /tmp
COPY build/libs/*.jar status-monitor.jar
ENTRYPOINT ["java","-jar","/status-monitor.jar"]
