param([switch]$Offline,[string]$GradleExecutable)
$ErrorActionPreference='Stop'
& (Join-Path $PSScriptRoot 'prepare-apis.ps1')
if(!$env:JAVA_HOME) {
    $localJava='F:\ИИшка\plugins\.tools\jdk-25.0.4.1+1'
    if(Test-Path -LiteralPath "$localJava\bin\java.exe") { $env:JAVA_HOME=$localJava }
}
if(!$GradleExecutable) {
    $localGradle='F:\ИИшка\моды\farmers-delight\.tools\gradle-user-home\wrapper\dists\gradle-9.6.1-bin\4ticwg1pgcbps2hj28r8so764\gradle-9.6.1\bin\gradle.bat'
    if(Test-Path -LiteralPath $localGradle) { $GradleExecutable=$localGradle }
    else { $GradleExecutable=Join-Path $PSScriptRoot 'gradlew.bat' }
}
$localCache=Join-Path (Split-Path $PSScriptRoot -Parent) 'tools\gradle-home'
if(!$env:GRADLE_USER_HOME -and (Test-Path -LiteralPath $localCache)) { $env:GRADLE_USER_HOME=$localCache }
$options=@('--no-daemon','-p',$PSScriptRoot)
if($env:OS -eq 'Windows_NT') {
    $codePage=[Globalization.CultureInfo]::CurrentCulture.TextInfo.ANSICodePage
    $options+="-Dorg.gradle.jvmargs=-Dfile.encoding=windows-$codePage"
}
if($Offline) { $options+='--offline' }
& $GradleExecutable @options clean build sourcesJar
if($LASTEXITCODE -ne 0) { throw 'Build/test failed. See Gradle output.' }
Write-Output 'Runtime plugin and source JAR are in build/libs.'
