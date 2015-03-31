package com.iadams.gradle.plugins.core

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

/**
 * Created by iwarapter
 */
class DependencyQuery {

    Project project

    DependencyQuery(Project proj){
        this.project = proj
    }

    public static final String SONAR_GROUPID = "org.codehaus.sonar"
    public static final String SONAR_PLUGIN_API_ARTIFACTID = "sonar-plugin-api"
    public static final String SONAR_PLUGIN_API_TYPE = "jar"

    Set<ResolvedDependency> getDependencyArtifacts(){
        project.configurations.compile.resolvedConfiguration.firstLevelModuleDependencies
    }

    final ResolvedDependency getSonarPluginApiArtifact() {

        for(ResolvedDependency it : getDependencyArtifacts()) {
            if (SONAR_GROUPID.equals(it.moduleGroup) && SONAR_PLUGIN_API_ARTIFACTID.equals(it.moduleName)
                    && SONAR_PLUGIN_API_TYPE.equals(it.moduleArtifacts[0].type)) {
                return it
            }
        }
        return null
    }

    void checkApiDependency() throws GradleException {
        ResolvedDependency sonarApi = getSonarPluginApiArtifact()

        project.logger.info "Checking for $SONAR_PLUGIN_API_ARTIFACTID"

        if (sonarApi == null) {
            throw new GradleException(
                    "$SONAR_GROUPID:$SONAR_PLUGIN_API_ARTIFACTID should be declared in dependencies");
        }
    }

    void checkForDependencies(String[] group){
        def ids = []
        getDependencyArtifacts().each{
            if(group.contains(it.moduleName)) {
                ids.add( it.moduleName )
            }
        }
        if (!ids.empty) {
            project.logger.warn "The following dependencies should be defined with scope 'provided': ${ids}"
        }
    }

    /**
     * Parses a pom.xml and returns the packaging value
     *
     * @param pomFile
     * @return
     */
    def getPomPackagingType(File pomFile) {
        project.logger.debug "Parsing $pomFile"
        def pom = new XmlSlurper().parseText(pomFile.text)

        return pom.packaging
    }

    def getDependency(String dep){
        for(it in project.configurations.compile.incoming.resolutionResult.allDependencies){
            if(it.selected.id.toString() == dep){
                return it.selected.id
            }
        }
    }

    /**
     * For each dependency, add all children in the given configuration (recursively)
     *
     * @param dependency
     * @param configuration
     * @param result
     */
    void getAllDependencies(ResolvedDependency dependency, String configuration, List<ResolvedDependency> result){

        dependency.getChildren().each { child ->
            if (child.getConfiguration() == configuration) {
                result.add(child)
                getAllDependencies(child, configuration, result)
            }
        }
    }

    /**
     * Queries a plugin pom to check if the packaging type is sonar-plugin
     *
     * @param plugin
     * @return
     */
    boolean isSonarPlugin(String plugin){
        def comp = getDependency(plugin)
        if(comp!=null) {

            def result = project.dependencies.createArtifactResolutionQuery()
                    .forComponents(comp)
                    .withArtifacts(MavenModule, MavenPomArtifact)
                    .execute()

            for (component in result.resolvedComponents) {
                for (art in component.getArtifacts(MavenPomArtifact)) {
                    if (getPomPackagingType(art.file).equals('sonar-plugin')) {
                        return true
                    }
                }
            }
        }
        return false
    }

    def getSonarProvidedArtifacts(){
        def myList = []
        project.configurations.compile.resolvedConfiguration.getFirstLevelModuleDependencies().each{
            searchForSonarProvidedArtifacts(it, myList, false)
        }
        return myList
    }

    def getNotProvidedDependencies(){
        def result = []
        def providedArtifacts = getSonarProvidedArtifacts();
        for (artifact in project.configurations.compile.resolvedConfiguration.getFirstLevelModuleDependencies()) {
            boolean include = true;
            if (isSonarPlugin(artifact.module.id.toString())) {
                project.logger.warn("${artifact.module.id.toString()} is a SonarQube plugin and will not be packaged in your plugin");
                include = false;
            }
            if (providedArtifacts.contains(artifact.getModule().id)) {
                project.logger.warn(artifact.name + " is provided by SonarQube plugin API and will not be packaged in your plugin");
                include = false;
            }
            if (include) {
                result.add(artifact);
            }
        }
        return result;
    }

    void searchForSonarProvidedArtifacts(ResolvedDependency dependency, def sonarArtifacts, boolean isParentProvided){
        if(dependency != null && dependency.configuration.toString().equals('default')) {
            boolean provided
            if(dependency.getParents().findAll{ it.configuration.equals('compile')} != null){
                provided = isParentProvided || ("org.codehaus.sonar".equals(dependency.moduleGroup))
            }
            else{
                provided = isParentProvided
            }
            if (provided) {
                sonarArtifacts.add(dependency.module.id)
            }

            for (child in dependency.getChildren()) {
                if (child.getConfiguration().equals('compile')) {
                    searchForSonarProvidedArtifacts(child, sonarArtifacts, provided)
                }
            }
        }
    }
}
