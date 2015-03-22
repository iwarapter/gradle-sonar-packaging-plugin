package com.iadams.gradle.plugins.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Created by iwarapter
 */
class SonarApiRestartTaskSpec extends Specification {

    static final TASK_NAME = 'myRestartTask'
    Project project

    void setup() {
        project = ProjectBuilder.builder().build()
    }

    def "create task with default configuration"(){
        expect:
        project.tasks.findByName( TASK_NAME ) == null

        when:
        project.task( TASK_NAME, type: SonarApiRestartTask )

        then:
        Task task = project.tasks.findByName( TASK_NAME )
        task != null
    }

    def "create a task with custom configuration"() {
        expect:
        project.tasks.findByName( TASK_NAME ) == null

        when:
        project.task( TASK_NAME, type: SonarApiRestartTask ) {
            serverUrl = 'http://localhost:9005'
        }

        then:
        Task task = project.tasks.findByName( TASK_NAME )
        task != null
        task.serverUrl == 'http://localhost:9005'
    }
}
