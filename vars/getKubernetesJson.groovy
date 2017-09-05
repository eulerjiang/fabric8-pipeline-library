#!/usr/bin/groovy
import io.fabric8.Utils
import io.fabric8.Fabric8Commands

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def flow = new Fabric8Commands()
    def utils = new Utils()

    def expose = config.exposeApp ?: 'true'
    def requestCPU = config.resourceRequestCPU ?: '0'
    def requestMemory = config.resourceRequestMemory ?: '0'
    def limitCPU = config.resourceLimitMemory ?: '0'
    def limitMemory = config.resourceLimitMemory ?: '0'
    def yaml

    def isSha = ''
    if (flow.isOpenShift()){
        isSha = utils.getImageStreamSha(env.JOB_NAME)
    }

    def fabric8Registry = ''
    if (env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST){
        fabric8Registry = env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST+':'+env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT+'/'
    }

    def sha
    
    // def db_yaml = readFile file: "src/rest/k8s-yaml/yodaRest_secret_db.yaml"
    // def keys_yaml = readFile file: "src/rest/k8s-yaml/yodaRest_secret_keys.yaml"
    // def deploy_yaml = readFile file: "src/rest/k8s-yaml/yodaRest_deployment_https.yaml"
    // def service_yaml = readFile file: "src/rest/k8s-yaml/yodaRest_service_https.yaml"
    // def cert_yaml = readFile file: "src/rest/k8s-yaml/yodaRest_secret_https_cert.yaml"
    // def ingress_yaml = readFile file: "src/rest/k8s-yaml/yodaRest_ingress_https.yaml"
    yaml = readFile file: "src/rest/k8s-yaml/yodaRest_list.template.yaml"
    yaml = yaml.replaceAll('YODA_STAGING_NAMESPACE',utils.environmentNamespace('staging'))
    yaml = yaml.replaceAll('STAGING_IMAGE_REPOSITORY',"${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}")
    yaml = yaml.replaceAll('YODA_STAGING_IMAGE_NAME',utils.getNamespace() + "/${env.JOB_NAME}")
    yaml = yaml.replaceAll('YODA_STAGING_IMAGE_TAG',"${env.VERSION}")
    yaml = yaml.replaceAll('YODA_RESTAPI_SERVICE_STAGING_NAME',"${SERVICE_NAME}")

    echo 'using resources:\n' + yaml
    return yaml

}
