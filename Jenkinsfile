pipeline {
  agent {
    node {
      label 'opensuse-slave'
    }
  }
  stages {
    stage('Build') {
      steps {
        withMaven(maven: 'maven-3.3.9',
                  mavenSettingsConfig: 'a5452263-40e5-4d71-a5aa-4fc94a0e6833',
                  mavenLocalRepo: '/tmp/.m2/repository')
        {
          sh 'mvn clean install -U -Preserve-ports,run-it-tests,!set-default-ports,deploy-release-artifact-to-github'
        }
      }
    }
  }
}