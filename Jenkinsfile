pipeline {
    agent any
    tools{
        maven 'maven-3-8.6'
    }
    stages {
        stage('Clone') {
            steps {
                git credentialsId: 'github-token', url: 'https://github.com/danielmartonca/UBoatVault.git'
                echo "Successfully cloned repository of UBoat Vault."
            }
        }
        stage('Build') {
            steps {
                bat "mvn -Dmaven.test.failure.ignore=true clean package"
                echo "Successfully built UBoat Vault with maven."
            }
        }
        stage('Test') {
            steps {
                bat "mvn test"
                echo "Successfully ran the tests of UBoat Vault."
            }
        }
    }
}
