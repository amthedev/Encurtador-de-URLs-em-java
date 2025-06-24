@echo off
cd /d "%~dp0"

rem Remove a pasta target local se existir
if exist target rmdir /s /q target

echo Instalando dependências...
docker run --rm -v "%CD%":/usr/src/mymaven -w /usr/src/mymaven maven:3.9.6-eclipse-temurin-17 mvn dependency:resolve

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao instalar dependências. Verifique as mensagens acima.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilando o projeto...
docker run --rm -v "%CD%":/usr/src/mymaven -w /usr/src/mymaven maven:3.9.6-eclipse-temurin-17 mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao compilar o projeto. Verifique as mensagens acima.
    pause
    exit /b %ERRORLEVEL%
)

echo Construindo o pacote...
docker run --rm -v "%CD%":/usr/src/mymaven -w /usr/src/mymaven maven:3.9.6-eclipse-temurin-17 mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao construir o pacote. Verifique as mensagens acima.
    pause
    exit /b %ERRORLEVEL%
)

echo Build concluído com sucesso!
pause
