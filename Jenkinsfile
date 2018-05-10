@Library('jenkins-shared-libraries')

def SERVER_ID = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://dev.carlspring.org/nexus/content/repositories/carlspring-oss-snapshots/'

def workspaceUtils = new org.carlspring.jenkins.workspace.WorkspaceUtils();

pipeline {
    agent {
        node {
            label 'alpine:jdk8-mvn-3.5'
            customWorkspace workspaceUtils.generateUniqueWorkspacePath()
        }
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
    }
    stages {
        stage('Node')
        {
            steps {
                sh "cat /etc/node"
                sh "cat /etc/os-release"
                sh "mvn --version"
            }
        }
        stage('Building...')
        {
            steps {
                withMaven(mavenLocalRepo: workspaceUtils.generateM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    withEnv(['PATH+MVN_CMD=$MVN_CMD']) {
                        timestamps {
                            sh "mvn -U clean install -Dprepare.revision -Dmaven.test.failure.ignore=true"
                        }
                    }
                }
            }
        }
        stage('Deploying to Nexus') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                withMaven(mavenLocalRepo: workspaceUtils.generateM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    withEnv(['PATH+MVN_CMD=$MVN_CMD']) {
                        sh "mvn package deploy:deploy" +
                           " -Dmaven.test.skip=true" +
                           " -DaltDeploymentRepository=${SERVER_ID}::default::${SERVER_URL}"
                    }
                }
            }
        }
        stage('Deploying to GitHub') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                withMaven(mavenLocalRepo: workspaceUtils.generateM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
                {
                    withEnv(['PATH+MVN_CMD=$MVN_CMD']) {
                        sh "mvn package -Pdeploy-release-artifact-to-github -Dmaven.test.skip=true"
                    }
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
        cleanup {
            // Cleanup workspace.
            cleanWs deleteDirs: true, externalDelete: 'rm -rf %s', notFailBuild: true
        }
    }
}
