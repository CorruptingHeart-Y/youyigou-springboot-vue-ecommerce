FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
RUN mkdir -p /app/uploads /app/logs /app/frontend/dist
ARG JAR_FILE=ecommerce-gateway/target/*.jar
COPY ${JAR_FILE} app.jar
COPY frontend/dist/ /app/frontend/dist/
EXPOSE 8080
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
