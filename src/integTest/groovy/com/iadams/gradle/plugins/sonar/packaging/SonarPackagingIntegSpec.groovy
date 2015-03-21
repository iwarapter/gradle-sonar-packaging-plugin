package com.iadams.gradle.plugins.sonar.packaging

import nebula.test.IntegrationSpec

/**
 * Created by iwarapter
 */
class SonarPackagingIntegSpec extends IntegrationSpec {

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
