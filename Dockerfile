FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser
USER appuser

COPY build/libs/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]