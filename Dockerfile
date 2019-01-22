from hseeberger/scala-sbt as sbt
copy . /build/
workdir /build/
run sbt "project tool" "docker:stage"
workdir /build/tool/target/docker/stage

FROM openjdk:8
WORKDIR /opt/docker
COPY --from=sbt --chown=daemon:daemon /build/tool/target/docker/stage/opt /opt
EXPOSE 8081
USER daemon
ENTRYPOINT ["/opt/docker/bin/idml-tool"]
CMD []

