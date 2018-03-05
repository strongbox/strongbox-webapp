@Library('jenkins-shared-libraries')

def SERVER_ID = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://dev.carlspring.org/nexus/content/repositories/carlspring-oss-snapshots/'

pipeline {
    agent {
        docker {
            args  '-v /mnt/ramdisk/3:/home/jenkins --privileged=true'
            image 'strongboxci/alpine:jdk8-mvn-3.5'
        }
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
        skipDefaultCheckout()
    }
    stages {
        stage('Setup workspace')
        {
            steps {
                script {
                    env.HDDWS=env.WORKSPACE
                    env.RAMWS="/home/jenkins/workspace/"+ sh(returnStdout: true, script: 'basename "${HDDWS}"').trim()
                    env.RAMMOUNT=env.WORKSPACE+"/ram"

                    cleanWs deleteDirs: true
                    checkout scm

                    echo "Preparing workspace..."
                    sh "mkdir -p '$RAMWS'"
                    // sh "cp -R `ls -A '$HDDWS' | grep -v ram` '$RAMWS'"
                    sh "find $HDDWS -maxdepth 1 ! -path 'ram' -exec cp -R {} '$RAMWS' \\;"
                    sh "mkdir -p '$RAMMOUNT'"
                    sh "sudo mount --bind  '$RAMWS' '$RAMMOUNT'"
                }
            }
        }
        stage('Building...')
        {
            steps {
                withMaven(maven: 'maven-3.3.9', mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    sh "cd '$RAMMOUNT' && mvn -U clean install -Dprepare.revision -Dmaven.test.failure.ignore=true"

                    // unmount and copy back to hdd
                    sh "sudo umount -f $RAMMOUNT"
                    sh "cp -R '$RAMWS/.' '$RAMMOUNT'"
                    sh "touch '$HDDWS/copied'"
                }
            }
        }
        stage('Deploying to Nexus') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                withMaven(maven: 'maven-3.3.9',
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    sh "mvn package deploy:deploy" +
                       " -Dmaven.test.skip=true" +
                       " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                }
            }
        }
        stage('Deploying to GitHub') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                withMaven(maven: 'maven-3.3.9',
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    sh "mvn package -Pdeploy-release-artifact-to-github -Dmaven.test.skip=true"
                }
            }
        }
    }
    post {
        success {
            script {
                if(BRANCH_NAME == 'master') {
                    build(job: "strongbox/strongbox-assembly/master", wait: false)
                }
            }
        }
        changed {
            script {
                if(BRANCH_NAME == 'master') {
                    def skype = new org.carlspring.jenkins.notification.skype.Skype()
                    skype.sendNotification("admins;devs");
                }
            }
        }
        always {
            script {
                // fallback copy
                if(!fileExists(env.HDDWS+'/copied'))
                {
                    // unmount and copy back to hdd
                    sh "sudo umount -f $RAMMOUNT"
                    sh "cp -R '$RAMWS/.' '$RAMMOUNT'"
                }
            }

            // remove unnecessary directories.
            sh "(cd '$HDDWS' && find . -maxdepth 1 ! -name 'ram' -exec rm -rf '{}' \\;)"

            // clean up ram
            sh "rm -rf '$RAMWS'"
        }
    }
}