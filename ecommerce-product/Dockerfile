FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
RUN mkdir -p /app/uploads /app/logs
ARG JAR_FILE=ecommerce-product/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8082
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
