#requires -PSEdition Core

# Powershell script to update a bookmark with the bookmartian api
#
# to install PowerShell on windows or linux
# https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7
# 
# The updated bookmark object is written to the pipeline on exit
#
# Example usage:
#
# .\update-bookmark.ps1 -endpoint http://localhost -title test -url "http://www.bing.com/ sdf" -tags "test code"
# .\update-bookmark.ps1 -title test -url "http://www.bing.com/ sdf" -tags "test code"

param (
    [Parameter(
        Mandatory=$true)
    ]
    [Alias('name')]
    $title,

    [Parameter(
        Mandatory=$true)
    ]
    $url,

    $tags,
    $imageUrl,
    $notes,

    [Alias('host')]
    $endpoint='http://localhost'
)

$enc_title = [System.Web.HttpUtility]::UrlEncode($title)
$enc_url = [System.Web.HttpUtility]::UrlEncode($url)
$enc_tags = [System.Web.HttpUtility]::UrlEncode($tags)
$enc_imageUrl = [System.Web.HttpUtility]::UrlEncode($imageUrl)
$enc_notes = [System.Web.HttpUtility]::UrlEncode($notes)

$response = curl -s -d "title=$enc_title&url=$enc_url&tags=$enc_tags&imageUrl=$enc_imageUrl&notes=$enc_notes" $($endpoint+'/api/bookmark/update') | ConvertFrom-Json

if ($LASTEXITCODE) {
    Write-Error $Error[0].Exception.Message
} else {
    write-output $response.data
}