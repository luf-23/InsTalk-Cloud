@echo off
chcp 65001 >nul
cd /d %~dp0
set JAVA="C:\Program Files\Java\jdk-21\bin\java"

set SERVICE=instalk-identity-service

call %SERVICE%\mvnw.cmd -f %SERVICE%\pom.xml package -DskipTests -q
start "%SERVICE%" cmd /k %JAVA% -jar %SERVICE%\target\%SERVICE%-1.0.0.jar
