#!/usr/bin/env groovy

if (env.BRANCH_NAME == "master") {
    properties([
        buildDiscarder(
            logRotator(
                daysToKeepStr: '90'
            )
        )
    ])
} else {
    properties([
        buildDiscarder(
            logRotator(
                numToKeepStr: '10'
            )
        )
    ])
}


node {
    checkout scm
    docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
        docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
                stage('Docker quickstart Tests') {
                    sh 'DOCKER_HOST=tcp://docker:2375 ./gradlew --no-daemon --parallel dockerQuickstartTest'
                }
            } finally {
                archiveArtifacts '**/build/reports/**'
                archiveArtifacts '**/build/test-results/**'
                archiveArtifacts 'build/reports/**'
                archiveArtifacts 'build/distributions/**'

                junit '**/build/test-results/**/*.xml'
            }
        }
    }
}