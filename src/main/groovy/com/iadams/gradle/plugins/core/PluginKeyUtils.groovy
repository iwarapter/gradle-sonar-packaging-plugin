package com.iadams.gradle.plugins.core

import org.apache.commons.lang.StringUtils

/**
 * Created by iwarapter
 */
final class PluginKeyUtils {

	static boolean isValid(String pluginKey){
		return StringUtils.isNotBlank(pluginKey) && StringUtils.isAlphanumeric(pluginKey);
	}
}
