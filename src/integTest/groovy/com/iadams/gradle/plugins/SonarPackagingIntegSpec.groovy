package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.SonarPackagingBaseIntegSpec
import nebula.test.functional.ExecutionResult

/**
 * Created by iwarapter
 */
class SonarPackagingIntegSpec extends SonarPackagingBaseIntegSpec {

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
        def squid = addSubproject('example-squid')
        def checks = addSubproject('example-checks', '''dependencies{
                                                            compile project(':example-squid')
                                                        }''')
        addSubproject('sonar-example-plugin')
        copyResources('org/sonar/plugins/example/SamplePlugin.java', 'sonar-example-plugin/src/main/java/org/sonar/plugins/example/SamplePlugin.java')
        buildFile << '''subprojects{
                            apply plugin: 'java'
                            version = '1.0'
                        }'''

        fork = true
        remoteDebug = true

        writeHelloWorld('com.example', squid)
        writeHelloWorld('com.example', checks)
        copyResources('multi-project-build.gradle', 'sonar-example-plugin/build.gradle')

        expect:
        runTasksSuccessfully('build')
        manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Description','An Example Plugin!')
        dependencyExists('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'META-INF/lib/example-squid-1.0.jar')
        dependencyExists('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'META-INF/lib/example-checks-1.0.jar')
    }

    def "invalid pluginKey causes failure"(){
        setup:
        buildFile << """apply plugin: 'com.iadams.sonar-packaging'

                        sonarpackaging {
                            pluginKey = 'key-with.bad%characters'
                            pluginClass = ' org.sonar.plugins.example.SamplePlugin'
                            pluginDescription = 'Sample Plugin!'
                        }"""

        when:
        ExecutionResult result = runTasksWithFailure('build')

        then:
        result.standardError.contains('Plugin key is badly formatted. Please use ascii letters and digits only: ')
    }
}
