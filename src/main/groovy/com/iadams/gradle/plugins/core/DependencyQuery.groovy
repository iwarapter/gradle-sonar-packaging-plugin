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

    /**
     * Helper method that returns a Set of first level module dependencies for the given configuration.
     * @param configuration
     * @return
     */
    Set<ResolvedDependency> getDependencies(String configuration){
        project.configurations.findByName(configuration).resolvedConfiguration.firstLevelModuleDependencies
    }

    /**
     * Queries the first level module dependencies for the provided configuration for the Sonar API.
     *
     * @return
     */
    final ResolvedDependency getSonarPluginApiArtifact() {

        for(ResolvedDependency it : getDependencies('provided')) {
            if (SONAR_GROUPID.equals(it.moduleGroup) && SONAR_PLUGIN_API_ARTIFACTID.equals(it.moduleName)
                    && SONAR_PLUGIN_API_TYPE.equals(it.moduleArtifacts[0].type)) {
                return it
            }
        }
        return null
    }

    /**
     * Checks the Sonar API is on the provided configuration, throws exception if not found.
     *
     * @throws GradleException
     */
    void checkApiDependency() throws GradleException {
        ResolvedDependency sonarApi = getSonarPluginApiArtifact()

        project.logger.info "Checking for $SONAR_PLUGIN_API_ARTIFACTID"

        if (sonarApi == null) {
            throw new GradleException(
                    "$SONAR_GROUPID:$SONAR_PLUGIN_API_ARTIFACTID should be declared in dependencies")
        }
    }

    /**
     * Check the compile dependencies do not have any of the given artifactIds
     *
     * @param artifactIds
     */
    void checkForDependencies(String[] artifactIds){
        List<String> ids = []
        getDependencies('compile').each{
            if(artifactIds.contains(it.moduleName)) {
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
    String getPomPackagingType(File pomFile) {
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
     * Get a list of all dependencies (recursive) in the given configuration.
     *
     * @param configuration
     * @return
     */
    List<ResolvedDependency> getAllDependencies(String configuration){
        List<ResolvedDependency> allDeps = []
        project.configurations.getByName(configuration).resolvedConfiguration.firstLevelModuleDependencies.each{
            retrieveDependencies(it, configuration, allDeps)
        }
        return allDeps.unique()
    }

    /**
     * For each dependency, add all children in the given configuration (recursively)
     *
     * @param dependency
     * @param configuration
     * @param result
     */
    void retrieveDependencies(ResolvedDependency dependency, String configuration, List<ResolvedDependency> result){

        result.add(dependency)
        dependency.getChildren().each { child ->
            if (child.getConfiguration() == configuration || child.getConfiguration() == 'default') {
                retrieveDependencies(child, configuration, result)
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

    /**
     * Return a list of all the artifacts provided by Sonar.
     *
     * @return
     */
    List<ResolvedDependency> getSonarProvidedArtifacts(){
        List<ResolvedDependency> myList = []
        project.configurations.provided.resolvedConfiguration.getFirstLevelModuleDependencies().each{
            searchForSonarProvidedDependencies(it, myList, false)
        }
        project.configurations.compile.resolvedConfiguration.getFirstLevelModuleDependencies().each{
            searchForSonarProvidedDependencies(it, myList, false)
        }
        return myList.unique()
    }

    /**
     * Get a list of all dependencies not provided by Soanr.
     * @return
     */
    List<ResolvedDependency> getNotProvidedDependencies(){
        List<ResolvedDependency> result = []
        List<ResolvedDependency> providedArtifacts = getSonarProvidedArtifacts();

        List<ResolvedDependency> allDeps = getAllDependencies('compile')

        for (dep in allDeps) {
            boolean include = true
            if (isSonarPlugin(dep.module.id.toString())) {
                project.logger.debug("${dep.module.id.toString()} is a SonarQube plugin and will not be packaged in your plugin")
                include = false
            }
            if (containsDependency(providedArtifacts, dep)) {
                project.logger.debug(dep.name + " is provided by SonarQube plugin API and will not be packaged in your plugin")
                include = false
            }
            if (include) {
                result.add(dep)
            }
        }
        return result
    }

    /**
     * Searches the dependency tree adding any dependency matching 'org.codehaus.sonar'
     * and all its children to the sonarDeps list.
     *
     * @param dependency
     * @param sonarDeps
     * @param isParentProvided
     */
    void searchForSonarProvidedDependencies(ResolvedDependency dependency, List<ResolvedDependency> sonarDeps, boolean isParentProvided){
        if(dependency != null) {
            boolean provided
            if(dependency.getParents().findAll{ it.configuration.equals('compile')} != null){
                provided = isParentProvided || ("org.codehaus.sonar".equals(dependency.moduleGroup))
            }
            else{
                provided = isParentProvided
            }
            if (provided) {
                sonarDeps.add(dependency)
            }

            for (child in dependency.getChildren()) {
                if (child.getConfiguration().equals('compile')) {
                    searchForSonarProvidedDependencies(child, sonarDeps, provided)
                }
            }
        }
    }

    /**
     * Check a given list of dependencies for the presence of one, by comparing group, name, version.
     *
     * @param list
     * @param dep
     * @return
     */
    private boolean containsDependency(List<ResolvedDependency> list, ResolvedDependency dep){
        for(it in list){
            if(it.moduleGroup == dep.moduleGroup &&
                it.moduleName == dep.moduleName &&
                it.moduleVersion == dep.moduleVersion){return true}
        }
        return false
    }
}
