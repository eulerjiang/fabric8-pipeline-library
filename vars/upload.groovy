#!/usr/bin/groovy
import io.fabric8.Fabric8Commands
import io.fabric8.Utils

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [version: '']
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    container('clients') {
        def newVersion = config.version
        if (newVersion == '') {
            newVersion = getNewVersion {}
        }

        env.setProperty('VERSION', newVersion)
        echo " version is ${newVersion}"
        uploadArm(newVersion)

        return newVersion
    }
}

def uploadArm(version){
    def utils = new Utils()
    def flow = new Fabric8Commands()
    def namespace = utils.getNamespace()
    def newImageName = "${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}/${namespace}/${env.JOB_NAME}:${version}"
    def armImageName = "armdocker.rnd.ericsson.se/proj_adp/${env.JOB_NAME}:${version}"
    sh "cp conf/config.json ~/.docker/config.json && docker pull ${newImageName} && docker tag ${newImageName} ${armImageName} && docker push ${armImageName}"

}


