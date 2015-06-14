Gradle Sonar Packaging Plugin
=========

This is a gradle plugin for building plugins for [SonarQube] it is a port of the original maven plugin. 

Waffle Board (Issues)
------------
[![Stories in Ready](https://badge.waffle.io/iwarapter/gradle-sonar-packaging-plugin.png?label=ready&title=Ready)](https://waffle.io/iwarapter/gradle-sonar-packaging-plugin)

Build Status
------------
[![Build Status](https://travis-ci.org/iwarapter/gradle-sonar-packaging-plugin.svg?branch=master)](https://travis-ci.org/iwarapter/gradle-sonar-packaging-plugin)

Latest Version
--------------
All versions can be found [here].

Usage
-----------

Build script snippet for use in all Gradle versions:
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.iadams.plugins:gradle-sonar-packaging-plugin:0.1"
  }
}

apply plugin: "com.iadams.sonar-packaging"
```
Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:
```
plugins {
  id "com.iadams.sonar-packaging" version "0.1"
}
```

Tasks
-----------
```
Sonar Packaging tasks
---------------------
localDeploy - Copies the built plugin to the local server.
pluginPackaging - Updates the Jar file with the correct dependencies and manifest info.
restartServer - Restarts a SonarQube server running in dev mode.
```
## Configuration

### build.gradle
```groovy
sonarPackaging {
    serverUrl = 'http://localhost:9000'
    pluginDir = '/tmp/sonarqube-5.1/extensions/plugins'
    pluginKey = 'example'
    pluginClass = 'com.example.ExamplePlugin'
    pluginName = 'An Example Plugin.'
    pluginDescription = 'This plugin is an example.'
    pluginParent = null
    pluginLicense = 'MIT'
    requirePlugins = null
    pluginUrl = 'http://mypluginwebsite.com'
    pluginIssueTrackerUrl = 'http://mypluginwebsite.com/issues'
    pluginTermsConditionsUrl = 'http://mypluginwebsite.com/terms'
    pluginSourceUrl = 'http://github.com'
    pluginDevelopers = 'Bob Smith'
    skipDependenciesPackaging = false
    useChildFirstClassLoader = false
    basePlugin = ''
    organization{
        name = 'company-corp'
        url = 'http://company-corp.com'
    }
}
```
Property     | Description
------------ | -------------
`serverUrl`| URL for the local dev server for the restartServer task
`pluginDir` | Location for the local dev server plugin directory for the localDeploy task.
`pluginKey`(required) | Key for plugin. Should contain only letters and digits and be unique among all plugins.
`pluginClass`(required) | The class which implements org.sonar.api.Plugin
`pluginName`(required) | Name of the plugin.
`pluginDescription`(required) | Plugin description.
`pluginParent` | Plugin parent.
`pluginLicense` | License plugin is released under.
`requirePlugins` | List of dependencies using the following format : parentPluginKey1:minimalParentPluginVersion1, parentPluginKey2:minimalParentPluginVersion2, ...
`pluginUrl` | Plugin homepage.
`pluginIssueTrackerUrl` | Plugin issue tracker URL.
`pluginTermsConditionsUrl` | Plugin Terms & Conditions URL.
`pluginSourceUrl` | Link to source repository.
`pluginDevelopers` | List of plugin developers.
`skipDependenciesPackaging` | Do not package plugin dependencies in META-INF
`useChildFirstClassLoader` | Default classloader strategy is parent-first. Set to true to use child-first strategy.
`basePlugin` | If specified, then plugin will re-use ClassLoader of specified plugin.
`organization name` | Organization name.
`organization url` | Organization url.

[SonarQube]:http://www.sonarqube.org/
[here]:https://plugins.gradle.org/plugin/com.iadams.sonar-packaging
