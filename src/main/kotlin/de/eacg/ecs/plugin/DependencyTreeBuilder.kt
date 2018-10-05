package de.eacg.ecs.plugin

import com.beust.kobalt.api.IClasspathDependency
import com.beust.kobalt.api.Project
import com.beust.kobalt.maven.aether.AetherDependency
import com.beust.kobalt.misc.warn
import de.eacg.ecs.client.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Takes the project and builds the dependency tree until a given level.
 * @param project Kobalt-project for which the dependency tree should be built
 * @param level the dept of the dependency tree (0 = all dependencies, default)
 */
class DependencyTreeBuilder(val project: Project, var level: Int = 0) {

    /**
     * Builds the dependency tree for the given project.
     * @return root dependency of the tree
     */
    fun build(): Dependency {
        val builder = Dependency.Builder()
        builder.setKey("mvn:${project.group}:${project.name}")
        builder.setName(project.name)
        if (project.version != null) builder.addVersion(project.version)
        builder.setDescription(project.description)
        builder.setHomepageUrl(project.url)
        builder.setRepoUrl(project.pom?.scm?.url)
        project.pom?.licenses?.forEach {
            builder.addLicense(it.name, it.url)
        }

        if (level == 0 || level > 1) {
            mapDependencies(project.compileDependencies, 1).forEach {
                builder.addDependency(it)
            }
        }

        return builder.buildDependency()
    }

    internal fun mapDependencies(dependencies: List<IClasspathDependency>, currentLevel: Int): List<Dependency> =
            dependencies.filter { it.isMaven }.map {
                val mvnIt = it.toMavenDependencies()
                val builder = Dependency.Builder()
                builder.setKey("mvn:${mvnIt.groupId}:${mvnIt.artifactId}")
                builder.setName(mvnIt.artifactId)
                builder.addVersion(mvnIt.version)

                if (it is AetherDependency) {
                    val pomModel = getPomModel(it)
                    if (pomModel != null) {
                        builder.setDescription(pomModel.description)
                        builder.setHomepageUrl(pomModel.url)
                        builder.setRepoUrl(pomModel.scm?.url)
                        pomModel.licenses?.forEach {
                            if (it.name != null && it.url != null) {
                                builder.addLicense(it.name, it.url)
                            } else if (it.name != null) {
                                builder.addLicense(it.name)
                            }
                        }
                    }
                }

                val nextLevel = currentLevel + 1
                if (level == 0 || level > nextLevel) {
                    mapDependencies(it.directDependencies(), nextLevel).forEach { dependency: Dependency ->
                        builder.addDependency(dependency)
                    }
                }

                builder.buildDependency()
            }

    internal fun getPomModel(dependency: AetherDependency): Model? {
        val jarFile = dependency.aether.resolve(dependency.artifact).root?.artifact?.file
        val pomFilePath = jarFile?.absolutePath?.replaceAfterLast('.', "pom")
        if (Files.exists(Paths.get(pomFilePath))) {
            try {
                val reader = MavenXpp3Reader()
                val fileReader = FileReader(pomFilePath)
                return reader.read(fileReader)
            }
            catch (e: java.lang.Exception) {
                warn("Exception during pom parsing", e)
            }
        }
        return null
    }

}