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

        stage('Analizando proyecto') {
            steps {
                sh 'chmod +x mvnw'
                sh 'java -version'
                sh './mvnw --version'
            }
        }

        stage('Contruyendo') {
            steps {
                sh './mvnw clean verify'
            }
        }

        stage('Analizando codigo') {
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

        stage('Analisis de codigo ok') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Construyendo imagen Docker') {
            steps {
                sh '''

                     \
                      -t ms-demo-jenkins:${BUILD_NUMBER} \
                      -t ms-demo-jenkins:latest \
                      .
                '''
            }
        }
        stage('Desplegando a DEV') {
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
            echo 'Pipeline completado: build, pruebas y análisis aprobados.'
        }

        failure {
            echo 'El pipeline falló. Revisa las pruebas o el Quality Gate.'
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