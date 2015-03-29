package com.iadams.gradle.plugins.core

import spock.lang.Specification

/**
 * Created by iwarapter
 */
class PluginManifestSpec extends Specification {
    def "AddManifestProperty"() {
        setup:
        PluginManifest manifest = new PluginManifest()

        when:
        manifest.addManifestProperty('Foo', 'Bar')

        then:
        manifest.attributes.containsKey('Foo')
        manifest.attributes.containsValue('Bar')
    }
}
