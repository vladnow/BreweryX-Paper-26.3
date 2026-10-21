$ErrorActionPreference='Stop'
$apis=@(
    @{File='LogBlock.jar'; Url='https://raw.githubusercontent.com/BreweryTeam/BreweryX/343872c66a6f37ec4da420cbf12637b4574d5d21/lib/LogBlock.jar'; Sha='25FFE1EDABF9307BCA43B96B8A9423D6DB299738EDAAEF8F3B488C670276D482'},
    @{File='Movecraft.jar'; Url='https://github.com/APDevTeam/Movecraft/releases/download/8.0.0_beta-6/Movecraft_8.0.0_beta-6.jar'; Sha='5FBC36B24B8C69AD7AF24B5C40D2359BDCC6905C50163C5E7D2F335CD35A1D6E'}
)
New-Item -ItemType Directory -Path (Join-Path $PSScriptRoot 'lib') -Force | Out-Null
foreach($api in $apis) {
    $path=Join-Path $PSScriptRoot "lib\$($api.File)"
    if(!(Test-Path -LiteralPath $path)) { Invoke-WebRequest -Uri $api.Url -OutFile $path }
    if((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $api.Sha) { throw "Unexpected compile-only API checksum: $($api.File). Existing file preserved; inspect it before continuing." }
}
Write-Output 'Compile-only APIs verified. They are not included in the runtime plugin.'
