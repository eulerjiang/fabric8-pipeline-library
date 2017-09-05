#!/usr/bin/groovy

import io.fabric8.Utils

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [version: '']
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    container('python') {
        
        try {
          //sh "yum install wget -y && wget https://bootstrap.pypa.io/get-pip.py && python get-pip.py"
          def utils = new Utils()
          def ns = utils.environmentNamespace('staging')
          sh "cd test/restapi && python test_restapi.py ${SERVICE_NAME}.${ns} 80 http `cat /home/jenkins/.arts01/username` `cat /home/jenkins/.arts01/password` "
        } catch (err){
            echo " test failed, please check the log"
            return false
        }
        return true 
    }
}

