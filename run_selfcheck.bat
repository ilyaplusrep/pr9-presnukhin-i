@echo off
chcp 65001 > nul
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
for /r src %%f in (*.java) do echo %%f>>sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr06.SelfCheck %*
