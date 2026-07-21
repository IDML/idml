from sbtscala/scala-sbt:amazoncorretto-al2023-17.0.16_1.12.11_2.13.18 as sbt
copy . /build/
workdir /build/
run sbt "project tool" "docker:stage"
workdir /build/tool/target/docker/stage

FROM amazoncorretto:17-alpine3.18-full
RUN apk update
RUN apk add bash
WORKDIR /opt/docker
COPY --from=sbt --chown=daemon:daemon /build/tool/target/docker/stage/opt /opt
EXPOSE 8081
USER daemon
ENTRYPOINT ["/opt/docker/bin/idml-tool"]
CMD []

