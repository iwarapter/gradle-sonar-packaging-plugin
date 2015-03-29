package com.iadams.gradle.plugins

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

import java.util.jar.JarFile

/**
 * Created by iwarapter
 */
class SonarPackagingIntegSpec extends IntegrationSpec {

    void manifestContains(String buildJar, String key, String value){
        JarFile jarFile = new JarFile(file(buildJar).absolutePath)
        assert jarFile.manifest.mainAttributes.getValue(key) == value
    }

    def "applying plugins provides all tasks"() {
        setup:
        writeHelloWorld('com.example')
        copyResources('build.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksSuccessfully('tasks')

        then:
        result.standardOutput.contains('localDeploy - Copies the built plugin to the local server.')
        result.standardOutput.contains('restartServer - Restarts a SonarQube server running in dev mode.')
    }

    def "we can deploy the plugin to a directory"() {
        setup:
        writeHelloWorld('com.example')
        copyResources('build.gradle', 'build.gradle')
        directory('build/myServer')
        settingsFile << '''rootProject.name="example"'''
        fork = true

        when:
        runTasksSuccessfully('build')
        runTasksSuccessfully('localDeploy')

        then:
        fileExists('build/myServer/example-1.0.jar')
    }

    def "deploy plugin shows up-to-date if no change"() {
        setup:
        writeHelloWorld('com.example')
        copyResources('build.gradle', 'build.gradle')
        directory('build/myServer')
        settingsFile << '''rootProject.name="example"'''
        fork = true

        when:
        runTasksSuccessfully('build')
        runTasksSuccessfully('localDeploy')

        then:
        fileExists('build/myServer/example-1.0.jar')

        when:
        ExecutionResult result = runTasksSuccessfully('localDeploy')

        then:
        result.wasUpToDate(':localDeploy')
    }

    def 'setup example build'() {
        setup:
        writeHelloWorld('com.example')
        copyResources('build.gradle', 'build.gradle')
        settingsFile << '''rootProject.name="example"'''
        fork = true

        expect:
        runTasksSuccessfully('build')
        manifestContains('build/libs/example-1.0.jar', 'Plugin-Description','An Example Plugin!')
    }

    def 'setup multi-project build'() {
        setup:
        def sub1 = addSubproject('example-squid')
        def sub2 = addSubproject('sonar-example-plugin')
        fork = true

        writeHelloWorld('com.example', sub1)
        writeHelloWorld('com.example', sub2)
        copyResources('build.gradle', 'sonar-example-plugin/build.gradle')

        expect:
        runTasksSuccessfully('build')
        manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Description','An Example Plugin!')
    }
}
