package de.eacg.ecs.plugin

import com.beust.kobalt.api.Project
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm
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
        val license = License().apply {
            name = "MIT"
            url = "http://example.com/mit"
        }
        project.apply {
            description = "This is the best testcase ever"
            url = "http://example.com"
            version = "1.0"
            pom = Model().apply {
                licenses = listOf(license)
                scm = Scm().apply {
                    url = "http://example.com/repo.git"
                }
            }
        }

        result = instance.build()
        Assert.assertEquals(project.description, result.description)
        Assert.assertEquals(1, result.versions.size)
        Assert.assertTrue(result.versions.contains(project.version))
        Assert.assertEquals(project.url, result.homepageUrl)
        Assert.assertEquals(project.pom?.scm?.url, result.repoUrl)
        Assert.assertEquals(1, result.licenses.size)
        var resultLicense = result.licenses.elementAt(0)
        Assert.assertEquals(license.name, resultLicense.name)
        Assert.assertEquals(license.url, resultLicense.url)
        Assert.assertEquals(0, result.dependencies.size)

        // second license
        val license2 = License().apply {
            name = "MIT2"
            url = "http://example.com/mit2"
        }
        project.pom?.licenses = listOf(license, license2)
        result = instance.build()
        Assert.assertEquals(2, result.licenses.size)
    }

    @Test
    fun testMapDependencies() {
        // TODO
    }

}