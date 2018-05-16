@Library('jenkins-shared-libraries')

def SERVER_ID = 'carlspring-oss-snapshots'
def SERVER_URL = 'https://dev.carlspring.org/nexus/content/repositories/carlspring-oss-snapshots/'

// Notification settings for "master" and "branch/pr"
def notifyMaster = [notifyAdmins: true, recipients: [culprits(), requestor()]]
def notifyBranch = [recipients: [brokenTestsSuspects(), requestor()]]

pipeline {
    agent {
        node {
            label 'alpine:jdk8-mvn-3.5'
            customWorkspace workspace().getUniqueWorkspacePath()
        }
    }
    parameters {
        booleanParam(defaultValue: true, description: 'Send email notification?', name: 'NOTIFY_EMAIL')
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
    }
    stages {
        stage('Node')
        {
            steps {
                nodeInfo("mvn")
            }
        }
        stage('Building...')
        {
            steps {
                withMavenPlus(mavenLocalRepo: workspace().getM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833', timestamps: true)
                {
                    sh "mvn -U clean install -Dprepare.revision -Dmaven.test.failure.ignore=true"
                }
            }
        }
        stage('Deploying to Nexus') {
            when {
                expression { BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS') }
            }
            steps {
                withMavenPlus(mavenLocalRepo: workspace().getM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
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
                withMavenPlus(mavenLocalRepo: workspace().getM2LocalRepoPath(), mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833')
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
        failure {
            script {
                if(params.NOTIFY_EMAIL) {
                    notifyFailed((BRANCH_NAME == "master") ? notifyMaster : notifyBranch)
                }
            }
        }
        unstable {
            script {
                if(params.NOTIFY_EMAIL) {
                    notifyUnstable((BRANCH_NAME == "master") ? notifyMaster : notifyBranch)
                }
            }
        }
        fixed {
            script {
                if(params.NOTIFY_EMAIL) {
                    notifyFixed((BRANCH_NAME == "master") ? notifyMaster : notifyBranch)
                }
            }
        }
        cleanup {
            // Cleanup workspace.
            cleanWs deleteDirs: true, externalDelete: 'rm -rf %s', notFailBuild: true
        }
    }
}
