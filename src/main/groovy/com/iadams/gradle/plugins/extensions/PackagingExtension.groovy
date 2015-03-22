package com.iadams.gradle.plugins.extensions

import org.gradle.api.Project
/**
 * Created by iwarapter
 */
class PackagingExtension {

    String localServerUrl = 'http://localhost:9000'

    String localServerPluginDir

    String pluginKey

    String pluginClass

    String pluginName

    String pluginDescription

    String pluginParent

    String requirePlugins

    String pluginUrl

    String pluginIssueTrackerUrl

    String pluginTermsConditionsUrl

    String addMavenDescriptor

    boolean skipDependenciesPackaging = false

    boolean useChildFirstClassLoader = false

    String basePlugin

    String includes

    String excludes

    PackagingExtension( Project project){
        pluginName = project.name
        pluginDescription = project.description
    }
}
