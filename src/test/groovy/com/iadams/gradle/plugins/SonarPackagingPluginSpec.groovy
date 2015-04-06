package com.iadams.gradle.plugins

import nebula.test.PluginProjectSpec
import org.gradle.api.Task

/**
 * Created by iwarapter
 */
class SonarPackagingPluginSpec extends PluginProjectSpec {

    static final String PLUGIN_ID = 'com.iadams.sonar-packaging'

    @Override
    String getPluginName() {
        return PLUGIN_ID
    }

    def setup() {
        project.apply plugin: pluginName
    }

    def "apply creates sonarPackaging extension" () {
        expect: project.extensions.findByName('sonarPackaging')
    }

    def "extension properties are mapped to packaging task"(){
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
        Task task = project.tasks.findByName( SonarPackagingPlugin.SONAR_PACKAGING_TASK )
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
}
