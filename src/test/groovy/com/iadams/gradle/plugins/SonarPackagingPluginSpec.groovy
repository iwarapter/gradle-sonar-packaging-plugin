package com.iadams.gradle.plugins

import nebula.test.PluginProjectSpec

/**
 * Created by iwarapter
 */
class SonarPackagingPluginSpec extends PluginProjectSpec {

    static final String PLUGIN_ID = 'com.iadams.sonarPackaging'

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
}
