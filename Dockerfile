FROM amazoncorretto:8u242

COPY target/git-api-jar-with-dependencies.jar  git-api.jar

EXPOSE 8989
VOLUME wd
WORKDIR wd

ENTRYPOINT ["java","-jar","/git-api.jar"]