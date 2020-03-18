# Powershell script to rename a tag (brute force) bookmartian api
#
# to install PowerShell on windows or linux
# https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7
# 
# The updated bookmark objects are written to the pipeline on exit
#
# Example usage:
#
# .\rename-tag.ps1 geek nerd

param (
    [Parameter(
        Position=0, 
        Mandatory=$true)
    ]
    [Alias('t')]
    $tag,

    [Parameter(
        Position=1, 
        Mandatory=$true)
    ]
    [Alias('n')]
    $new,

    [Alias('host')]
    $endpoint='http://localhost'
)

$enc_tag = [System.Web.HttpUtility]::UrlEncode($tag)
$enc_new = [System.Web.HttpUtility]::UrlEncode($new)

$bookmarks = curl -s $($endpoint+'/api/bookmarks?q='+$enc_tag) | ConvertFrom-Json

$totalUpdated = 0

if ($LASTEXITCODE) {
    Write-Error $Error[0].Exception.Message
} else {
    $bookmarks.data.bookmarks | ForEach-Object {
        $newtags = $_.tags -replace $enc_tag, $enc_new
        $enc_newtags = [System.Web.HttpUtility]::UrlEncode($newtags)
        $enc_title = [System.Web.HttpUtility]::UrlEncode($_.title)
        $enc_url = [System.Web.HttpUtility]::UrlEncode($_.url)
        $enc_imageUrl = [System.Web.HttpUtility]::UrlEncode($_.imageUrl)
        $enc_notes = [System.Web.HttpUtility]::UrlEncode($_.notes)
        $res = curl -s -d "title=$enc_title&imageUrl=$enc_imageUrl&notes=$enc_notes&url=$enc_url&tags=$enc_newtags" $($endpoint+'/api/bookmark/update') | ConvertFrom-Json
        if ($res.data) {
            $totalUpdated += 1
        }
        Write-Output $res.data
    }
}

write-host "Updated" $totalUpdated "bookmarks"
