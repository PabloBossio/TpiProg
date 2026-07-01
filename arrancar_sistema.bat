@echo off
title Sistema de Subastas - Presentación TPI
echo ====================================================
echo   PREPARANDO ENTORNO PARA LA DEFENSA DEL TPI
echo ====================================================

:: Se para en la carpeta del script (TpiProg)
cd /d "%~dp0"

:: Configura la ruta de Java 24
set JAVA_HOME=C:\Program Files\Java\jdk-24

:: AGREGA POWERSHELL AL PATH TEMPORALMENTE (Evita el error de "Cannot start maven from wrapper")
set PATH=%JAVA_HOME%\bin;C:\Windows\System32\WindowsPowerShell\v1.0\;%PATH%

echo.
echo [1/2] Compilando Backend y Frontend unificados con Maven...
echo (Esto puede demorar un momento, compilando React y Java...)
echo ====================================================
echo.

:: Llama al empaquetador nativo usando la consola reparada
call mvnw.cmd clean package -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al compilar el proyecto. 
    echo Verifica que tengas conexion e internet la primera vez para las dependencias.
    goto final
)

echo.
echo ====================================================
echo   [2/2] ¡COMPILACION EXITOSA! ARRANCANDO SERVIDOR
echo ====================================================
echo.

:: Busca de forma inteligente el archivo .jar recién generado en target y lo corre
for %%i in (target\*.jar) do (
    echo [INFO] Iniciando ejecutable: %%i
    java -jar "%%i"
    goto final
)

:final
pause