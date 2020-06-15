#requires -PSEdition Core

# Powershell script to build docker container with correct tagging
# NOTE: this script is only necessary on Windows until WSLv2 ships and I will be able to run docker build from bash
#
# to install PowerShell on windows or linux
# https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7
# 
# Run this command from the root of the project (where the Dockerfile lives)
#
# Example usage:
#
# .\scripts\build\build-docker.ps1

function main() {
    # get the version string from pom and local build number
    $buildVersion = $(wsl /mnt/d/local/70-code/73-github-personal/bookmartian/scripts/build/gettag)

    # build and tag
    docker build -t bookmartian:$buildVersion --build-arg BUILDVERSION=$buildVersion .
    docker tag bookmartian:$buildVersion bookmartian.azurecr.io/martylamb/bookmartian:$buildVersion
}

main $args
