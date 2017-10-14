#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    def defaultLabel = "maven.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
    def label = parameters.get('label', defaultLabel)

    def mavenImage = parameters.get('mavenImage', 'fabric8/maven-builder:2.2.297')
    def clientsImage = parameters.get('clientsImage', 'fabric8/builder-clients:0.1')
    def inheritFrom = parameters.get('inheritFrom', 'base')

    def flow = new io.fabric8.Fabric8Commands()

    if (flow.isOpenShift()) {
        podTemplate(label: label, inheritFrom: "${inheritFrom}",
                containers: [
                        [name: 'maven', image: "${mavenImage}", command: 'cat', ttyEnabled: true,
                         envVars: [
                                 [key: 'MAVEN_OPTS', value: '-Duser.home=/root/']]],
                        [name: 'clients', image: "${clientsImage}", command: 'cat', ttyEnabled: true]],
                volumes: [secretVolume(secretName: 'jenkins-maven-settings', mountPath: '/root/.m2'),
                          persistentVolumeClaim(claimName: 'jenkins-mvn-local-repo', mountPath: '/root/.mvnrepo'),
                          secretVolume(secretName: 'jenkins-release-gpg', mountPath: '/home/jenkins/.gnupg'),
                          secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                          secretVolume(secretName: 'jenkins-ssh-config', mountPath: '/root/.ssh'),
                          secretVolume(secretName: 'jenkins-git-ssh', mountPath: '/root/.ssh-git')]) {

            body(

            )
        }
    } else {
        podTemplate(label: label, inheritFrom: "${inheritFrom}",
                containers: [
                        [name: 'maven', image: "${mavenImage}", command: 'cat', ttyEnabled: true,
                            envVars: [
                                    [key: 'MAVEN_OPTS', value: '-Duser.home=/root/']
                                  ]
                        ],
                        [name: 'docker-cmds', image: 'docker:1.12.6', command: 'cat', ttyEnabled: true,
                            envVars: [[key: 'DOCKER_HOST', value: 'tcp://localhost:2375']]
                        ],
                        [name: 'dind-daemon', image: 'docker:1.12.6-dind', ttyEnabled: true, privileged: true,
                            envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]
                        ]],

                volumes: [secretVolume(secretName: 'jenkins-maven-settings', mountPath: '/root/.m2'),
                          persistentVolumeClaim(claimName: 'jenkins-mvn-local-repo', mountPath: '/root/.mvnrepo'),
                          secretVolume(secretName: 'jenkins-docker-cfg', mountPath: '/home/jenkins/.docker'),
                          secretVolume(secretName: 'jenkins-release-gpg', mountPath: '/home/jenkins/.gnupg'),
                          secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                          secretVolume(secretName: 'jenkins-ssh-config', mountPath: '/root/.ssh'),
                          secretVolume(secretName: 'jenkins-git-ssh', mountPath: '/root/.ssh-git'),
                          emptyDirVolume(mountPath: '/var/lib/docker', memory: false)
                ],
                envVars: [[key: 'DOCKER_HOST', value: 'tcp://localhost:2375'], [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]) {
            body(

            )
        }
    }
}

