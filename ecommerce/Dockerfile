FROM amazoncorretto:17

# Çalışma dizini ayarla
WORKDIR /usr/app

# JAR dosyasını kopyala
COPY target/product-service.jar /usr/app/product-service.jar

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "product-service.jar"]


