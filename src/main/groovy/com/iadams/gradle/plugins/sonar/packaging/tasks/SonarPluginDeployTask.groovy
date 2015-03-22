package com.iadams.gradle.plugins.sonar.packaging.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Created by iwarapter
 */
class SonarPluginDeployTask extends DefaultTask {

    @OutputDirectory
    File localServer

    @InputFile
    File pluginJar

    @TaskAction
    void deployPlugin() {
        logger.info "Deploying $pluginJar to $localServer"
        project.copy {
            from pluginJar
            into localServer
        }
    }
}
