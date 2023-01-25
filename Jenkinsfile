pipeline {
    agent any

    tools {
        maven 'maven-3-8.6'
        dockerTool 'docker-latest'
    }

    environment {
        gitUrl = 'https://github.com/danielmartonca/UBoatVault.git'
        uboatUrl = 'https://uboat-vault.herokuapp.com'
        VERSION = readMavenPom().getVersion()
        dockerAccount = "danielmartonca"
        registry = "uboat-vault"
        imageTag = "$VERSION"
    }

    stages {
        stage('Clone') {
            steps {
                git credentialsId: 'github-token', url: gitUrl
                echo 'Successfully cloned repository of UBoat Vault.'
            }
        }

        stage('Build') {
            steps {
                echo "Building UBoat-Vault-${VERSION}."
                sh 'mvn clean package -P production -DskipTests --batch-mode'
                echo 'Successfully built UBoat Vault with maven.'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -P test -Dspring.profiles.active=test'
                echo 'Successfully ran the tests of UBoat Vault.'
            }
        }

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

        stage('Build Docker Image') {
            steps {
                sh "docker build -t $imageTag ."
                echo "Built Docker Image '$imageTag'"
            }
        }

        stage('Push Image to Dockerhub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'password', usernameVariable: 'username')]) {
                    sh 'docker logout > /dev/null 2>&1'
                    sh "docker login -u $username -p $password"
                    sh "docker push $dockerAccount/$registry:$imageTag"
                    echo "Pushed the docker image $imageTag to Docker Hub"
                }
            }
        }

//        stage('Deploy') {
//            steps {
//                withCredentials([string(credentialsId: 'heroku-token', variable: 'authToken')]) {
//                    sh 'heroku container:login'
//                    sh 'heroku git:remote -a uboat-vault'
//                    sh 'heroku container:push web'
//                    sh 'heroku container:release web'
//                    echo 'Deployed Docker container on Heroku successfully. Testing if app is running...'
//                }
//            }
//        }

//        stage('Test if Vault is running') {
//            steps {
//                script {
//                    final String response = sh(script: "curl -s $uboatUrl/api/isVaultActive", returnStdout: true).trim()
//                    echo response
//                }
//            }
//        }
    }

    post {
        always
                {
                    sh "docker rmi $imageTag"
                    sh 'docker logout'
                }
    }
}
