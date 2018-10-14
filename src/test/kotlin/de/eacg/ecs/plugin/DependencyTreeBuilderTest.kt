package de.eacg.ecs.plugin

import com.beust.kobalt.api.Project
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Unit-test for DependencyTreeBuilder.
 */
class DependencyTreeBuilderTest {
    private var project = Project()
    private var instance = DependencyTreeBuilder(project)

    @BeforeMethod
    fun before() {
        project = Project()
        instance = DependencyTreeBuilder(project)
    }

    @Test
    fun testBuild() {
        // basic attributes
        project.apply {
            name = "my-project"
            group = "com.example"
        }

        var result = instance.build()
        Assert.assertEquals("mvn:${project.group}:${project.name}", result.key)
        Assert.assertEquals(project.name, result.name)
        Assert.assertEquals(0, result.versions.size)
        Assert.assertEquals(0, result.licenses.size)
        Assert.assertEquals(0, result.dependencies.size)

        // full attributes
        fillProject(project, true)
        val license = project.pom?.licenses?.get(0)
        result = instance.build()
        Assert.assertEquals(project.description, result.description)
        Assert.assertEquals(1, result.versions.size)
        Assert.assertTrue(result.versions.contains(project.version))
        Assert.assertEquals(project.url, result.homepageUrl)
        Assert.assertEquals(project.pom?.scm?.url, result.repoUrl)
        Assert.assertEquals(2, result.licenses.size)
        var resultLicense = result.licenses.elementAt(0)
        Assert.assertEquals(license?.name, resultLicense.name)
        Assert.assertEquals(license?.url, resultLicense.url)
        Assert.assertEquals(0, result.dependencies.size)

        // third license
        val license2 = generateLicense(2)
        project.pom?.licenses?.add(license2)
        result = instance.build()
        Assert.assertEquals(3, result.licenses.size)
    }

    @Test
    fun testMapDependenciesWithoutPom() {
        val result = instance.mapDependencies(generateDependencies(1), 1)

        Assert.assertEquals(1, result.size)
        val resultDependency = result[0]
        Assert.assertEquals("mvn:my.group0:my-artifact0", resultDependency.key)
        Assert.assertEquals("my-artifact0", resultDependency.name)
        Assert.assertEquals(1, resultDependency.versions.size)
        Assert.assertTrue(resultDependency.versions.contains("5.0.0"))
        Assert.assertNull(resultDependency.description)
        Assert.assertNull(resultDependency.homepageUrl)
        Assert.assertNull(resultDependency.repoUrl)
        Assert.assertEquals(0, resultDependency.licenses.size)
    }

    @Test
    fun testMapDependenciesWithPom() {
        val pomModel = generatePomModel()
        val spyInstance = spy(instance) {
            on { getPomModel(any()) } doReturn pomModel
        }

        val result = spyInstance.mapDependencies(generateDependencies(1), 1)

        Assert.assertEquals(1, result.size)
        val resultDependency = result[0]
        Assert.assertEquals(pomModel.description, resultDependency.description)
        Assert.assertEquals(pomModel.url, resultDependency.homepageUrl)
        Assert.assertEquals(pomModel.scm.url, resultDependency.repoUrl)
        Assert.assertEquals(2, resultDependency.licenses.size)
        var resultLicence = resultDependency.licenses.elementAt(0)
        Assert.assertEquals(pomModel.licenses[0].name, resultLicence.name)
        Assert.assertEquals(pomModel.licenses[0].url, resultLicence.url)
        resultLicence = resultDependency.licenses.elementAt(1)
        Assert.assertEquals(pomModel.licenses[1].name, resultLicence.name)
        Assert.assertNull(resultLicence.url)
    }

    @Test
    fun testSkipDoubleEntries() {
        // skip double entries
        val dependency = generateDependency(0)

        var result = instance.mapDependencies(listOf(dependency, dependency), 1)
        Assert.assertEquals(1, result.size)

        // allow double entries
        instance = DependencyTreeBuilder(project, skipDoubleEntries = false)
        result = instance.mapDependencies(listOf(dependency, dependency), 1)
        Assert.assertEquals(2, result.size)
    }
}