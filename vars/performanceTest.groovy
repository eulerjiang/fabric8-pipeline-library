#!/usr/bin/groovy

import io.fabric8.Utils

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [version: '']
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    container('jmeter') {
        
        try {
          def utils = new Utils()
          def ns = utils.environmentNamespace('staging')
          sh "pwd && ls && /jmeter/bin/jmeter -Jthreads=7 -Jhost=${SERVICE_NAME}.${ns} -Jport=80 -Jprotocol=http -Jusername=`cat /home/jenkins/.arts01/username` -Jpassword=`cat /home/jenkins/.arts01/password` -n -t test/restapi/performance/jmeter_restapi.jmx -l performance.jtl "
          sh "more performance.jtl && more jmeter.log"
          //perfReport 'performance.jtl'
          performanceReport parsers: [[$class: 'JMeterParser', glob: 'performance.jtl']], relativeFailedThresholdNegative: 1.2, relativeFailedThresholdPositive: 1.89, relativeUnstableThresholdNegative: 1.8, relativeUnstableThresholdPositive: 1.5
        } catch (err){
            echo " test failed, please check the log"
            return false
        }
        return true 
    }
}

