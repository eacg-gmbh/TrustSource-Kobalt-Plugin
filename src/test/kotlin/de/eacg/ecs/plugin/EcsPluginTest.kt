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
    }

    @Test(expectedExceptions = [InvalidConfigurationException::class])
    fun testValidateConfigFails() {
        instance.config = PluginConfig()
        instance.validateConfig()
    }

    @Test
    fun testValidateConfig() {
        instance.config = generateConfig(false)
        instance.validateConfig()

        instance.config = generateConfig(true)
        instance.validateConfig()
    }

    @Test(expectedExceptions = [InvalidConfigurationException::class])
    fun testCreateApiConfigFails() {
        instance.config = PluginConfig()
        instance.createApiClientConfig()
    }

    @Test
    fun testCreateApiConfig() {
        instance.config = generateConfig(false)
        val result = instance.createApiClientConfig()

        val config = instance.config
        Assert.assertEquals(config?.baseUrl, result.getProperty("baseUrl"))
        Assert.assertEquals(config?.apiPath, result.getProperty("apiPath"))
        Assert.assertEquals(config?.userName, result.getProperty("userName"))
        Assert.assertEquals(config?.apiKey, result.getProperty("apiKey"))
    }
}