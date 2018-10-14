package de.eacg.ecs.plugin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.eacg.ecs.client.RestClient
import de.eacg.ecs.client.Scan
import org.mockito.ArgumentCaptor
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Integration-test for the EcsPlugin with mocked transfer
 */
class EcsPluginIT {
    private var instance = EcsPlugin()
    private var restClient = mock<RestClient>()

    @BeforeMethod
    fun before() {
        instance = EcsPlugin()
        instance.config = generateConfig(false)
        restClient = mock()
        instance.apiClient = restClient
    }

    @Test
    fun testEcsPlugin() {
        val project = generateProject(true)
        project.compileDependencies.addAll(generateDependencies(3))
        val firstDependency = project.compileDependencies[0]
        project.compileDependencies.add(firstDependency)
        val subDependencies = generateDependencies(4)
        whenever(firstDependency.directDependencies()).thenReturn(subDependencies)

        val result = instance.dependencyScan(project)

        Assert.assertNotNull(result)

        val captor = ArgumentCaptor.forClass(Scan::class.java)
        verify(restClient).transferScan(captor.capture())
        val capturedScan = captor.value

        Assert.assertEquals(instance.config?.projectName, capturedScan.project)
        Assert.assertEquals(project.name, capturedScan.module)
        Assert.assertEquals("${project.group}:${project.name}", capturedScan.moduleId)
        Assert.assertEquals(1, capturedScan.dependencies.size)

        val rootDependency = capturedScan.dependencies[0]
        Assert.assertEquals("mvn:${project.group}:${project.name}", rootDependency.key)
        Assert.assertEquals(project.name, rootDependency.name)
        Assert.assertEquals(project.description, rootDependency.description)
        Assert.assertEquals(1, rootDependency.versions.size)
        Assert.assertTrue(rootDependency.versions.contains(project.version))
        Assert.assertEquals(project.url, rootDependency.homepageUrl)
        Assert.assertEquals(project.pom?.scm?.url, rootDependency.repoUrl)
        Assert.assertEquals(2, rootDependency.licenses.size)
        var resultLicense = rootDependency.licenses.elementAt(0)
        Assert.assertEquals(project.pom?.licenses?.get(0)?.name, resultLicense.name)
        Assert.assertEquals(project.pom?.licenses?.get(0)?.url, resultLicense.url)
        Assert.assertEquals(3, rootDependency.dependencies.size)

        // double dependencies are removed
        var hasSubDependencies = false
        rootDependency.dependencies.forEach {
            if (it.dependencies.size == 1) hasSubDependencies = true
        }
        Assert.assertTrue(hasSubDependencies)
    }
}