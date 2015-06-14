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
