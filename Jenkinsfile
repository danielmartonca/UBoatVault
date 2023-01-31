def VERSION
def imageTag

pipeline {
    agent any

    tools {
        maven 'maven-3-8.6'
        dockerTool 'docker-latest'
    }

    environment {
        gitUrl = 'https://github.com/danielmartonca/UBoatVault.git'
        uboatUrl = 'https://uboat-vault.herokuapp.com'
    }
    parameters {
        string(name: 'VERSION', defaultValue: '')
        string(name: 'imageTag', defaultValue: '')
    }
    stages {
        stage('Clone') {
            steps {
                git credentialsId: 'github-jenkins-token', url: gitUrl
                script {
                    VERSION = readMavenPom().getVersion()
                    imageTag = "danielmartonca/uboat-vault:$VERSION"
                }
                echo 'Successfully cloned repository of UBoat Vault.'
            }
        }

        stage('Build') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    echo "Building UBoat-Vault-${VERSION}."
                    bat 'mvn clean package -P production -DskipTests --batch-mode'
                    echo 'Successfully built UBoat Vault with maven.'
                }
            }
        }

        stage('Test') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    bat 'mvn test -P test -Dspring.profiles.active=test'
                    echo 'Successfully ran the tests of UBoat Vault.'
                }
            }
        }

        stage('Quality Check') {
            steps {
                withSonarQubeEnv(credentialsId: 'sonarqube-token', installationName: 'UBoat-SonarQube') {
                    bat 'mvn sonar:sonar'
                }
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                    echo 'Successfully ran code Quality Check on SonarQube '
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t $imageTag ."
                echo "Built Docker Image '$imageTag'"
            }
        }

        stage('Push Image to Dockerhub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'password', usernameVariable: 'username')]) {
                    bat 'docker logout'
                    bat "docker login -u $username -p $password"
                    bat "docker push \"${imageTag}\""
                    echo "Pushed the docker image ${imageTag} to Docker Hub"
                }
            }
        }

        stage('Deploy') {
            steps {
//                withCredentials([string(credentialsId: 'heroku-token', variable: 'authToken')]) {
//                    bat 'heroku container:login'
//                    bat 'heroku git:remote -a uboat-vault'
//                    bat 'heroku container:push web'
//                    bat 'heroku container:release web'
                echo 'Deployed Docker Image to the Environment successfully.'
//                }
            }
        }

//        stage('Test if Vault is running') {
//            steps {
//                script {
//                    final String response = bat(script: "curl -s $uboatUrl/api/isVaultActive", returnStdout: true).trim()
//                    echo response
//                }
//            }
//        }
    }

    post {
        always
                {
                    bat "docker rmi ${imageTag}"
                    bat 'docker logout'
                }
    }
}
