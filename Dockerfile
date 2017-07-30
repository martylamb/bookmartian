FROM alpine:latest

RUN set -ex && \
    apk upgrade --update && \
    apk add openjdk8-jre
    
RUN mkdir -p /opt/bookmartian

COPY target/*-jar-with-dependencies.jar /opt/bookmartian/bookmartian.jar  

EXPOSE 4567

CMD java -cp /opt/bookmartian/bookmartian.jar com.martiansoftware.bookmartian.App -d /data
