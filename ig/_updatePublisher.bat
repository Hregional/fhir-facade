@ECHO OFF
REM Descarga la ultima version del IG Publisher en input-cache\publisher.jar
IF NOT EXIST input-cache MKDIR input-cache
powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://github.com/HL7/fhir-ig-publisher/releases/latest/download/publisher.jar' -OutFile 'input-cache\publisher.jar'"
