package com.iadams.gradle.plugins

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

import java.util.zip.ZipFile

/**
 * Created by iwarapter
 */
class PackageDependenciesIntegTest extends IntegrationSpec{

    def setup(){
        copyResources('org/sonar/plugins/example/SamplePlugin.java', 'src/main/java/org/sonar/plugins/example/SamplePlugin.java')
        settingsFile << '''rootProject.name="example"'''
    }

    def dependencyExists(File buildJar, String name){
        boolean result = true

        ZipFile zipFile = new ZipFile(buildJar)
        if(!zipFile.getEntry(name)){
            result = false
        }
        zipFile.close()

        return result
    }

    def "dependencies by sonar are not packaged"() {
        setup:
        //forked for dependency resolution.
        fork = true
        remoteDebug = true
        copyResources('build-with-sonar-dep.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully('build')
        dependencyExists(file('build/libs/example-1.0.jar'), "META-INF/lib/commons-lang-2.6.jar")
    }

    def "sonar plugins are not packaged"() {
        setup:
        //forked for dependency resolution.
        fork = true
        remoteDebug = true
        copyResources('build-with-sonar-dep.gradle', 'build.gradle')

        expect:
        ExecutionResult result = runTasksSuccessfully('build')
        println result.standardOutput
        println result.standardError
        dependencyExists(file('build/libs/example-1.0.jar'), "META-INF/lib/commons-lang-2.6.jar")
        !dependencyExists(file('build/libs/example-1.0.jar'), "META-INF/lib/sonar-surefire-plugin-2.4.jar")
    }
}
