$hostName = "http://localhost"
$jenkinsInstallPath = "C:\Program Files\Jenkins"
$sonarQubeInstallPath = "F:\Programs\sonarqube-9.6.1.59531"
$jenkinsPort = 8080
$sonarqubePort = 9090

function startProcessAndGetOutput {
    param (
        [string]$path,
        [string]$argument
    )
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $path
    $pinfo.RedirectStandardError = $true
    $pinfo.RedirectStandardOutput = $true
    $pinfo.UseShellExecute = $false
    $pinfo.Verb = "RunAs"
    $pinfo.Arguments = $argument
    $p = New-Object System.Diagnostics.Process
    $p.StartInfo = $pinfo
    $p.Start() | Out-Null
    $p.WaitForExit()
    return $p.StandardOutput.ReadToEnd().Trim();
}


# 1. Jenkins
Write-Output "Checking if Jenkins is running."
$ServiceName = "Jenkins"
$jenkinsService = Get-Service -Name $ServiceName
if (-Not ($jenkinsService.Status -eq "Running")) {
    Write-Output "Jenkins is not running. Starting service."
    Start-Process java -ArgumentList '-jar', "$jenkinsInstallPath\Jenkins.war" `
        if (-Not ($jenkinsService.Status -eq "Running")) {
        Write-Output "Failed to start Jenkins Service...";
        exit -1;
    }
    Write-Output "Jenkins is now running."
}
else {
    Write-Output "Jenkins is already running."
}


# 2. SonarQube
$sonarTool = "$sonarQubeInstallPath\bin\windows-x86-64\SonarService.bat"
Write-Output "Checking if SonarQube is running."
$serviceStatus = startProcessAndGetOutput $sonarTool "status"
if (-Not ($serviceStatus -eq "Started")) {
    Write-Output "SonarQube is not running. Starting service."
    startProcessAndGetOutput $sonarTool "stop"
    startProcessAndGetOutput $sonarTool "start"

    $serviceStatus = startProcessAndGetOutput $sonarTool "status"
    if (-Not ($serviceStatus -eq "Started")) {
        Write-Output "Failed to start SonarQube Service...";
        exit -1;
    }
    Write-Output "SonarQube is now running."
}
else {
    Write-Output "SonarQube is already running."
}
Write-Output "`n"
Write-Output "Jenkins running    at: ${hostName}:${jenkinsPort}."
Write-Output "SonarQube running  at: ${hostName}:${sonarqubePort}."
Write-Output "ngrok will forward to: ${hostName}:${jenkinsPort}."
Write-Output "`n"
Write-Output "PLEASE CONFIGURE WEBHOOKS ON SONARQUBE AND GITHUB IN ORDER FOR JENKINS TO WORK PROPERLY"
Write-Host "Press any key to start ngrok..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown');
# 3. ngrok
Write-Output "Checking if ngrok is running."
$status = startProcessAndGetOutput "curl" "http://127.0.0.1:4040"
if (-Not $status) {
    Write-Output "ngrok is not running. Starting ngrock."
    ngrok http $jenkinsPort

    $status = startProcessAndGetOutput "curl" "http://127.0.0.1:4040"
    if (-Not $status) {
        Write-Output "Failed to start ngrok...";
        exit -1;
    }
    Write-Output "ngrok is now running."
}
else {
    Write-Output "ngrok is already running."
}