#!/usr/bin/groovy

def call(Map parameters = [:], body) {
    def defaultLabel = "maven.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
    def label = parameters.get('label', defaultLabel)

    if ("${env.DIND_FT_ENABLED}" == "true") {
        println "Using dindTemplate with docker"
        dindTemplate(parameters) {
            node(label) {
                body()
            }
        }
    }
    else {
        println "Using mavenTemplate without docker"
        mavenTemplate(parameters) {
            node(label) {
                body()
            }
        }
    }
}

