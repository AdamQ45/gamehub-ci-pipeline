pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        SONAR_URL = 'http://localhost:9000'
        DOCKER_HOST_IP = '172.31.39.71'
    }

    stages {
        stage('Cleanup') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/AdamQ45/gamehub-ci-pipeline.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -Dtest="*Test" -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -Dsonar.host.url=${SONAR_URL}'
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

        stage('Deploy - Copy Artifacts') {
            steps {
                sshPublisher(publishers: [
                    sshPublisherDesc(
                        configName: 'ansible-server',
                        transfers: [
                            sshTransfer(
                                sourceFiles: 'target/game-service-0.0.1-SNAPSHOT.jar',
                                removePrefix: 'target',
                                remoteDirectory: '/opt/docker'
                            ),
                            sshTransfer(
                                sourceFiles: 'Dockerfile',
                                remoteDirectory: '/opt/docker'
                            )
                        ]
                    )
                ])
            }
        }

        stage('Deploy - Ansible') {
            steps {
                sh 'ansible-playbook ansible/deploy-game-service.yml -i ansible/hosts'
            }
        }
    }

    post {
        failure {
            echo 'Pipeline FAILED - check the logs above for details'
        }
        success {
            echo 'Pipeline completed successfully - game-service deployed!'
        }
    }
}
