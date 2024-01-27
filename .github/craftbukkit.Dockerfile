# Docker multi-stage build which prepares the image for building InvSee++.
# See https://docs.docker.com/build/building/multi-stage/ for more info in mulit-stage builds.

FROM eclipse-temurin:8-jdk-alpine as jdk8

RUN wget -O BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

RUN java -jar BuildTools.jar --rev 1.8.8 --compile craftbukkit
RUN java -jar BuildTools.jar --rev 1.12.2 --compile craftbukkit
RUN java -jar BuildTools.jar --rev 1.15.2 --compile craftbukkit
RUN java -jar BuildTools.jar --rev 1.16.5 --compile craftbukkit

FROM eclipse-temurin:17-jdk-alpine as jdk17

COPY --from=jdk8 ~/.m2 ~/.m2
COPY --from=jdk8 BuildTools.jar BuildTools.jar

RUN java -jar BuildTools.jar --rev 1.17.1 --compile craftbukkit --remapped
RUN java -jar BuildTools.jar --rev 1.18.2 --compile craftbukkit --remapped
RUN java -jar BuildTools.jar --rev 1.19.4 --compile craftbukkit --remapped
RUN java -jar BuildTools.jar --rev 1.20.2 --compile craftbukkit --remapped
RUN java -jar BuildTools.jar --rev 1.20.4 --compile craftbukkit --remapped

FROM eclipse-temurin:21-jdk-alpine as jdk21

COPY --from=jdk17 ~/.m2 ~/.m2

ENTRYPOINT ["/__cacert_entrypoint.sh"]
