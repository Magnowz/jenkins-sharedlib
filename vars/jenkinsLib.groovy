def call(Map config = [:]) {
    pipeline {
        agent any
        parameters {
            stashedFile(config.scriptName ?: 'script.jmx')
            string(name: 'executionId', defaultValue: '', description: 'ID de execução')
        }
        stages {
            stage('Executar Teste e Enviar Métricas') {
                steps {
                    script {
                        // Configurar nome da build
                        currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.executionId}"
                        
                        // Recuperar o arquivo JMX stashed
                        unstash config.scriptName ?: 'script.jmx'
                        
                        try {
                            // Executar o teste JMeter
                            sh "jmeter -n -t script.jmx -l report${env.BUILD_NUMBER}.jtl -e -o report${env.BUILD_NUMBER}"
                            
                            // Se houver comando adicional, executa-o
                            if (config.command) {
                                sh config.command
                            }
                            
                            // Ler o arquivo JSON de métricas gerado pelo JMeter
                            def stats = readJSON file: "report${env.BUILD_NUMBER}/statistics.json"
                            
                            // Extrair apenas as métricas do objeto 'Total'
                            def totalPayload = groovy.json.JsonOutput.toJson(stats.Total)
                            
                            // Definir a URL da API (usa valor de env.API_URL ou valor padrão)
                            def apiUrl = env.API_URL ?: "http://host.docker.internal:3000"
                            
                            // Enviar as métricas para a API
                            sh """
                                curl -X POST "${apiUrl}/test-executions/${params.executionId}/results" \\
                                -H "Content-Type: application/json" \\
                                -d '${totalPayload}'
                            """
                            
                            echo "Teste concluído com sucesso e resultados enviados."
                        } catch (Exception e) {
                            // Em caso de erro, envia status FAILED com payload fixo
                            echo "Teste falhou: ${e.message}"
                            throw e
                        }
                    }
                }
            }
        }
        post {
            failure {
                script {
                    echo "Teste falhou."
                    // Status já foi atualizado no catch.
                }
            }
            aborted {
                script {
                    echo "Teste foi abortado."
                }
            }
        }
    }
}
