$hostName = "http://localhost"
$jenkinsContainerName = "jenkins-container"
$sonarQubeContainerName = "sonarqube-container"
$jenkinsPort = 8080
$sonarqubePort = 9090
$githubHookUrl = "https://github.com/danielmartonca/UBoatVault/settings/hooks/377374767"
$sonarQubeHookUrl="http://${hostName}:${sonarqubePort}/admin/webhooks"

Clear-Host

$runningContainers = (docker ps) -join " "

# 1. Jenkins
Write-Output "Checking if Jenkins Container is running."
if (!$runningContainers.Contains($jenkinsContainerName)) {
    Write-Output "Jenkins is not running. Starting container..."
    docker container run -d -p $jenkinsPort`:$jenkinsPort -v jenkins-data:/var/jenkins_home --name jenkins-container jenkins/jenkins:lts
    Start-Sleep -Seconds 5
    if (!$runningContainers.Contains($jenkinsContainerName)) {
        Write-Output "Failed to start Jenkins Container...";
        exit -1;
    }
    Write-Output "Jenkins is now running."
}
else {
    Write-Output "Jenkins is already running."
}
Write-Output "`n"

# 2. SonarQube
Write-Output "Checking if SonarQube Container is running."

if (!$runningContainers.Contains($sonarQubeContainerName)) {
    Write-Output "SonarQube is not running. Starting container..."
    docker container run -d -p $sonarqubePort`:$sonarqubePort --name sonarqube-container sonarqube:9.6.1-community
    Start-Sleep -Seconds 15
    if (!$runningContainers.Contains($sonarQubeContainerName)) {
        Write-Output "Failed to start SonarQube Container...";
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

Write-Output "!PLEASE CONFIGURE WEBHOOKS ON SONARQUBE AND GITHUB IN ORDER FOR JENKINS TO WORK PROPERLY!"

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
    Start-Process $githubHookUrl
    Start-Process $sonarQubeHookUrl
}
else {
    Write-Output "ngrok is already running."
}