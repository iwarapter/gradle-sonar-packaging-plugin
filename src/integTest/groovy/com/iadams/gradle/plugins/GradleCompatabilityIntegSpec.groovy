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
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleCompatabilityIntegSpec extends SonarPackagingBaseIntegSpec {

  @Unroll
  def "compatible with gradle #gradleVersion"() {
    setup:
    def sub1 = addSubproject('example-squid')
    def sub2 = addSubproject('sonar-example-plugin')

    writeHelloWorld('com.example', sub1.name)
    writeHelloWorld('com.example', sub2.name)
    copyResources('build.gradle', 'sonar-example-plugin/build.gradle')

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(gradleVersion)
        .withArguments('build')
        .withPluginClasspath(pluginClasspath)
        .build()

    then:
    result.task(':sonar-example-plugin:build').outcome == SUCCESS
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Description', 'An Example Plugin!')
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Key', 'example')
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Class', 'com.example.HelloWorld')
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Name', 'Example')

    where:
    //using the new framework
    gradleVersion << ['2.8', '2.9', '2.10', '2.11', '2.12', '2.13', '2.14', '3.0']
  }

  @Unroll
  def "compatible with legacy gradle #gradleVersion"() {
    setup:
    def sub1 = addSubproject('example-squid')
    def sub2 = addSubproject('sonar-example-plugin')

    writeHelloWorld('com.example', sub1.name)
    writeHelloWorld('com.example', sub2.name)
    copyResources('legacy_build.gradle', 'sonar-example-plugin/build.gradle')

    def classpathString = pluginClasspath
        .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
        .collect { "'$it'" }
        .join(", ")

    new File(sub2, 'build.gradle') << """
        buildscript {
            dependencies {
                classpath files($classpathString)
            }
        }
    """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(gradleVersion)
        .withArguments('build')
        .build()

    then:
    result.task(':sonar-example-plugin:build').outcome == SUCCESS
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Description', 'An Example Plugin!')
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Key', 'example')
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Class', 'com.example.HelloWorld')
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Name', 'Example')

    where:
    //testing the older versions
    gradleVersion << ['2.5', '2.6', '2.7']
  }
}
