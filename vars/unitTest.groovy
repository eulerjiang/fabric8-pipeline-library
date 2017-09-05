#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [version: '']
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    container('postgres') {
        
        try {
            echo "Unit test is starting"
            //sh "yum install wget -y && wget https://bootstrap.pypa.io/get-pip.py && python get-pip.py"
           //  echo "$SHELL"
             sh "/docker-entrypoint.sh postgres & sleep 10"
          sh "mkdir -p lib/flyway/jars && cd sql && psql -U postgres -d postgres --file=baseline/baseline.sql && ../lib/flyway/flyway -configFile=../conf/flyway.local.conf -locations=filesystem:flyway/ baseline && ../lib/flyway/flyway -configFile=../conf/flyway.local.conf -locations=filesystem:flyway/ migrate && psql -U postgres -d yodadb --file=../t/data/harness.sql && psql -U postgres -d yodadb --file=../t/data/test_data.sql"
        } catch (err){
            echo " test failed, please check the log"
            return false
        }
    }
    
    container('golang') {

        try {
        sh " export GOPATH=`pwd` &&mkdir -p src/rest/cfg/keys && cd src/rest/cfg/keys && openssl genrsa -out app.rsa 2048 && openssl rsa -in app.rsa -pubout > app.rsa.pub && go test rest -v -signum `cat /home/jenkins/.arts01/username` -password `cat /home/jenkins/.arts01/password`"
        } catch (err){
            echo " test failed, please check the log"
            return false
        }
    }
   return true
}

