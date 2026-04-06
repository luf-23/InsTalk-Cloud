@echo off
chcp 65001 >nul
title InsTalk Cloud 启动器
cd /d %~dp0

:: 指定 Java 21 路径
set JAVA_HOME=C:\Program Files\Java\jdk-21
set JAVA=%JAVA_HOME%\bin\java
echo ================================================
echo   InsTalk Cloud - 一键启动所有微服务
echo ================================================
echo.

:: 第一步：构建整个项目
echo [1/2] 正在构建项目 (跳过测试)...
call instalk-identity-service\mvnw.cmd -f pom.xml package -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] 构建失败，请检查 Maven 输出！
    pause
    exit /b 1
)
echo [完成] 构建成功！
echo.

:: 第二步：逐个启动服务
echo [2/2] 正在启动所有服务...
echo.

start "Gateway        :10010" cmd /k "%JAVA%" -jar instalk-gateway\target\instalk-gateway-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "Identity Service:8081" cmd /k "%JAVA%" -jar instalk-identity-service\target\instalk-identity-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "Social Service :8083 " cmd /k "%JAVA%" -jar instalk-social-service\target\instalk-social-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "AI Service     :8084 " cmd /k "%JAVA%" -jar instalk-ai-service\target\instalk-ai-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "Chat Service   :8085 " cmd /k "%JAVA%" -jar instalk-chat-service\target\instalk-chat-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "OSS Service    :8088 " cmd /k "%JAVA%" -jar instalk-oss-service\target\instalk-oss-service-1.0.0.jar

echo.
echo ================================================
echo   所有服务已启动！(各服务已在独立窗口中运行)
echo   Gateway      : http://localhost:10010
echo   Identity     : http://localhost:8081
echo   Social       : http://localhost:8083
echo   AI           : http://localhost:8084
echo   Chat         : http://localhost:8085
echo   OSS          : http://localhost:8088
echo ================================================
pause
