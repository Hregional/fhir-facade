@ECHO OFF
REM Genera el sitio de la guia en output\ (requiere haber corrido _updatePublisher.bat)
IF NOT EXIST input-cache\publisher.jar (
  ECHO Falta input-cache\publisher.jar. Ejecuta primero _updatePublisher.bat
  EXIT /B 1
)
java -Dfile.encoding=UTF-8 -jar input-cache\publisher.jar -ig . %*
