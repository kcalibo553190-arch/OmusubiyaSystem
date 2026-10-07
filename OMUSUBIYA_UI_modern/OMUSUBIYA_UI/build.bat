@echo off
rem Compile and package the Omusubiya system (requires JDK 14 or newer)
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -d out *.java
if errorlevel 1 exit /b 1
jar cfe RoleBasedPOSSystem.jar Main -C out .
echo Done. Run run.bat to start the system.
