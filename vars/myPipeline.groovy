def call(Map config = [:]) {
    pipeline {
        agent any
        parameters {
            // Define um parâmetro para carregar o arquivo JMX stashed
            stashedFile(config.scriptName ?: 'script.jmx')
        }
        stages {
            stage('Ler jmx') {
                steps {
                    script {
                        // Descompacta o arquivo stashed
                        unstash config.scriptName ?: 'script.jmx'
                        sh 'cat script.jmx'    
                        // Executa algum comando (exemplo: sleep)
                        if (config.command) {
                            sh config.command
                        } else {
                            echo "Nenhum comando específico foi configurado."
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
