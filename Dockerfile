FROM alpine:latest

RUN set -ex && \
    apk upgrade --update && \
    apk add openjdk8-jre
    
EXPOSE 4567 80 443

RUN mkdir -p /opt/bookmartian

COPY target/*-jar-with-dependencies.jar /opt/bookmartian/bookmartian.jar  
COPY docker /

CMD bookmartian
