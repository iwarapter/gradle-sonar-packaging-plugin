package com.iadams.gradle.plugins.tasks

import com.iadams.gradle.plugins.SonarPackagingPlugin
import com.iadams.gradle.plugins.core.DependencyQuery
import com.iadams.gradle.plugins.core.PluginManifest
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by iwarapter
 */
class PackagePluginTask extends Jar {

    private static final String[] GWT_ARTIFACT_IDS = ["gwt-user", "gwt-dev", "sonar-gwt-api"]
    private static final String[] LOG_GROUP_IDS = ["log4j", "commons-logging"]

    //TODO Add all the plugin properties to the task.
    //Pulling direct from the extension feels.... dirty

    @Input
    boolean isSkipDependenciesPackaging

    @TaskAction
    protected void copy() {
        def query = new DependencyQuery(project)
        if (!isSkipDependenciesPackaging) {
            query.checkApiDependency()
            query.checkForDependencies(LOG_GROUP_IDS)
            query.checkForDependencies(GWT_ARTIFACT_IDS)
        }

        def extension = project.extensions.findByName(SonarPackagingPlugin.SONAR_PACKAGING_EXTENSION)
        getLogger().info "-------------------------------------------------------"
        getLogger().info "Plugin definition in update center"
        manifest = new PluginManifest()
        manifest.addManifestProperty("Created-By", "Gradle")
        manifest.addManifestProperty("Built-By", System.getProperty('user.name'))
        manifest.addManifestProperty("Build-Jdk", System.getProperty('java.version'))
        manifest.addManifestProperty("Build-Time", new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"))
        manifest.addManifestProperty(PluginManifest.BUILD_DATE, new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"))
        manifest.addManifestProperty(PluginManifest.MAIN_CLASS, extension.pluginClass)
        manifest.addManifestProperty(PluginManifest.DESCRIPTION, extension.pluginDescription)
        manifest.addManifestProperty(PluginManifest.DEVELOPERS, extension.pluginDevelopers)
        manifest.addManifestProperty(PluginManifest.HOMEPAGE, extension.pluginUrl)
        manifest.addManifestProperty(PluginManifest.ISSUE_TRACKER_URL, extension.pluginIssueTrackerUrl)
        manifest.addManifestProperty(PluginManifest.KEY, extension.pluginKey)
        manifest.addManifestProperty(PluginManifest.LICENSE, extension.pluginLicense)
        manifest.addManifestProperty(PluginManifest.NAME, extension.pluginName)
        manifest.addManifestProperty(PluginManifest.ORGANIZATION, extension.organization.name)
        manifest.addManifestProperty(PluginManifest.ORGANIZATION_URL, extension.organization.url)
        manifest.addManifestProperty(PluginManifest.SOURCES_URL, extension.pluginSourceUrl)
        manifest.addManifestProperty(PluginManifest.TERMS_CONDITIONS_URL, extension.pluginTermsConditionsUrl)
        manifest.addManifestProperty(PluginManifest.VERSION, project.version)
        manifest.addManifestProperty(PluginManifest.SONAR_VERSION, new DependencyQuery(project).sonarPluginApiArtifact.moduleVersion)
        if (extension.useChildFirstClassLoader){
            manifest.addManifestProperty(PluginManifest.USE_CHILD_FIRST_CLASSLOADER, extension.useChildFirstClassLoader.toString())
        }

        /**
         * If these extension points are null dont add to manifest
         */
        if(extension.pluginParent) {
            manifest.addManifestProperty(PluginManifest.PARENT, extension.pluginParent)
        }
        if(extension.requirePlugins){
            manifest.addManifestProperty(PluginManifest.REQUIRE_PLUGINS, extension.requirePlugins)
        }
        if(extension.basePlugin){
            manifest.addManifestProperty(PluginManifest.BASE_PLUGIN, extension.basePlugin)
        }

        getLogger().info '-------------------------------------------------------'

        if(!isSkipDependenciesPackaging) {

            List<ResolvedDependency> dependencies = new DependencyQuery(project).getNotProvidedDependencies()
            from project.sourceSets.main.output
            into('META-INF/lib') {
                List<File> artifacts = []
                dependencies.each{ ResolvedDependency dep->
                    dep.getModuleArtifacts().each{ artifacts.add(it.getFile().absoluteFile) }
                }
                from artifacts
            }
            manifest.addManifestProperty(PluginManifest.DEPENDENCIES, dependencies.collect{ "META-INF/lib/${it.moduleName}:${it.moduleVersion}.jar" }.join(' '))
        }

        super.copy()
        getLogger().info('Sonar Plugin Created.')
    }
}
