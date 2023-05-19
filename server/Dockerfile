FROM amazoncorretto:11-alpine-jdk
RUN apk --update add tzdata && \
    cp /usr/share/zoneinfo/Europe/Moscow /etc/localtime && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9091
COPY target/*jar server.jar
ENTRYPOINT ["java","-jar", "/server.jar"]
