FROM openjdk:alpine

ENV FILEPATH="target/scala-2.13/ucu-scala-final-project-assembly-0.1.jar"
ENV FILE="ucu-scala-final-project-assembly-0.1.jar"

WORKDIR /usr/app
ADD $FILEPATH /usr/app

EXPOSE 8080
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar $FILE
