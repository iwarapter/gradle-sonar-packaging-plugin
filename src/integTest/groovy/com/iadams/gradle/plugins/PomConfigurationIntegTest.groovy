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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PomConfigurationIntegTest extends SonarPackagingBaseIntegSpec {

  def setup() {
    copyResources('org/sonar/plugins/sample/SamplePlugin.java', 'src/main/java/org/sonar/plugins/sample/SamplePlugin.java')
    settingsFile << '''rootProject.name="example"'''
  }

  def "a plugin publishing with the maven plugin has correct packaging value"() {
    setup:
    copyResources('maven-plugin.gradle', 'build.gradle')

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('install')
      .withPluginClasspath(pluginClasspath)
      .build()

    then:
    println result.output
    result.task(':install').outcome == SUCCESS
    file('build/libs/example-1.0.jar').exists()
    def project = new XmlSlurper().parse(file('build/poms/pom-default.xml'))
    project.packaging == 'sonar-plugin'
  }

  def "a plugin publishing with the maven-publish plugin has correct packaging value"() {
    setup:
    copyResources('maven-publish-plugin.gradle', 'build.gradle')

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('build', 'publish')
      .withPluginClasspath(pluginClasspath)
      .build()

    then:
    println result.output
    result.task(':publish').outcome == SUCCESS
    file('build/libs/example-1.0.jar').exists()
    def project = new XmlSlurper().parse(file('build/publications/mavenJava/pom-default.xml'))
    project.packaging == 'sonar-plugin'
  }
}
