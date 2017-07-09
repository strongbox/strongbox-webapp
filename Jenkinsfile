@Library('jenkins-shared-libraries')

def SERVER_ID = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://dev.carlspring.org/nexus/content/repositories/carlspring-oss-snapshots/'

pipeline {
    agent { label 'opensuse-slave' }
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'maven-3.3.9',
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                          mavenLocalRepo: '/home/jenkins/.m2/repository')
                {
                    sh 'mvn -U clean install -Dprepare.revision'
                }
            }
        }
        stage('Deploying to Nexus') {
            steps {
                script {
                    if(BRANCH_NAME == 'master') {
                        withMaven(maven: 'maven-3.3.9',
                                  mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                                  mavenLocalRepo: '/home/jenkins/.m2/repository')
                        {
                            sh "mvn package deploy:deploy" +
                               " -Dmaven.test.skip=true" +
                               " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                        }
                    }
                    else
                    {
                        echo "Deploying to Nexus is skipped for PRs and branches other than master."
                    }
                }
            }
        }
        stage('Deploying to GitHub') {
            steps {
                script {
                    if(BRANCH_NAME == 'master') {
                        withMaven(maven: 'maven-3.3.9',
                                  mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                                  mavenLocalRepo: '/home/jenkins/.m2/repository')
                        {
                            sh "mvn package -Pdeploy-release-artifact-to-github -Dmaven.test.skip=true"
                        }
                    }
                    else
                    {
                        echo "Deploying to GitHub is skipped for PRs and branches other than master."
                    }
                }
            }
        }
    }
    post {
        changed {
            script {
                if(BRANCH_NAME == 'master') {
                    def skype = new org.carlspring.jenkins.notification.skype.Skype()
                    skype.sendNotification("admins;devs");
                }
            }
        }
        always {
            deleteDir()
        }
    }
}