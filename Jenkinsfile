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

        stage('Analicis de codigo ok') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
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