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
package com.iadams.gradle.plugins.extensions

import org.gradle.api.Project

/**
 * Created by iwarapter
 */
class PackagingExtension {

  /**
   * Address for the web server for the SonarApiRestartTask.
   */
  String serverUrl = 'http://localhost:9000'
  /**
   * Address path for the API call to restart Sonar server.
   * Note: I wouldn't expect this to change.
   */
  String restartApiPath = '/api/system/restart'

  /**
   * Directory to the local sonar server's plugin directory.
   */
  String pluginDir

  /**
   *
   */
  String pluginKey

  String pluginClass

  String pluginName

  String pluginDescription

  String pluginParent = null
  String pluginLicense = ''
  String requirePlugins = null
  String pluginUrl = ''
  String pluginIssueTrackerUrl = ''
  String pluginTermsConditionsUrl = ''
  String pluginSourceUrl = ''
  String pluginDevelopers = ''
  boolean skipDependenciesPackaging = false
  boolean useChildFirstClassLoader = false
  String basePlugin = ''

  PackagingExtension(Project project) {
    pluginName = project.name
    pluginDescription = project.description
  }
}
