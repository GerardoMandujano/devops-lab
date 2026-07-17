pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Iniciando') {
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

        stage('Construyendo') {
            steps {
                sh './mvnw clean verify'
            }
        }

        stage('Analisis SonarQube') {
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

        stage('Validando rating') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Construyendo imagen Docker') {
            steps {
                sh '''
                    docker build \
                      -t ms-demo-jenkins:${BUILD_NUMBER} \
                      -t ms-demo-jenkins:latest \
                      .
                '''
            }
        }
        stage('Aprobación para desplegar') {
             steps {
                    timeout(time: 15, unit: 'MINUTES') {
                        input(
                            message: "¿Desplegar ms-demo-jenkins:${BUILD_NUMBER} a DEV?",
                            ok: 'Desplegar',
                            submitter: 'gerardo,admin'
                        )
                    }
                }
        }
        stage('Desplegando a Kubernetes Dev') {
            steps {
                sh '''
                    set -e

                    IMAGE="ms-demo-jenkins:${BUILD_NUMBER}"
                    TAR_FILE="/tmp/ms-demo-jenkins-${BUILD_NUMBER}.tar"

                    echo "Desplegando ${IMAGE}"

                    docker image inspect "${IMAGE}" > /dev/null

                    docker save -o "${TAR_FILE}" "${IMAGE}"

                    docker cp "${TAR_FILE}" \
                        minikube:"${TAR_FILE}"

                    docker exec minikube ctr \
                        --namespace=k8s.io \
                        images import "${TAR_FILE}"

                    docker exec minikube ctr \
                        --namespace=k8s.io \
                        images list | grep "ms-demo-jenkins:${BUILD_NUMBER}"

                    kubectl apply -f k8s/

                    kubectl set image deployment/ms-demo \
                        ms-demo="${IMAGE}" \
                        -n dev

                    kubectl rollout status deployment/ms-demo \
                        -n dev \
                        --timeout=180s

                    kubectl get pods -n dev

                    rm -f "${TAR_FILE}"
                    docker exec minikube rm -f "${TAR_FILE}"
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