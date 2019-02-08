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

def docker_image = 'docker:18.06.0-ce-dind'
def build_image = 'pegasyseng/pantheon-build:0.0.5-jdk11'

try {
    parallel UnitTests: {
        def stage_name = "Unit tests stage: "
        node {
            checkout scm
            docker.image(docker_image).withRun('--privileged') { d ->
                docker.image(build_image).inside("--link ${d.id}:docker") {
                    try {
                        stage(stage_name + 'Compile') {
                            sh './gradlew --no-daemon --parallel clean compileJava'
                        }
                        stage(stage_name + 'compile tests') {
                            sh './gradlew --no-daemon --parallel compileTestJava'
                        }
                        stage(stage_name + 'assemble') {
                            sh './gradlew --no-daemon --parallel assemble'
                        }
                        stage(stage_name + 'Unit tests') {
                            sh './gradlew --no-daemon --parallel build'
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
    }, ReferenceTests: {
        def stage_name = "Reference tests stage: "
        node {
            checkout scm
            docker.image(docker_image).withRun('--privileged') { d ->
                docker.image(build_image).inside("--link ${d.id}:docker") {
                    try {
                        stage(stage_name + 'Compile') {
                            sh './gradlew --no-daemon --parallel clean compileJava'
                        }
                        stage(stage_name + 'compile tests') {
                            sh './gradlew --no-daemon --parallel compileTestJava'
                        }
                        stage(stage_name + 'assemble') {
                            sh './gradlew --no-daemon --parallel assemble'
                        }
                        stage(stage_name + 'Reference tests') {
                            sh './gradlew --no-daemon --parallel referenceTest'
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
    }, IntegrationTests: {
        def stage_name = "Integration tests stage: "
        node {
            checkout scm
            docker.image(docker_image).withRun('--privileged') { d ->
                docker.image(build_image).inside("--link ${d.id}:docker") {
                    try {
                        stage(stage_name + 'Compile') {
                            sh './gradlew --no-daemon --parallel clean compileJava'
                        }
                        stage(stage_name + 'compile tests') {
                            sh './gradlew --no-daemon --parallel compileTestJava'
                        }
                        stage(stage_name + 'assemble') {
                            sh './gradlew --no-daemon --parallel assemble'
                        }
                        stage(stage_name + 'Integration Tests') {
                            sh './gradlew --no-daemon --parallel integrationTest'
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
    }, AcceptanceTests: {
        def stage_name = "Acceptance tests stage: "
        node {
            checkout scm
            docker.image(docker_image).withRun('--privileged') { d ->
                docker.image(build_image).inside("--link ${d.id}:docker") {
                    try {
                        stage(stage_name + 'Compile') {
                            sh './gradlew --no-daemon --parallel clean compileJava'
                        }
                        stage(stage_name + 'compile tests') {
                            sh './gradlew --no-daemon --parallel compileTestJava'
                        }
                        stage(stage_name + 'assemble') {
                            sh './gradlew --no-daemon --parallel assemble'
                        }
                        stage(stage_name + 'Acceptance Tests') {
                            sh './gradlew --no-daemon --parallel acceptanceTest'
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
    }, OtherChecks: {
        def stage_name = "Other checks stage: "
        node {
            checkout scm
            docker.image(docker_image).withRun('--privileged') { d ->
                docker.image(build_image).inside("--link ${d.id}:docker") {
                    try {
                        stage(stage_name + 'Compile') {
                            sh './gradlew --no-daemon --parallel clean compileJava'
                        }
                        stage(stage_name + 'compile tests') {
                            sh './gradlew --no-daemon --parallel compileTestJava'
                        }
                        stage(stage_name + 'assemble') {
                            sh './gradlew --no-daemon --parallel assemble'
                        }
                        stage(stage_name + 'Check Licenses') {
                            sh './gradlew --no-daemon --parallel checkLicenses'
                        }
                        stage(stage_name + 'Check javadoc') {
                            sh './gradlew --no-daemon --parallel javadoc'
                        }
                    } finally {
                        archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/reports/**'
                        archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/test-results/**'
                        archiveArtifacts allowEmptyArchive: true, artifacts: 'build/reports/**'
                        archiveArtifacts allowEmptyArchive: true, artifacts: 'build/distributions/**'

                        junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'
                    }
                }
            }
        }
    }
} catch (e) {
    currentBuild.result = 'FAILURE'
} finally {
    // If we're on master and it failed, notify slack
    if (env.BRANCH_NAME == "master") {
        def currentResult = currentBuild.result ?: 'SUCCESS'
        def channel = '#priv-pegasys-prod-dev'
        if (currentResult == 'SUCCESS') {
            def previousResult = currentBuild.previousBuild?.result
            if (previousResult != null && (previousResult == 'FAILURE' || previousResult == 'UNSTABLE')) {
                slackSend(
                    color: 'good',
                    message: "Pantheon branch ${env.BRANCH_NAME} build is back to HEALTHY.\nBuild Number: #${env.BUILD_NUMBER}\n${env.BUILD_URL}",
                    channel: channel
                )
            }
        } else if (currentBuild.result == 'FAILURE') {
            slackSend(
                color: 'danger',
                message: "Pantheon branch ${env.BRANCH_NAME} build is FAILING.\nBuild Number: #${env.BUILD_NUMBER}\n${env.BUILD_URL}",
                channel: channel
            )
        } else if (currentBuild.result == 'UNSTABLE') {
            slackSend(
                color: 'warning',
                message: "Pantheon branch ${env.BRANCH_NAME} build is UNSTABLE.\nBuild Number: #${env.BUILD_NUMBER}\n${env.BUILD_URL}",
                channel: channel
            )
        }
    }
}
