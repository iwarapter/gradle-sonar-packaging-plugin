package com.iadams.gradle.plugins.sonar.packaging

import nebula.test.IntegrationSpec
import spock.lang.Unroll

/**
 * Created by iwarapter
 */
class GradleCompatabilityIntegSpec extends IntegrationSpec {

    @Unroll
    def "should use Gradle #requestedGradleVersion when requested"() {
        setup:
        def sub1 = addSubproject('example-squid')
        def sub2 = addSubproject('sonar-example-plugin')

        writeHelloWorld('com.example', sub1)
        writeHelloWorld('com.example', sub2)
        copyResources('build.gradle', 'sonar-example-plugin/build.gradle')

        and:
        gradleVersion = requestedGradleVersion

        expect:
        runTasksSuccessfully('build')
        file('sonar-example-plugin/build/tmp/jar/MANIFEST.MF').text.contains('Plugin-Description: An Example Plugin!')

        where:
        requestedGradleVersion << ['2.0','2.1','2.2', '2.3']
    }
}
