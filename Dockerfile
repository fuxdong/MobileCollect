FROM base-image:1.0

ADD target/mobileCollect-1.0.0.jar /opt/app.jar

WORKDIR /opt/
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar","simulate=true"]