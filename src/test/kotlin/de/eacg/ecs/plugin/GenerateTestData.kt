package de.eacg.ecs.plugin

import com.beust.kobalt.api.IClasspathDependency
import com.beust.kobalt.api.Project
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.maven.model.Dependency
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm

fun generateProject(withPomModel: Boolean = true): Project = fillProject(Project(), withPomModel)

fun fillProject(project: Project, withPomModel: Boolean = true): Project =
        project.apply {
            name = "my-project"
            group = "com.example"
            description = "This is the best project ever"
            url = "http://example.com"
            version = "3.0"
            if (withPomModel) pom = generatePomModel()
        }

fun generatePomModel(amountLicenses: Int = 2): Model =
    Model().apply {
        artifactId = "my-artifact"
        groupId = "my.group"
        version = "2.0"
        description = "My pom description"
        url = "http://example.com/pom.xml"
        licenses = generateLicenses(amountLicenses)
        scm = Scm().apply {
            url = "http://example.com/repo.git"
        }
    }

fun generateLicenses(amount: Int): List<License> {
    val result = ArrayList<License>()
    for (i in 0 until amount) {
        result.add(generateLicense(i))
    }
    return result
}

fun generateLicense(i: Int): License =
        License().apply {
            name = "MIT$i"
            if (i % 2 == 0) url = "http://example.com/mit$i"
        }

fun generateDependencies(amount: Int): List<IClasspathDependency> {
    val result = ArrayList<IClasspathDependency>()
    for (i in 0 until amount) {
        result.add(generateDependency(i))
    }
    return result
}

fun generateDependency(i: Int): IClasspathDependency {
    val mvnDependency = mock<Dependency> {
        on { groupId } doReturn "my.group$i"
        on { artifactId } doReturn "my-artifact$i"
        on { version } doReturn "5.0.$i"
    }
    return mock {
        on { isMaven } doReturn true
        on { toMavenDependencies() } doReturn mvnDependency
    }
}

fun generateConfig(useExternalCredentials: Boolean): PluginConfig = fillConfig(PluginConfig(), useExternalCredentials)

fun fillConfig(config: PluginConfig, useExternalCredentials: Boolean): PluginConfig =
        config.apply {
            projectName = "My Project"
            if (useExternalCredentials) {
                credentials = "~/ecs-credentials.json"
            } else {
                userName = "test@example.com"
                apiKey = "1234"
            }
        }

