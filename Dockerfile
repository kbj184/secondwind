# 1. Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle 래퍼와 핵심 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 권한 부여 및 빌드 (테스트 제외하여 속도 향상)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 jar 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# .env 파일 역할을 할 실행 인자 (환경 변수 우선)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

# 전역 포트
EXPOSE 8443
