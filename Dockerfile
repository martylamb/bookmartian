#
# Bookmartian bookmark manager
#
# Stuff you need to know for launching:
#
#   - all data is stored in /data - be sure to mount something here for
#     persistence!
#
# Service runs "raw" (no authentication, no ssl), listening on port
# 4567 (or as specified) for http connections

# build the vue ui in a temp container and copy the results into the final
FROM alpine:latest AS builder
RUN mkdir -p bookmartian/src/vue && \
    apk upgrade --update && \
    apk add maven \
            npm \
            openjdk8
COPY ./src/vue/package*.json /bookmartian/src/vue/
WORKDIR /bookmartian/src/vue
RUN npm install
COPY . /bookmartian/
RUN npm run build
WORKDIR /bookmartian
RUN mvn package

# build the production container with jre, etc.
FROM alpine:latest

RUN set -ex && \
    apk upgrade --update && \
    apk add openjdk8-jre bash && \
    rm -rf /var/cache/apk/*
       
EXPOSE 80

COPY docker-filesystem /

RUN mkdir -p /opt/bookmartian
COPY --from=builder /bookmartian/target/*-jar-with-dependencies.jar /opt/bookmartian/bookmartian.jar 
ENV user.dir /data

CMD java -Duser.dir=/data -Dwebserver.port=80 -cp /opt/bookmartian/bookmartian.jar com.martiansoftware.bookmartian.App
