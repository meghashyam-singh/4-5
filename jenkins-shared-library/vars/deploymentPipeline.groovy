def call(Map configMap) {
    pipeline {
        agent {
            node {
                label "AGENT-1"
            }
        }
        environment {
            REGION = "us-east-1"
            CLUSTER_NAME = "roboshop-cluster"
            GIT_URL = "${configMap.GIT_URL}"
            COMPONENT = "${configMap.COMPONENT}"
            BRANCH = "${configMap.BRANCH}"
            NAMESPACE = "roboshop"
        }
        options {
            timeout(time:15, unit: 'MINUTES')
            disableConcurrentBuilds()
        }
        stages {
            stage('checkout scm') {
                steps {
                    git url: "${GIT_URL}", branch: "${BRANCH}"
                }
            }
            stage('deploy') {
                steps {
                    dir("${COMPONENT}") {
                        sh """
                        aws eks update-kubeconfig --region ${REGION} --cluster ${CLUSTER_NAME}
                        kubectl apply -f manifestfile.yaml
                        """
                    }
                }
            }
            stage('health check') {
                steps {
                    sh "kubectl rollout status deployment ${COMPONENT} -n ${NAMESPACE}"
                }
            }
        }
    }
}