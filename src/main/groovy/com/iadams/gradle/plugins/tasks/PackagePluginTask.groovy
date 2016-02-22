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
package com.iadams.gradle.plugins.tasks

import com.iadams.gradle.plugins.core.DependencyQuery
import com.iadams.gradle.plugins.core.PluginKeyUtils
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by iwarapter
 */
class PackagePluginTask extends Jar {

  @Override
  PackagePluginTask manifest(Closure<?> configureClosure) {
    super.manifest(configureClosure)
    return this;
  }

  private static final String[] GWT_ARTIFACT_IDS = ["gwt-user", "gwt-dev", "sonar-gwt-api"]
  private static final String[] LOG_GROUP_IDS = ["log4j", "commons-logging"]

  @Input
  String pluginClass

  @Input
  String pluginDescription

  @Input
  String pluginDevelopers

  @Input
  String pluginUrl

  @Input
  String pluginIssueTrackerUrl

  @Input
  String pluginKey

  @Input
  String pluginLicense

  @Input
  String pluginName

  @Input
  String organizationName

  @Input
  String organizationUrl

  @Input
  String pluginSourceUrl

  @Input
  String pluginTermsConditionsUrl

  @Input
  @Optional
  Boolean useChildFirstClassLoader

  @Input
  @Optional
  String pluginParent = null

  @Input
  @Optional
  String requirePlugins = null

  @Input
  @Optional
  String basePlugin

  @Input
  boolean skipDependenciesPackaging

  @TaskAction
  protected void copy() {
    logging.level = LogLevel.INFO

    checkMandatoryAttributes()

    def query = new DependencyQuery(project)
    if (!getSkipDependenciesPackaging()) {
      query.checkApiDependency()
      query.checkForDependencies(LOG_GROUP_IDS)
      query.checkForDependencies(GWT_ARTIFACT_IDS)
    }

    logger.info "-------------------------------------------------------"
    logger.info "Plugin definition in update center"
    //manifest = new PluginManifest()
    manifest {
      attributes("Created-By": "Gradle",
        "Built-By": System.getProperty('user.name'),
        "Build-Jdk": System.getProperty('java.version'),
        "Build-Time": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
        "Plugin-BuildDate": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
        "Plugin-Class": getPluginClass(),
        "Plugin-Description": getPluginDescription(),
        "Plugin-Developers": getPluginDevelopers(),
        "Plugin-Homepage": getPluginUrl(),
        "Plugin-IssueTrackerUrl": getPluginIssueTrackerUrl(),
        "Plugin-Key": getPluginKey(),
        "Plugin-License": getPluginLicense(),
        "Plugin-Name": getPluginName(),
        "Plugin-Organization": getOrganizationName(),
        "Plugin-OrganizationUrl": getOrganizationUrl(),
        "Plugin-SourcesUrl": getPluginSourceUrl(),
        "Plugin-TermsConditionsUrl": getPluginTermsConditionsUrl(),
        "Plugin-Version": project.version,
        "Sonar-Version": new DependencyQuery(project).sonarPluginApiArtifact.moduleVersion)
    }

    /**
     * If these extension points are null dont add to manifest
     */
    if (getUseChildFirstClassLoader()) {
      manifest.attributes.put("Plugin-ChildFirstClassLoader", getUseChildFirstClassLoader().toString())
    }
    if (getPluginParent()) {
      manifest.attributes.put("Plugin-Parent", getPluginParent())
    }
    if (getRequirePlugins()) {
      manifest.attributes.put("Plugin-RequirePlugins", getRequirePlugins())
    }
    if (getBasePlugin()) {
      manifest.attributes.put("Plugin-Base", getBasePlugin())
    }

    getLogger().info '-------------------------------------------------------'

    checkParentAndRequiresPluginProperties()

    if (!getSkipDependenciesPackaging()) {

      List<ResolvedDependency> dependencies = new DependencyQuery(project).getNotProvidedDependencies()

      final List<String> deps = new ArrayList<>();
      dependencies.each {
        String classifier = it.moduleArtifacts[0].classifier ? "-${it.moduleArtifacts[0].classifier}" : ''
        deps.add("${it.moduleName}-${it.moduleVersion}${classifier}.jar")
      }

      manifest.attributes.put("Plugin-Dependencies", deps.collect {"META-INF/lib/${it}"}.join(' '))

      if (dependencies.size() > 0) {
        logger.info "Following dependencies are packaged in the plugin:\n"
        dependencies.each {logger.info "\t${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}"}
        logger.info "\nSee following page for more details about plugin dependencies:\n"
        logger.info "\thttp://docs.sonarqube.org/display/DEV/Coding+a+Plugin\n"
      }

      from project.sourceSets.main.output
      into('META-INF/lib') {
        List<File> artifacts = []
        dependencies.each {ResolvedDependency dep ->
          dep.getModuleArtifacts().each {artifacts.add(it.getFile().absoluteFile)}
        }
        from artifacts
      }
    }

    super.copy()
    getLogger().info('Sonar Plugin Created.')

    logging.level = LogLevel.LIFECYCLE
  }

  /**
   * Checks basic properties are valid, before packaging.
   */
  private void checkMandatoryAttributes() {
    checkPluginKey()
    checkPluginClass()
  }

  private void checkPluginKey() throws GradleException {
    if (!getPluginKey().isEmpty() && !PluginKeyUtils.isValid(getPluginKey())) {
      throw new GradleException("Plugin key is badly formatted. Please use ascii letters and digits only: " + getPluginKey());
    }
  }

  private void checkPluginClass() throws GradleException {
    if (!new File(project.sourceSets.main.output.classesDir, getPluginClass().replace('.', '/') + ".class").exists()) {
      throw new GradleException("Plugin class not found: " + getPluginClass())
    }
  }

  private void checkParentAndRequiresPluginProperties() {
    if (getPluginParent() != null && getRequirePlugins() != null) {
      throw new GradleException("The plugin '${getPluginKey()}' can't be both part of the plugin '${getPluginParent()}' and having a dependency on '${getRequirePlugins()}'")
    }
    if (getPluginParent() != null && getPluginParent().equals(getPluginKey())) {
      throw new GradleException("The plugin '${getPluginKey()}' can't be his own parent. Please remove the '${"Plugin-Parent"}' property.")
    }
  }
}
