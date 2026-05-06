def call(Map configMap) {
    pipeline {
        agent {
            node {
                label "AGENT-1"
            }
        }
        environment {
            PROJECT = "roboshop"
            REGION = "us-east-1"
            ACCOUNT_ID = "515138251473"
            APPVERSION = ""
            GIT_URL = "${configMap.GIT_URL}"
            BRANCH = "${configMap.BRANCH}"
            COMPONENT = "${configMap.COMPONENT}"
        }
        options {
            timeout(time:15, unit: 'MINUTES')
            disableConcurrentBuilds()
        }
        stages {
            stage('clean workspace') {
                steps {
                    cleanWs()
                }
            }
            stage('get code') {
                steps {
                    git url: "${GIT_URL}", branch: "${BRANCH}"
                }
            }
            stage('read version') {
                steps {
                    dir("${COMPONENT}") {
                        script {
                            def pom = readMavenPom file: 'pom.xml'
                            APPVERSION = pom.version
                            echo "APPVERSION IS: ${APPVERSION}"
                        }
                    }
                }
            }
            stage('build code') {
                steps {
                    dir("${COMPONENT}") {
                        sh "mvn clean package"
                    }
                }
            }
            stage('cqa-sonar') {
                steps {
                    dir("${COMPONENT}") {
                        script {
                            def scannerHome = tool = 'sonar-8.0'
                            withSonarQubeEnv('sonar-server') {
                                sh "${scannerHome}/bin/sonar-scanner"
                            }
                        }
                    }
                }
            }
            stage('quality gates') {
                steps {
                    script {
                        timeout(time:15, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                }
            }
            stage('image build') {
                steps {
                    sh """
                    docker build --no-cache -t ${PROJECT}/${COMPONENT}:${APPVERSION}-${BUILD_NUMBER} ./${COMPONENT}
                    docker images
                    """
                }
            }
            stage('scan-image') {
                steps {
                    sh "trivy image ${PROJECT}/${COMPONENT}:${APPVERSION}-${BUILD_NUMBER} > ${COMPONENT}-image-scan-report.txt"
                }
            }
            stage('push to ecr') {
                steps {
                    script {
                        withAWS(region:"${REGION}",credentials:'aws-creds') {
                            sh """
                            aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com
                            docker tag ${PROJECT}/${COMPONENT}:${APPVERSION}-${BUILD_NUMBER} ${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${PROJECT}/${COMPONENT}:${APPVERSION}-${BUILD_NUMBER}
                            docker push ${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${PROJECT}/${COMPONENT}:${APPVERSION}-${BUILD_NUMBER}
                            """
                        }
                    }
                }
            }
            stage('trigger cd job') {
                steps {
                    build job: "${COMPONENT}-cd-pipeline",
                    propagate: false,
                    wait: false
                }
            }
        }
    }
}