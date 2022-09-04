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
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests --batch-mode'
                echo 'Successfully built UBoat Vault with maven.'
            }
        }
//        stage('Test') {
//            steps {
//                sh 'mvn test'
//                echo 'Successfully ran the tests of UBoat Vault.'
//            }
//        }
        stage('Quality Check') {
            steps {
                withSonarQubeEnv(credentialsId: 'sonarqube-token', installationName: 'UBoat-SonarQube') {
                    sh 'mvn sonar:sonar'
                }
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                    echo 'Successfully ran code Quality Check on SonarQube '
                }
            }
        }
    }
}
