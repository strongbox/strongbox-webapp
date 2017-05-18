def REPO_NAME = 'strongbox/strongbox-webapp'
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
                    sh 'mvn -U clean install'
                }
            }
        }
        stage('Deploy') {
            steps {
                withMaven(maven: 'maven-3.3.9',
                          mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                          mavenLocalRepo: '/home/jenkins/.m2/repository')
                {
                    sh "mvn package "+
                       " -Dmaven.test.skip=true" +
                       " deploy:deploy" +
                       " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                }
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
