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
    manifestContains('sonar-example-plugin/build/libs/sonar-example-plugin-1.0.jar', 'Plugin-Description', 'An Example Plugin!')

    where:
    //Add future versions, not backwards compatible.
    requestedGradleVersion << ['2.3', '2.4', '2.5', '2.6', '2.7']
  }
}
