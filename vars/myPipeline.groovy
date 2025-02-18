def call(Map config = [:]) {
    pipeline {
        agent any
        parameters {
            stashedFile(config.scriptName ?: 'script.jmx')
        }
        stages {
            stage('Ler jmx') {
                steps {
                    script {
                        currentBuild.displayName  = "${env.BUILD_NUMBER}-teste-api"
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
