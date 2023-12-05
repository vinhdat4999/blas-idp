FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar /app/
CMD java -jar $(find /app -name '*.jar' | head -n 1)
