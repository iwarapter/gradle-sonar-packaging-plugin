package com.iadams.gradle.plugins

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

/**
 * Created by iwarapter
 */
class SonarPackagingIntegSpec extends IntegrationSpec {

    def "deploy plugin task is available"() {
        setup:
        writeHelloWorld('com.example')
        copyResources('build.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksSuccessfully('tasks')

        then:
        result.standardOutput.contains('localDeploy - Copies the built plugin to the local server.')
    }

    def "we can deploy the plugin to a directory"() {
        setup:
        writeHelloWorld('com.example')
        copyResources('build.gradle', 'build.gradle')
        directory('build/myServer')
        settingsFile << '''rootProject.name="example"'''

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

        expect:
        runTasksSuccessfully('build')
        file('build/tmp/jar/MANIFEST.MF').text.contains('Plugin-Description: An Example Plugin!')
    }

    def 'setup multi-project build'() {
        setup:
        def sub1 = addSubproject('example-squid')
        def sub2 = addSubproject('sonar-example-plugin')

        writeHelloWorld('com.example', sub1)
        writeHelloWorld('com.example', sub2)
        copyResources('build.gradle', 'sonar-example-plugin/build.gradle')

        expect:
        runTasksSuccessfully('build')
        file('sonar-example-plugin/build/tmp/jar/MANIFEST.MF').text.contains('Plugin-Description: An Example Plugin!')
    }
}
