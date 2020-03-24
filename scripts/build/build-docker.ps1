#requires -PSEdition Core

# Powershell script to build docker container with correct tagging
# NOTE: this script is only necessary on Windows until WSLv2 ships and I will be able to run docker build from bash
#
# to install PowerShell on windows or linux
# https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7
# 
# Example usage:
#
# .\build.ps1

$buildVersion = $(wsl /mnt/d/local/70-code/73-github-personal/bookmartian/scripts/build/gettag)

docker build -t bookmartian:$buildVersion --build-arg BUILDVERSION=$buildVersion .
docker tag bookmartian:$buildVersion bookmartian.azurecr.io/martylamb/bookmartian:$buildVersion
