// vars/myPipeline.groovy

def call() {
    pipeline {
        agent any
        parameters {
            stashedFile 'script.jmx'
        }
        stages {
            stage('Ler jmx') {
                steps {
                    unstash 'script.jmx'
                    sh 'sleep 300'
                }
            }
        }
        post {
            success {
                echo "Executado quando o build termina com sucesso."
            }
            failure {
                echo "Executado quando o build falha."
            }
            aborted {
                echo "Abortado"
            }
        }
    }
}
