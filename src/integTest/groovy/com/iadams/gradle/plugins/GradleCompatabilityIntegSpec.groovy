package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.SonarPackagingBaseIntegSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Unroll

/**
 * Created by iwarapter
 */
class GradleCompatabilityIntegSpec extends SonarPackagingBaseIntegSpec {

    @Unroll
    def "should use Gradle #requestedGradleVersion when requested"() {
        setup:
        def sub1 = addSubproject('example-squid')
        def sub2 = addSubproject('sonar-example-plugin')

        writeHelloWorld('com.example', sub1)
        writeHelloWorld('com.example', sub2)
        copyResources('build.gradle', 'sonar-example-plugin/build.gradle')
        fork = true
        //remoteDebug = true

        and:
        gradleVersion = requestedGradleVersion

        when:
        runTasksSuccessfully('build')

        then:
        manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar','Plugin-Description','An Example Plugin!')

        where:
        //Add future versions, not backwards compatible.
        requestedGradleVersion << ['2.3']
    }
}
