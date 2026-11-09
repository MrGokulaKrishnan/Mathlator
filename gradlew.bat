@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off

set APP_HOME=%~dp0
set JAVA_HOME=C:\Program Files\Java\jdk-17

"%JAVA_HOME%\bin\java.exe" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
