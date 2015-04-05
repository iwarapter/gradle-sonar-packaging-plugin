package com.iadams.gradle.plugins.core

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by iwarapter
 */
class PluginKeyUtilsSpec extends Specification {

	@Unroll
	def "#input is valid"() {
		expect:
		PluginKeyUtils.isValid(input)

		where:
		input << ['foo', 'sonarfooplugin', 'foo6', 'FOO6']
	}

	@Unroll
	def "#input is invalid"(){
		expect:
		!PluginKeyUtils.isValid(input)

		where:
		input << [null, '', 'sonar-foo-plugin', 'foo.bar', '  nowhitespaces   ', 'no whitespaces']
	}
}
