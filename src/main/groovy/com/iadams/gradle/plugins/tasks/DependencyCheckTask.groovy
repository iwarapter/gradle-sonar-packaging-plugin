package com.iadams.gradle.plugins.tasks

import com.iadams.gradle.plugins.core.DependencyQuery
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by iwarapter
 */
class DependencyCheckTask extends DefaultTask {

    private static final String[] GWT_ARTIFACT_IDS = ["gwt-user", "gwt-dev", "sonar-gwt-api"]
    private static final String[] LOG_GROUP_IDS = ["log4j", "commons-logging"]

    @Input
    boolean isSkipDependenciesPackaging

    @TaskAction
    void checkDependencies(){
        def query = new DependencyQuery(project)
        if (!isSkipDependenciesPackaging) {
            query.checkApiDependency()
            query.checkForDependencies(LOG_GROUP_IDS)
            query.checkForDependencies(GWT_ARTIFACT_IDS)
        }
    }
}
