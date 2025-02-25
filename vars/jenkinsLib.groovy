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
                       currentBuild.displayName  = "#${env.BUILD_NUMBER} teste-api 1.0.1 - 10000"
                        unstash config.scriptName ?: 'script.jmx'
                        //sh 'sleep 60'
                        //sh 'cat script.jmx'   
                        sh 'jmeter -n -t script.jmx'
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
            always {
                echo "teste"
            }
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
