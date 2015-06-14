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
package com.iadams.gradle.plugins.core

import groovy.util.logging.Slf4j
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.java.archives.internal.DefaultManifest

/**
 * Created by iwarapter
 */
@Slf4j
final class PluginManifest extends DefaultManifest {

    public static final String KEY = "Plugin-Key"
    public static final String NAME = "Plugin-Name"
    public static final String DESCRIPTION = "Plugin-Description"
    public static final String VERSION = "Plugin-Version"
    public static final String MAIN_CLASS = "Plugin-Class"
    public static final String ORGANIZATION = "Plugin-Organization"
    public static final String ORGANIZATION_URL = "Plugin-OrganizationUrl"
    public static final String LICENSE = "Plugin-License"
    public static final String SONAR_VERSION = "Sonar-Version"
    public static final String DEPENDENCIES = "Plugin-Dependencies"
    public static final String HOMEPAGE = "Plugin-Homepage"
    public static final String TERMS_CONDITIONS_URL = "Plugin-TermsConditionsUrl"
    public static final String BUILD_DATE = "Plugin-BuildDate"
    public static final String ISSUE_TRACKER_URL = "Plugin-IssueTrackerUrl"
    public static final String PARENT = "Plugin-Parent"
    public static final String REQUIRE_PLUGINS = "Plugin-RequirePlugins"
    public static final String USE_CHILD_FIRST_CLASSLOADER = "Plugin-ChildFirstClassLoader"
    public static final String BASE_PLUGIN = "Plugin-Base"
    public static final String IMPLEMENTATION_BUILD = "Implementation-Build"
    public static final String SOURCES_URL = "Plugin-SourcesUrl"
    public static final String DEVELOPERS = "Plugin-Developers"

    PluginManifest(){
        super(new IdentityFileResolver())
    }

    void addManifestProperty(String key, String value) {
        attributes.put(key, value)
        log.info "    $key: $value"
    }
}
