package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.SonarPackagingBaseIntegSpec
import nebula.test.functional.ExecutionResult

/**
 * Created by iwarapter
 */
class PackageDependenciesIntegTest extends SonarPackagingBaseIntegSpec {

    def setup(){
        copyResources('org/sonar/plugins/sample/SamplePlugin.java', 'src/main/java/org/sonar/plugins/sample/SamplePlugin.java')
        settingsFile << '''rootProject.name="example"'''
    }

    def "sonar provided dependencies are NOT packaged"() {
        setup:
        //forked for dependency resolution.
        fork = true
        //remoteDebug = true
        copyResources('deps-to-exclude.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully('build')
        !dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/commons-lang-2.5.jar")
        !dependencyExists('build/libs/example-1.0.jar', 'META-INF/lib/sonar-plugin-api-2.4.jar')
    }

    def "sonar plugins are not packaged"() {
        setup:
        //forked for dependency resolution.
        fork = true
        //remoteDebug = true
        copyResources('build-with-sonar-plugin.gradle', 'build.gradle')

        expect:
        ExecutionResult result = runTasksSuccessfully('build')
        !dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/commons-lang-2.5.jar")
        !dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/sonar-surefire-plugin-2.4.jar")
    }

    def "package dependencies NOT provided by sonar"(){
        setup:
        //forked for dependency resolution.
        fork = true
        //remoteDebug = true
        copyResources('should-be-packaged.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully('build')
        dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/commons-email-1.2.jar")
    }

    def "provided scope dependencies should NOT be packaged"(){
        setup:
        //forked for dependency resolution.
        fork = true
        //remoteDebug = true
        copyResources('provided-should-not-be-packaged.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully('build')
        !dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/commons-email-1.2.jar")
    }

    def "package dependencies excluded from api"(){
        setup:
        //forked for dependency resolution.
        fork = true
        //remoteDebug = true
        copyResources('package-excluded-api-deps.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully('build')
        dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/plexus-utils-1.5.6.jar")
    }

    def "build without api in compile fails"(){
        setup:
        file('src/main/java/org/sonar/plugins/sample/SamplePlugin.java').delete()
        writeHelloWorld('com.example')
        copyResources('build-without-api.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksWithFailure('build')

        then:
        result.standardError.contains("org.codehaus.sonar:sonar-plugin-api should be declared in dependencies")
    }

    def "build with api in provided scope passes"(){
        given:
        copyResources('api-in-provided-scope.gradle', 'build.gradle')
        fork = true

        expect:
        runTasksSuccessfully('build')
    }

    def "build with api passes"(){
        given:
        copyResources('build-with-api.gradle', 'build.gradle')
        fork = true

        expect:
        runTasksSuccessfully('build')
    }

    def "build with logging deps logs warnings"(){
        setup:
        copyResources('build-with-logging-deps.gradle', 'build.gradle')
        fork = true

        when:
        ExecutionResult result = runTasksSuccessfully('build')

        then:
        result.standardOutput.contains("The following dependencies should be defined with scope 'provided': [log4j]")
        result.standardOutput.contains("The following dependencies should be defined with scope 'provided': [gwt-user]")
    }

    def "build with parent and plugin dependencies fails"(){
        setup:
        copyResources('require-plugin-and-parent.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksWithFailure('build')

        then:
        result.standardError.contains("The plugin 'example' can't be both part of the plugin 'java' and having a dependency on 'scm:1.0-SNAPSHOT'")
    }

    def "cannot define self as parent"(){
        setup:
        copyResources('self-parent.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksWithFailure('build')

        then:
        result.standardError.contains("The plugin 'example' can't be his own parent. Please remove the 'Plugin-Parent' property.")
    }

    def "dependencies with classifiers"(){
        setup:
        copyResources('dependencies-with-classifier.gradle', 'build.gradle')
        fork = true

        when:
        runTasksSuccessfully('build')

        then:
        dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/grappa-2.0.0-beta.4-all.jar")
        manifestContains('build/libs/example-1.0.jar', 'Plugin-Dependencies','META-INF/lib/grappa-2.0.0-beta.4-all.jar META-INF/lib/asm-debug-all-5.0.3.jar META-INF/lib/jitescript-0.4.0.jar')
    }
}
