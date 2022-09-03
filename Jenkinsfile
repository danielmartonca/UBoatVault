pipeline {
    agent any

    stages {
        stage('Clone') {
            steps {
                try {
                    git credentialsId: 'github-token', url: 'https://github.com/danielmartonca/UBoatVault.git'
                    echo "Successfully cloned repository of UBoat Vault."
                } catch (err) {
                    error "Failed to clone repository. Reason: \n${err}"
                    currentBuild.result = 'FAILURE'
                }
            }
        }
        stage('Build') {
            steps {
                try {
                    sh "mvn -Dmaven.test.failure.ignore=true clean package"
                    echo "Successfully built UBoat Vault with maven."
                } catch (err) {
                    error "Failed to build UBoat Vaultt with maven. Reason: \n${err}"
                    currentBuild.result = 'FAILURE'
                }
            }
        }
        stage('Test') {
            steps {
                try {
                    sh "mvn test"
                    echo "Successfully ran the tests of UBoat Vault."
                } catch (err) {
                    error "Failed to run the tests of UBoat Vault. Reason: \n${err}"
                    currentBuild.result = 'FAILURE'
                }

            }
        }
    }
}
