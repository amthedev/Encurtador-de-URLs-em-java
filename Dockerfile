# Use uma imagem base com Maven e Java
FROM maven:3.9.6-eclipse-temurin-17 as builder

# Cria um usuário não-root
RUN useradd -m myuser

# Define o diretório de trabalho
WORKDIR /app

# Copia apenas os arquivos necessários para o build
COPY pom.xml .
COPY src ./src

# Define as permissões corretas
RUN chown -R myuser:myuser /app

# Muda para o usuário não-root
USER myuser

# Compila o projeto
RUN mvn clean package -DskipTests

# Segunda etapa - imagem final
FROM eclipse-temurin:17-jre

# Cria o mesmo usuário não-root
RUN useradd -m myuser

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR da etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Define as permissões corretas
RUN chown -R myuser:myuser /app

# Muda para o usuário não-root
USER myuser

# Porta que a aplicação irá rodar
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
