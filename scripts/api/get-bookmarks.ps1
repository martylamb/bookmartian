#requires -PSEdition Core

# Powershell script to query bookmartian api
#
# to install PowerShell on windows or linux
# https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7
# 
# The retreived bookmark objects are written to the pipeline on exit
#
# Example usage:
#
# .\get-bookmarks.ps1 -endpoint http://localhost -query "personal tools" | format-table title, tags
# .\get-bookmarks.ps1 "personal tools" | format-table title, tags
# "personal tools" | .\get-bookmarks.ps1 | format-table title, tags


param (
    [Parameter(
        Position=0, 
        Mandatory=$true, 
        ValueFromPipeline=$true,
        ValueFromPipelineByPropertyName=$true)
    ]
    [Alias('q')]
    $query,

    [Alias('host')]
    $endpoint='http://localhost'
)

$enc_query = [System.Web.HttpUtility]::UrlEncode($query)

$response = curl -s $($endpoint+'/api/bookmarks?q='+$enc_query) | ConvertFrom-Json

if ($LASTEXITCODE) {
    Write-Error $Error[0].Exception.Message
} else {
    write-output $response.data.bookmarks
}
