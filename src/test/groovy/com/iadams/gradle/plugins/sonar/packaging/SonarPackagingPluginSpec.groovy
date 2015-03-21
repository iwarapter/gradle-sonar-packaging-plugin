package com.iadams.gradle.plugins.sonar.packaging

import nebula.test.PluginProjectSpec

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

    def "apply creates sonar-packaging extension" () {
        expect: project.extensions.findByName('sonarpackaging')
    }
}
