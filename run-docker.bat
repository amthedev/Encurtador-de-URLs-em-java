@echo off
echo Construindo a imagem Docker...
docker build -t url-shortener .

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao construir a imagem Docker. Verifique as mensagens acima.
    pause
    exit /b %ERRORLEVEL%
)

echo Iniciando o container...
docker run -d --name url-shortener -p 8080:8080 --restart unless-stopped url-shortener

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao iniciar o container. Verifique se a porta 8080 está disponível.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Aplicação iniciada com sucesso!
echo Acesse: http://localhost:8080
echo.
echo Para ver os logs, execute: docker logs -f url-shortener
echo Para parar o container, execute: docker stop url-shortener
echo Para remover o container, execute: docker rm url-shortener
pause
