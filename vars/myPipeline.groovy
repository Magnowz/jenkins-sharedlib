def call(Map config = [:]) {
    pipeline {
        agent any
        environment {
        CUSTOM_BUILD_ID = "${env.BUILD_NUMBER}-custom" // Essa interpolação pode não funcionar como esperado aqui
        }
        parameters {
            stashedFile(config.scriptName ?: 'script.jmx')
        }
        stages {
            stage('Ler jmx') {
                steps {
                    script {
                       currentBuild.displayName  = "#${env.BUILD_NUMBER} teste-api 1.0.1 - 10000"
                        unstash config.scriptName ?: 'script.jmx'
                        sh 'cat script.jmx'    
                        if (config.command) {
                            sh config.command
                        } else {
                            echo ""
                        }
                    }
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
                echo "Executado quando o build foi abortado."
            }
        }
    }
}
