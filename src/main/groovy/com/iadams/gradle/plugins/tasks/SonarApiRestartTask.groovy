package com.iadams.gradle.plugins.tasks

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import org.apache.http.conn.HttpHostConnectException
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import static groovyx.net.http.ContentType.URLENC

/**
 * Created by iwarapter
 */
class SonarApiRestartTask extends DefaultTask {

    @Input
    String serverUrl

    @Input
    String restartApiPath

    @TaskAction
    void restartServer() {
        logger.lifecycle "Restarting Server: ${getServerUrl()}"
        try {
            def http = new HTTPBuilder(getServerUrl())

            http.post(path: getRestartApiPath(), requestContentType: URLENC) { }
        }
        catch( HttpHostConnectException e ){
            throw new GradleException("Connection to ${getServerUrl()} refused.", e)
        }
        catch( HttpResponseException e){
            throw new GradleException("Connection Forbidden, is Dev-Mode enabled?", e)
        }
        logger.lifecycle "Server Restarted."
    }
}
