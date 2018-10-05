package de.eacg.ecs.plugin

import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Unit-tests for EcsPlugin-class.
 */
class EcsPluginTest {
    private var instance: EcsPlugin = EcsPlugin()

    @BeforeMethod
    fun before() {
        instance = EcsPlugin()
        instance.config = PluginConfig()
    }

    @Test(expectedExceptions = [InvalidConfigurationException::class])
    fun testValidateConfigFails() {
        instance.validateConfig()
    }

    @Test
    fun testValidateConfig() {
        fillConfig()
        instance.validateConfig()

        instance.config = PluginConfig().apply {
            projectName = "My Project"
            credentials = "credentials-file.json"
        }
        instance.validateConfig()
    }

    @Test(expectedExceptions = [InvalidConfigurationException::class])
    fun testCreateApiConfigFails() {
        instance.createApiClientConfig()
    }

    @Test
    fun testCreateApiConfig() {
        fillConfig()
        val result = instance.createApiClientConfig()

        val config = instance.config
        Assert.assertEquals(config?.baseUrl, result.getProperty("baseUrl"))
        Assert.assertEquals(config?.apiPath, result.getProperty("apiPath"))
        Assert.assertEquals(config?.userName, result.getProperty("userName"))
        Assert.assertEquals(config?.apiKey, result.getProperty("apiKey"))
    }

    @Test
    fun testTransferScan() {
        // TODO
    }

    private fun fillConfig() {
        instance.config?.apply {
            projectName = "My Project"
            userName = "test@example.com"
            apiKey = "1234"
        }
    }
}