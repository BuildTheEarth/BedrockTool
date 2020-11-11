@echo off
title Bedrock Tool
set /p region= "Region: " 
set /p discordUsername= "Discord Username: "
java -jar BedrockTool.jar %region% %discordUsername%
pause