package com.iadams.gradle.plugins

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Ignore

/**
 * Created by iwarapter
 */
class DependencyCheckIntegSpec extends IntegrationSpec{

    def setup(){
        copyResources('org/sonar/plugins/example/SamplePlugin.java', 'src/main/java/org/sonar/plugins/example/SamplePlugin.java')
    }

    def "build without api in compile fails"(){
        setup:
        copyResources('build-without-api.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksWithFailure(SonarPackagingPlugin.SONAR_CHECK_DEPENDENCIES_TASK)

        then:
        result.standardError.contains("org.codehaus.sonar:sonar-plugin-api should be declared in dependencies")
    }

    def "build with api passes"(){
        setup:
        //forked for dependency resolution.
        //fork = true
        copyResources('build-with-api.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully(SonarPackagingPlugin.SONAR_CHECK_DEPENDENCIES_TASK)
    }

    def "build with logging deps logs warnings"(){
        setup:
        //forked for dependency resolution.
        //fork = true
        copyResources('build-with-logging-deps.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksSuccessfully(SonarPackagingPlugin.SONAR_CHECK_DEPENDENCIES_TASK)

        then:
        result.standardOutput.contains("The following dependencies should be defined with scope 'provided': [log4j]")
        result.standardOutput.contains("The following dependencies should be defined with scope 'provided': [gwt-user]")
    }
}
