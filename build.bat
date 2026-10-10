@echo off
if not exist build mkdir build
if not exist dist mkdir dist

for /R build %%f in (*.class) do del "%%f"

javac -cp "lib\mysql-connector-j.jar" -d build src\model\*.java src\dao\*.java src\gui\*.java src\main\*.java
jar cfm dist\WasteManagementSystem.jar manifest.txt -C build .

echo Build complete: dist\WasteManagementSystem.jar
