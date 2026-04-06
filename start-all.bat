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
call instalk-auth-service\mvnw.cmd -f pom.xml package -DskipTests -q
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

start "Auth Service   :8081 " cmd /k "%JAVA%" -jar instalk-auth-service\target\instalk-auth-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "User Service   :8082 " cmd /k "%JAVA%" -jar instalk-user-service\target\instalk-user-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "Friend Service :8083 " cmd /k "%JAVA%" -jar instalk-friendship-service\target\instalk-friendship-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "AI Service     :8084 " cmd /k "%JAVA%" -jar instalk-ai-service\target\instalk-ai-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "Chat Service   :8085 " cmd /k "%JAVA%" -jar instalk-chat-service\target\instalk-chat-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "Group Service  :8087 " cmd /k "%JAVA%" -jar instalk-group-service\target\instalk-group-service-1.0.0.jar
ping 127.0.0.1 -n 3 >nul 2>&1

start "OSS Service    :8088 " cmd /k "%JAVA%" -jar instalk-oss-service\target\instalk-oss-service-1.0.0.jar

echo.
echo ================================================
echo   所有服务已启动！(各服务已在独立窗口中运行)
echo   Gateway      : http://localhost:10010
echo   Auth         : http://localhost:8081
echo   User         : http://localhost:8082
echo   Friendship   : http://localhost:8083
echo   AI           : http://localhost:8084
echo   Chat         : http://localhost:8085
echo   Group        : http://localhost:8087
echo   OSS          : http://localhost:8088
echo ================================================
pause
