FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN mkdir -p out && find . -name "*.java" > sources.txt && javac -d out @sources.txt
EXPOSE 8080
CMD ["java", "-cp", "out", "com.financetracker.main.Main"]
