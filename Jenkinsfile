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

        stage('Environment') {
            steps {
                sh 'java -version'
                sh './mvnw --version'
            }
        }

        stage('Compile') {
            steps {
                sh './mvnw clean compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -DskipTests'
            }
        }
    }

    post {
        success {
            echo 'La aplicación se compiló correctamente.'
        }

        failure {
            echo 'El pipeline falló. Revisa la etapa marcada en rojo.'
        }

        always {
            junit allowEmptyResults: true,
                  testResults: '**/target/surefire-reports/*.xml'

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: '**/target/*.jar',
                             fingerprint: true
        }
    }
}