package com.iadams.gradle.plugins.sonar.packaging

import com.iadams.gradle.plugins.sonar.packaging.extensions.PackagingExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by iwarapter
 */
class SonarPackagingPlugin implements Plugin<Project> {

    static final SONAR_PACKAGING_EXTENSION = 'sonar-packaging'

    @Override
    void apply(Project project) {

        project.extensions.create( SONAR_PACKAGING_EXTENSION, PackagingExtension, project)

        addTasks(project)
    }

    void addTasks(Project project){
        project.task( 'jar' , type: Jar, overwrite: true){

        }
    }
}
