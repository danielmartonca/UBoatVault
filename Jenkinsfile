pipeline {
    agent any
    tools {
        maven 'maven-3-8.6'
    }
    stages {
        stage('Clone') {
            steps {
                git credentialsId: 'github-token', url: 'https://github.com/danielmartonca/UBoatVault.git'
                echo 'Successfully cloned repository of UBoat Vault.'
            }
        }
        stage('Quality Check') {
            steps {
                withSonarQubeEnv(credentialsId: 'sonarqube-token', installationName: 'UBoat-SonarQube') {
                    bat 'mvn sonar:sonar'
                }
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                    echo 'Successfully ran code Quality Check on SonarQube '
                }
            }
        }
        stage('Build') {
            steps {
                bat 'mvn -Dmaven.test.failure.ignore=true clean package'
                echo 'Successfully built UBoat Vault with maven.'
            }
        }
        stage('Test') {
            steps {
                bat 'mvn test'
                echo 'Successfully ran the tests of UBoat Vault.'
            }
        }
    }
}
