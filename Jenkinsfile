pipeline {
    agent any

    tools {
        maven 'maven-3-8.6'
        dockerTool 'docker-latest'
    }

    environment {
        gitUrl = 'https://github.com/danielmartonca/UBoatVault.git'
        VERSION = readMavenPom().getVersion()
        imageTag = "danielmartonca/uboat-vault:$VERSION"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
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
                sh 'mvn clean package -p production -DskipTests --batch-mode'
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

        stage('Build Docker Image') {
            steps {
                sh "docker build -t $imageTag ."
                echo "Built Docker Image '$imageTag'"
            }
        }

        stage('Push Image to Dockerhub') {
            steps {
                sh 'docker logout > /dev/null 2>&1'
                sh "docker login -u $DOCKERHUB_CREDENTIALS_USR -p $DOCKERHUB_CREDENTIALS_PSW"
                sh "docker push $imageTag"
                echo "Pushed the docker image $imageTag to Docker Hub"
            }
        }

        stage('Deploy') {
            steps {
                sh 'heroku container:login'
                sh 'heroku git:remote -a uboat-vault'
                sh 'heroku container:push web'
                sh 'heroku container:release web'
                echo 'This stage should deploy the docker image to Heroku'
            }
        }
    }

    post {
        always
        {
            sh "docker rmi $imageTag"
            sh 'docker logout'
        }
    }
}
