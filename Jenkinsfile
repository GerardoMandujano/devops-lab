pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Preparar entorno') {
            steps {
                sh '''
                    chmod +x mvnw
                    java -version
                    ./mvnw --version
                    docker --version
                '''
            }
        }

        stage('Build and Test') {
            steps {
                sh './mvnw clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                        ./mvnw sonar:sonar \
                          -Dsonar.projectKey=ms-demo-jenkins \
                          -Dsonar.projectName=ms-demo-jenkins
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build \
                      -t ms-demo-jenkins:${BUILD_NUMBER} \
                      -t ms-demo-jenkins:latest \
                      .
                '''
            }
        }

        stage('Deploy DEV') {
            steps {
                sh '''
                    docker rm -f ms-demo-jenkins-dev 2>/dev/null || true

                    docker run -d \
                      --name ms-demo-jenkins-dev \
                      --restart unless-stopped \
                      -p 8081:8080 \
                      ms-demo-jenkins:${BUILD_NUMBER}
                '''
            }
        }
    }

    post {
        success {
            echo """
                Pipeline completado correctamente.

                Imagen generada:
                ms-demo-jenkins:${BUILD_NUMBER}

                Contenedor desplegado:
                ms-demo-jenkins-dev

                Aplicación:
                http://localhost:8081
            """
        }

        failure {
            echo 'El pipeline falló. Revisa la etapa marcada en rojo.'
        }

        always {
            junit(
                allowEmptyResults: true,
                testResults: '**/target/surefire-reports/*.xml'
            )

            archiveArtifacts(
                allowEmptyArchive: true,
                artifacts: '**/target/*.jar',
                fingerprint: true
            )
        }
    }
}