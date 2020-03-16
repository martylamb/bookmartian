#
# Bookmartian bookmark manager
#
# Stuff you need to know for launching:
#
#   - all data is stored in /data - be sure to mount something here for
#     persistence!
#
# Service can run "raw" (no authentication, no ssl), listening on port
# 4567 for http connections, OR behind an (included) caddy server,
# providing ssl, letsencrypt, and authentication.  "Raw" is the default.
#
# To run using (included) caddy server for ssl, letsencrypt, and
# authentication:
#
#   - forward your http port to port 80 in the container
#   - forward your https port to port 443 in the container
#   - create a .htpasswd file and store it in the directory you have
#     mounted in /data (optional)
#
#
# To run raw:
#
#   - forward an external port (tcp) to 4567 in the container

# build the vue ui in a temp container and copy the results into the final
FROM alpine:latest AS builder
RUN apk upgrade --update && \
    apk add npm && \
    apk add openjdk8 && \
    apk add maven
RUN apk add bash
RUN mkdir -p bookmartian/src/vue
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
       
EXPOSE 4567 80 443

COPY docker-filesystem /

RUN mkdir -p /opt/bookmartian
COPY --from=builder /bookmartian/target/*-jar-with-dependencies.jar /opt/bookmartian/bookmartian.jar 

CMD java -cp /opt/bookmartian/bookmartian.jar com.martiansoftware.bookmartian.App
