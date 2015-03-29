package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.core.DependencyQuery
import com.iadams.gradle.plugins.core.PluginManifest
import com.iadams.gradle.plugins.extensions.PackagingExtension
import com.iadams.gradle.plugins.extensions.PackagingOrganizationExtension
import com.iadams.gradle.plugins.tasks.DependencyCheckTask
import com.iadams.gradle.plugins.tasks.SonarApiRestartTask
import com.iadams.gradle.plugins.tasks.SonarPluginDeployTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by iwarapter
 */
class SonarPackagingPlugin implements Plugin<Project> {

    static final SONAR_PACKAGING_GROUP = 'Sonar Packaging'
    static final SONAR_PACKAGING_EXTENSION = 'sonarpackaging'
    static final SONAR_PACKAGING_ORGANIZATION_EXTENSION = 'organization'
    static final SONAR_PLUGIN_LOCAL_DEPLOY_TASK = 'localDeploy'
    static final SONAR_API_RESTART_TASK = 'restartServer'
    static final SONAR_CHECK_DEPENDENCIES_TASK = 'checkDependencies'

    @Override
    void apply(Project project) {

        project.plugins.apply JavaPlugin
        project.extensions.create( SONAR_PACKAGING_EXTENSION, PackagingExtension, project)
        project.extensions.findByName(SONAR_PACKAGING_EXTENSION).extensions.create( SONAR_PACKAGING_ORGANIZATION_EXTENSION, PackagingOrganizationExtension)
        project.configurations.create('sonarqube')
        project.configurations.create('provided')
        project.sourceSets.main.compileClasspath += project.configurations.provided
        project.sourceSets.test.compileClasspath += project.configurations.provided
        project.sourceSets.test.runtimeClasspath += project.configurations.provided

        project.afterEvaluate{
            addTasks(project)
        }
    }

    /**
     * Adds the plugin deploy and server restart tasks.
     *
     * @param project
     */
    void addTasks(Project project){
        def extension = project.extensions.findByName(SONAR_PACKAGING_EXTENSION)

        //TODO REFACTOR ME
        //I don't like this, i'd much rather define a default task to perform these actions
        project.tasks.withType(Jar) {

            List<ResolvedDependency> dependencies = []
            //TODO Tasks with type will do this to all Jar tasks :(
            doFirst {
                project.logger.info "Creating Manifest"
                manifest = new PluginManifest()
                manifest.addManifestProperty("Created-By", "Gradle")
                manifest.addManifestProperty("Built-By", System.getProperty('user.name'))
                manifest.addManifestProperty("Build-Jdk", System.getProperty('java.version'))
                manifest.addManifestProperty("Build-Time", new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"))
                manifest.addManifestProperty(PluginManifest.BUILD_DATE, new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"))
                manifest.addManifestProperty(PluginManifest.MAIN_CLASS, extension.pluginClass)
                if(!extension.skipDependenciesPackaging) {
                    dependencies = new DependencyQuery(project).getNotProvidedDependencies()
                    manifest.addManifestProperty(PluginManifest.DEPENDENCIES, dependencies.collect{ "META-INF/lib/${it.moduleName}:${it.moduleVersion}.jar" }.join(' '))
                }
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

                manifest.writeTo("${project.buildDir}/tmp/jar/MANIFEST.MF")
            }
            if(!extension.skipDependenciesPackaging) {
                into('META-INF/lib') {

                    List<File> artifacts = []
                    new DependencyQuery(project).getNotProvidedDependencies().each{ ResolvedDependency dep->
                        dep.getModuleArtifacts().each{ artifacts.add(it.getFile().absoluteFile) }
                    }
                    from artifacts
                }
            }
        }

        project.task( SONAR_CHECK_DEPENDENCIES_TASK, type: DependencyCheckTask ) {
            description = 'Checks the dependencies for packaging.'
            group = SONAR_PACKAGING_GROUP

            conventionMapping.isSkipDependenciesPackaging = { extension.skipDependenciesPackaging }
        }

        project.task( SONAR_PLUGIN_LOCAL_DEPLOY_TASK, type: SonarPluginDeployTask) {
            description = 'Copies the built plugin to the local server.'
            group = SONAR_PACKAGING_GROUP

            conventionMapping.localServer = { project.file(extension.pluginDir) }
            conventionMapping.pluginJar = { project.file("${project.libsDir}/${project.archivesBaseName}-${project.version}.jar") }
        }

        project.task( SONAR_API_RESTART_TASK, type: SonarApiRestartTask) {
            description = 'Restarts a SonarQube server running in dev mode.'
            group = SONAR_PACKAGING_GROUP

            conventionMapping.serverUrl = { extension.serverUrl }
            conventionMapping.restartApiPath = { extension.restartApiPath }
        }
    }
}
