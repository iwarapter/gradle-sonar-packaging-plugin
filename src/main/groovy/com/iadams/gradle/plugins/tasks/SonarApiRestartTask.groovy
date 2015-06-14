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
