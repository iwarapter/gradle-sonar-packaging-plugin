/*
 * Gradle Sonar Packaging Plugin
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Iain Adams
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.SonarPackagingBaseIntegSpec
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class SonarPackagingIntegSpec extends SonarPackagingBaseIntegSpec {

  def "applying plugins provides all tasks"() {
    setup:
    writeHelloWorld('com.example')
    copyResources('build.gradle', 'build.gradle')

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('tasks')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":tasks").outcome == SUCCESS
    result.output.contains('localDeploy - Copies the built plugin to the local server.')
    result.output.contains('restartServer - Restarts a SonarQube server running in dev mode.')
  }

  def "we can deploy the plugin to a directory"() {
    setup:
    writeHelloWorld('com.example')
    copyResources('build.gradle', 'build.gradle')
    directory('build/myServer')
    settingsFile << '''rootProject.name="example"'''

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build', 'localDeploy')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":build").outcome == SUCCESS
    file('build/myServer/example-1.0.jar').exists()
  }


  def "deploy plugin shows up-to-date if no change"() {
    setup:
    writeHelloWorld('com.example')
    copyResources('build.gradle', 'build.gradle')
    directory('build/myServer')
    settingsFile << '''rootProject.name="example"'''

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build', 'localDeploy')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":build").outcome == SUCCESS
    file('build/myServer/example-1.0.jar').exists()

    when:
    result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('localDeploy')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':localDeploy').outcome == UP_TO_DATE
  }

  def 'setup example build'() {
    setup:
    writeHelloWorld('com.example')
    copyResources('build.gradle', 'build.gradle')
    settingsFile << '''rootProject.name="example"'''

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":build").outcome == SUCCESS
    manifestContains('build/libs/example-1.0.jar', 'Plugin-Description', 'An Example Plugin!')
  }

  def 'setup multi-project build'() {
    setup:
    def squid = addSubproject('example-squid')
    def checks = addSubproject('example-checks', '''dependencies{
                                                            compile project(':example-squid')
                                                        }''')
    addSubproject('sonar-example-plugin')
    copyResources('org/sonar/plugins/sample/SamplePlugin.java', 'sonar-example-plugin/src/main/java/org/sonar/plugins/sample/SamplePlugin.java')
    buildFile << '''subprojects{
                            apply plugin: 'java'
                            version = '1.0'
                        }'''

    writeHelloWorld('com.example', squid.name)
    writeHelloWorld('com.example', checks.name)
    copyResources('multi-project-build.gradle', 'sonar-example-plugin/build.gradle')

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":sonar-example-plugin:build").outcome == SUCCESS
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Description', 'An Example Plugin!')
    dependencyExists('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'META-INF/lib/example-squid-1.0.jar')
    dependencyExists('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'META-INF/lib/example-checks-1.0.jar')
  }

  def "invalid pluginKey causes failure"() {
    setup:
    buildFile << """plugins {
                      id 'com.iadams.sonar-packaging'
                    }

                    sonarPackaging {
                        pluginKey = 'key-with.bad%characters'
                        pluginClass = ' org.sonar.plugins.example.SamplePlugin'
                        pluginDescription = 'Sample Plugin!'
                    }"""

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .buildAndFail()

    then:
    result.task(":pluginPackaging").outcome == FAILED
    result.output.contains('Plugin key is badly formatted. Please use ascii letters and digits only: ')
  }

  def 'setup a plugin for sonarLint'() {
    setup:
    writeHelloWorld('com.example')
    copyResources('sonarlint.gradle', 'build.gradle')
    settingsFile << '''rootProject.name="example"'''

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":build").outcome == SUCCESS
    manifestContains('build/libs/example-1.0.jar', 'SonarLint-Supported', 'true')
  }

  def 'we can define the minimum sonarqube version'() {
    setup:
    writeHelloWorld('com.example')
    copyResources('sonarQubeMinVersion.gradle', 'build.gradle')
    settingsFile << '''rootProject.name="example"'''

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(":build").outcome == SUCCESS
    manifestContains('build/libs/example-1.0.jar', 'Sonar-Version', '4.4.0')
  }
}
