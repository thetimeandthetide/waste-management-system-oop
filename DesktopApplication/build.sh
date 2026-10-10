#!/bin/sh
set -e

mkdir -p build dist
find build -type f -name "*.class" -delete

javac -cp "lib/mysql-connector-j.jar" -d build $(find src -name "*.java")
jar cfm dist/WasteManagementSystem.jar manifest.txt -C build .

echo "Build complete: dist/WasteManagementSystem.jar"
