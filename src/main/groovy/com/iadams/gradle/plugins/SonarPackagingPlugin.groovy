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

import com.iadams.gradle.plugins.extensions.PackagingExtension
import com.iadams.gradle.plugins.extensions.PackagingOrganizationExtension
import com.iadams.gradle.plugins.tasks.PackagePluginTask
import com.iadams.gradle.plugins.tasks.SonarApiRestartTask
import com.iadams.gradle.plugins.tasks.SonarPluginDeployTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.plugins.PublishingPlugin

/**
 * Created by iwarapter
 */
class SonarPackagingPlugin implements Plugin<Project> {

  static final SONAR_PACKAGING_GROUP = 'Sonar Packaging'
  static final SONAR_PACKAGING_EXTENSION = 'sonarPackaging'
  static final SONAR_PACKAGING_ORGANIZATION_EXTENSION = 'organization'
  static final SONAR_PACKAGING_TASK = 'pluginPackaging'
  static final SONAR_PLUGIN_LOCAL_DEPLOY_TASK = 'localDeploy'
  static final SONAR_API_RESTART_TASK = 'restartServer'

  @Override
  void apply(Project project) {

    project.plugins.apply JavaPlugin

    PackagingExtension packagingExtension = project.extensions.create(SONAR_PACKAGING_EXTENSION, PackagingExtension, project)
    packagingExtension.extensions.create(SONAR_PACKAGING_ORGANIZATION_EXTENSION, PackagingOrganizationExtension)
    project.configurations.create('provided')
    project.configurations.compile.extendsFrom(project.configurations.getByName('provided'))

    addTasks(project)

    configureMavenPublishPlugin(project)
    configureMavenPlugin(project)
  }

  /**
   * Adds the plugin deploy and server restart tasks.
   *
   * @param project
   */
  void addTasks(Project project) {
    def extension = project.extensions.findByName(SONAR_PACKAGING_EXTENSION)

    project.task(SONAR_PACKAGING_TASK, type: PackagePluginTask) {
      description = 'Updates the Jar file with the correct dependencies and manifest info.'
      group = SONAR_PACKAGING_GROUP

      conventionMapping.pluginClass = {extension.pluginClass}
      conventionMapping.pluginDescription = {extension.pluginDescription}
      conventionMapping.pluginDevelopers = {extension.pluginDevelopers}
      conventionMapping.pluginUrl = {extension.pluginUrl}
      conventionMapping.pluginIssueTrackerUrl = {extension.pluginIssueTrackerUrl}
      conventionMapping.pluginKey = {extension.pluginKey}
      conventionMapping.pluginLicense = {extension.pluginLicense}
      conventionMapping.pluginName = {extension.pluginName}
      conventionMapping.organizationName = {extension.organization.name}
      conventionMapping.organizationUrl = {extension.organization.url}
      conventionMapping.pluginSourceUrl = {extension.pluginSourceUrl}
      conventionMapping.pluginTermsConditionsUrl = {extension.pluginTermsConditionsUrl}
      conventionMapping.useChildFirstClassLoader = {extension.useChildFirstClassLoader}
      conventionMapping.pluginParent = {extension.pluginParent}
      conventionMapping.requirePlugins = {extension.requirePlugins}
      conventionMapping.basePlugin = {extension.basePlugin}
      conventionMapping.skipDependenciesPackaging = {extension.skipDependenciesPackaging}
    }

    project.tasks.findByName('jar').finalizedBy SONAR_PACKAGING_TASK

    project.task(SONAR_PLUGIN_LOCAL_DEPLOY_TASK, type: SonarPluginDeployTask) {
      description = 'Copies the built plugin to the local server.'
      group = SONAR_PACKAGING_GROUP

      conventionMapping.localServer = {project.file(extension.pluginDir)}
      conventionMapping.pluginJar = {
        project.file("${project.libsDir}/${project.archivesBaseName}-${project.version}.jar")
      }
    }

    project.task(SONAR_API_RESTART_TASK, type: SonarApiRestartTask) {
      description = 'Restarts a SonarQube server running in dev mode.'
      group = SONAR_PACKAGING_GROUP

      conventionMapping.serverUrl = {extension.serverUrl}
      conventionMapping.restartApiPath = {extension.restartApiPath}
    }
  }

  static void configureMavenPublishPlugin(Project project) {
    project.plugins.withType(PublishingPlugin) {PublishingPlugin publishingPlugin ->
      project.publishing {
        publications.withType(MavenPublication) {
          MavenPublication publication ->
            publication.pom.withXml {
              it.asNode().appendNode('packaging', 'sonar-plugin')
            }
        }
      }
    }
  }

  private static void configureMavenPlugin(Project project) {
    project.plugins.withType(MavenPlugin) {
      // Requires user definition of Maven installer/deployer which could be anywhere in the build script
      project.afterEvaluate {
        def installer = project.tasks.install.repositories.mavenInstaller
        def deployer = project.tasks.uploadArchives.repositories.mavenDeployer
        [installer, deployer]*.pom*.whenConfigured {pom ->
          pom.packaging = 'sonar-plugin'
        }
      }
    }
  }
}
