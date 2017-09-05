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
            sh "pip install robotframework"
          
          	def utils = new Utils()
            def ns = utils.environmentNamespace('staging')
            sh "pybot --variable USER:`cat /home/jenkins/.arts01/username` --variable PASSWORD:`cat /home/jenkins/.arts01/password` --variable HOST:${SERVICE_NAME}.${ns} --variable PORT:80 --variable PROTOCOL:http test/restapi/robotrest_restapi.txt" 
            
            step([$class: 'RobotPublisher',
                  disableArchiveOutput: false,
                  logFileName: 'log.html',
                  onlyCritical: true,
                  otherFiles: '',
                  outputFileName: 'output.xml',
                  outputPath: '.',
				  passThreshold: 90,
				  reportFileName: 'report.html',
				  unstableThreshold: 100]);
        } catch (err){
            echo " test failed, please check the log"
            return false
        }
        return true 
    }
}

