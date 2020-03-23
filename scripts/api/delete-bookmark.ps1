#requires -PSEdition Core

# Powershell script to delete a bookmark with the bookmartian api
#
# to install PowerShell on windows or linux
# https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7
# 
# Example usage:
#
# .\delete-bookmark.ps1 -url http://www.bing.com/
# .\get-bookmarks.ps1 mytag | .\delete-bookmark.ps1

[CmdletBinding(DefaultParameterSetName = 'ByUrl')]
param (
    [Parameter(
        Position=0, 
        Mandatory = $true,
        ParameterSetName = 'ByUrl')
    ]
    $url,

    [Parameter(
        Mandatory = $true,
        ParameterSetName = 'ByObject',
        ValueFromPipeline = $True
        )
    ]
    $bookmark,

    [Alias('host')]
    $endpoint='http://localhost'
)

begin {
    $totalDeleted = 0
}

process {
    If ($PSCmdlet.ParameterSetName -eq 'ByUrl') {
        $enc_url = [System.Web.HttpUtility]::UrlEncode($url)
    } else {
        $enc_url = [System.Web.HttpUtility]::UrlEncode($bookmark.url)
    }
    $response = {}
    $response = curl -s -d "url=$enc_url" $($endpoint+'/api/bookmark/delete') | ConvertFrom-Json

    if ($LASTEXITCODE) {
        Write-Error $Error[0].Exception.Message
    } else {
        if ($response.data) {
            $totalDeleted += 1
        }
        write-output $response.data
    }
}

end {
    write-host "Deleted" $totalDeleted "bookmarks"
}