# powershell script to launch the bookmartian server in a linux docker container on windows
#
# Example usage:
#
# usage: . ./go-docker.ps1

# get the latest image tagged with current user's linux username
$username = $(wsl whoami)
$imageId = $(docker images --filter "reference=*-$username" --format "{{.ID}}" | Select-Object -first 1)

# run that image with default port and data location for dev
$dataDir = "" + $(Get-Location) + "\data\"
docker run -p 80:80 -v $($dataDir + ":/data") $imageId