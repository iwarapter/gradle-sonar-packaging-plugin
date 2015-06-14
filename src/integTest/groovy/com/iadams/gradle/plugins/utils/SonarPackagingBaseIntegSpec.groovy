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
package com.iadams.gradle.plugins.utils

import nebula.test.IntegrationSpec

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by iwarapter
 */
class SonarPackagingBaseIntegSpec extends IntegrationSpec {

    /**
     * Given a jar file asserts the key/value pair exists in the manifest.
     *
     * @param buildJar
     * @param key
     * @param value
     */
    void manifestContains(String buildJar, String key, String value){
        JarFile jarFile = new JarFile(file(buildJar).absolutePath)
        assert jarFile.manifest.mainAttributes.getValue(key) == value
    }

    /**
     * Check to see if a dependency has been packaged.
     *
     * @param buildJar
     * @param name
     * @return
     */
    boolean dependencyExists(String buildJar, String name){
        JarFile jar = new JarFile(file(buildJar).absolutePath)
        JarEntry entry = jar.getJarEntry(name)
        if(entry != null){
            return true
        }else {
            return false
        }
    }
}
