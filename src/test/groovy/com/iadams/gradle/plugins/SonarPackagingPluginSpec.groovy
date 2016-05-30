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

import com.iadams.gradle.plugins.core.DependencyQuery
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Created by iwarapter
 */
class SonarPackagingPluginSpec extends Specification {

  static final String PLUGIN_ID = 'com.iadams.sonar-packaging'
  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.pluginManager.apply PLUGIN_ID
  }

  def "apply creates sonarPackaging extension"() {
    expect:
    project.extensions.findByName('sonarPackaging')
  }

  def "extension properties are mapped to packaging task"() {
    setup:
    def ext = project.extensions.findByName('sonarPackaging')
    ext.pluginKey = 'cheese'
    ext.pluginClass = 'cheese'
    ext.pluginName = 'cheese'
    ext.pluginDescription = 'cheese'
    ext.pluginParent = 'cheese'
    ext.pluginLicense = 'cheese'
    ext.requirePlugins = 'cheese'
    ext.pluginUrl = 'cheese'
    ext.pluginIssueTrackerUrl = 'cheese'
    ext.pluginTermsConditionsUrl = 'cheese'
    ext.pluginSourceUrl = 'cheese'
    ext.pluginDevelopers = 'cheese'
    ext.skipDependenciesPackaging = true
    ext.useChildFirstClassLoader = true
    ext.basePlugin = 'cheese'
    ext.organization.name = 'cheese'
    ext.organization.url = 'cheese'

    expect:
    Task task = project.tasks.findByName(SonarPackagingPlugin.SONAR_PACKAGING_TASK)
    task.getPluginKey() == 'cheese'
    task.getPluginClass() == 'cheese'
    task.getPluginName() == 'cheese'
    task.getPluginDescription() == 'cheese'
    task.getPluginParent() == 'cheese'
    task.getPluginLicense() == 'cheese'
    task.getRequirePlugins() == 'cheese'
    task.getPluginUrl() == 'cheese'
    task.getPluginIssueTrackerUrl() == 'cheese'
    task.getPluginTermsConditionsUrl() == 'cheese'
    task.getPluginSourceUrl() == 'cheese'
    task.getPluginDevelopers() == 'cheese'
    task.getSkipDependenciesPackaging() == true
    task.getUseChildFirstClassLoader() == true
    task.getBasePlugin() == 'cheese'
    task.getOrganizationName() == 'cheese'
    task.getOrganizationUrl() == 'cheese'
  }

  def "we can check plugin dependencies"() {
    given:
    project.repositories {jcenter()}
    project.dependencies {provided 'org.sonarsource.sonarqube:sonar-plugin-api:5.2'}
    project.dependencies {provided 'org.sonarsource.java:sonar-java-plugin:3.9'}
    project.dependencies {provided 'org.sonarsource.dotnet:sonar-csharp-plugin:4.4'}
    project.dependencies {compile 'org.apache.commons:commons-lang3:3.2.1'}
    project.dependencies {compile 'com.thoughtworks.xstream:xstream:1.4.8'}

    when:
    DependencyQuery q = new DependencyQuery(project)
    def r = q.getNotProvidedDependencies()

    then:
    r.size() == 3
  }

  def "sonar dependencies are not packaged"() {
    given:
    project.repositories {jcenter()}
    project.dependencies {provided 'org.sonarsource.sonarqube:sonar-plugin-api:5.2'}
    project.dependencies {provided 'org.sonarsource.java:sonar-java-plugin:3.9'}
    project.dependencies {provided 'org.sonarsource.dotnet:sonar-csharp-plugin:4.4'}

    when:
    DependencyQuery q = new DependencyQuery(project)
    def r = q.getNotProvidedDependencies()

    then:
    r.size() == 0
  }

  def "provided dependencies are not included"() {
    given:
    project.repositories {jcenter()}
    project.dependencies {provided 'org.sonarsource.sonarqube:sonar-plugin-api:5.2'}
    project.dependencies {compile 'org.codehaus.sonar.sslr:sslr-core:1.20'}
    project.dependencies {compile 'org.codehaus.sonar.sslr-squid-bridge:sslr-squid-bridge:2.5.3'}

    when:
    DependencyQuery q = new DependencyQuery(project)
    def r = q.getNotProvidedDependencies()

    then:
    r.find{ it.toString().contains('org.codehaus.sonar.sslr-squid-bridge:sslr-squid-bridge:2.5.3')}
    r.find{ it.toString().contains('org.codehaus.sonar.sslr:sslr-core:1.20')}
    r.find{ it.toString().contains('org.codehaus.sonar.sslr:sslr-xpath:1.20')}
    r.find{ it.toString().contains('jaxen')}
    r.find{ it.toString().contains('picocontainer')}
    r.size() == 5
  }
}
