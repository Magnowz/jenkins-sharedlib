def call(Map config = [:]) {
    def apiUrl = env.API_URL ?: "https://jmeter-api-production-661f.up.railway.app" 
    pipeline {
        agent any
        parameters {
            stashedFile(config.scriptName ?: 'script.jmx')
            string(name: 'executionId', defaultValue: '', description: 'ID de execução')
        }
        stages {
            stage('Atualizando status da execução') {
                steps {
                    script{
                        currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.executionId}"
                        sh """
                                curl -X PATCH "${apiUrl}/test-executions/${params.executionId}/status" \\
                                -H "Content-Type: application/json" \\
                                -d '{ "status": "EXECUTANDO" }'
                         """
                    }
                }
            }
            stage('Executar Teste e Enviar Métricas') {
                steps {
                    script {
                        currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.executionId}"
                        unstash config.scriptName ?: 'script.jmx'
                        try {
                            sh "jmeter -n -t script.jmx -l report${env.BUILD_NUMBER}.jtl -e -o report${env.BUILD_NUMBER}"
                            if (config.command) {
                                sh config.command
                            }
                            def stats = readJSON file: "report${env.BUILD_NUMBER}/statistics.json"
                            def totalPayload = groovy.json.JsonOutput.toJson(stats.Total)
                            //def apiUrl = env.API_URL ?: "http://host.docker.internal:3000"
                            //def apiUrl = env.API_URL ?: "https://jmeter-api-production.up.railway.app"
                            sh """
                                curl -X POST "${apiUrl}/test-executions/${params.executionId}/results" \\
                                -H "Content-Type: application/json" \\
                                -d '${totalPayload}'
                            """
                            
                            echo "Teste concluído com sucesso e resultados enviados."
                        } catch (Exception e) {
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
