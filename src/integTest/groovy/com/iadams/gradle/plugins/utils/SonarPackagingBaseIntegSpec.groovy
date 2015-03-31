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
