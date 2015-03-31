package com.iadams.gradle.plugins

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Unroll

import java.util.jar.JarFile

/**
 * Created by iwarapter
 */
class GradleCompatabilityIntegSpec extends IntegrationSpec {

    void manifestContains(String buildJar, String key, String value){
        JarFile jarFile = new JarFile(file(buildJar).absolutePath)
        assert jarFile.manifest.mainAttributes.getValue(key) == value
    }

    @Unroll
    def "should use Gradle #requestedGradleVersion when requested"() {
        setup:
        def sub1 = addSubproject('example-squid')
        def sub2 = addSubproject('sonar-example-plugin')

        writeHelloWorld('com.example', sub1)
        writeHelloWorld('com.example', sub2)
        copyResources('build.gradle', 'sonar-example-plugin/build.gradle')
        fork = true
        remoteDebug = true

        and:
        gradleVersion = requestedGradleVersion

        when:
        ExecutionResult result = runTasksSuccessfully('build')

        then:
        manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar','Plugin-Description','An Example Plugin!')

        where:
        //Add future versions, not backwards compatible.
        requestedGradleVersion << ['2.3']
    }
}
