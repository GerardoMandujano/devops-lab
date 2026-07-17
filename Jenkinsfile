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
        stage('Desplegar en Kubernetes Dev') {
            steps {
                sh '''
                    set -e

                    IMAGE="ms-demo-jenkins:${BUILD_NUMBER}"

                    echo "Desplegando ${IMAGE}"

                    echo "1. Verificando imagen construida en Docker..."
                    docker image inspect "${IMAGE}" > /dev/null

                    echo "2. Cargando imagen en el Docker interno de Minikube..."
                    docker save "${IMAGE}" | \
                        docker exec -i minikube docker load

                    echo "3. Verificando imagen dentro de Minikube..."
                    docker exec minikube docker image inspect "${IMAGE}" > /dev/null
                    docker exec minikube docker images | grep ms-demo-jenkins

                    echo "4. Aplicando manifiestos de Kubernetes..."
                    kubectl apply -f k8s/

                    echo "5. Actualizando imagen del Deployment..."
                    kubectl set image deployment/ms-demo \
                        ms-demo="${IMAGE}" \
                        -n dev

                    echo "6. Esperando a que termine el rollout..."
                    kubectl rollout status deployment/ms-demo \
                        -n dev \
                        --timeout=180s

                    echo "7. Estado final..."
                    kubectl get pods -n dev -o wide
                    kubectl get services -n dev
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