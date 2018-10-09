package de.eacg.ecs.plugin

import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.BasePlugin
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.misc.error
import de.eacg.ecs.client.Dependency
import de.eacg.ecs.client.JsonProperties
import de.eacg.ecs.client.RestClient
import de.eacg.ecs.client.Scan
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Main-method to run kobalt for debugging (uncomment for development)
 */
//fun main(args: Array<String>) {
//    com.beust.kobalt.main(args)
//}

/**
 * Main-class for executing the plugin.
 */
class EcsPlugin : BasePlugin() {
    private val emptyJsonObject = "{}"
    private val prefixPluginLog = "ecsScan =>"
    private val pluginProperties = PluginProperties()

    override val name = "ecs-kobalt-plugin"
    var config: PluginConfig? = null
    var apiClient: RestClient? = null

    /**
     * Collects all the dependencies for the given project and sends them to the TrustSource-API.
     * @param project project to scan
     * @return empty TaskResult
     */
    @Task(name = "dependency-scan", description = "Scans the project and sends the result to TrustSource", runBefore = arrayOf("compile"))
    fun dependencyScan(project: Project): TaskResult {
        if (config?.skip == true) {
            println("$prefixPluginLog Skipping execution")
            return TaskResult()
        }

        validateConfig()

        val rootDependency = DependencyTreeBuilder(project, verbose = config?.verbose ?: false).build()

        if (config?.verbose == true) printDependencies(listOf(rootDependency))

        if (config?.skipTransfer == true) {
            println("$prefixPluginLog Skipping transfer")
        } else {
            if (apiClient == null) apiClient = createApiClient()
            val projectId = "${project.group}:${project.name}"
            val scan = Scan(config?.projectName, project.name, projectId, rootDependency)
            transferScan(scan)
        }

        return TaskResult()
    }

    internal fun validateConfig() {
        if (config != null &&
                config?.projectName != null &&
                config?.apiPath != null &&
                config?.baseUrl != null &&
                ((config?.userName != null &&
                        config?.apiKey != null) ||
                        config?.credentials != null)) {
            return
        }
        error("No valid config: There are parameters missing")
        throw InvalidConfigurationException("No valid config: There are parameters missing")
    }

    private fun printDependencies(dependencies: Collection<Dependency>, level: Int = 0): Unit =
            dependencies.forEach { d: Dependency ->
                val sb = StringBuilder()
                for (i in 0..level) {
                    sb.append(" ")
                }
                println("$sb${d.name} - ${d.key}")
                val version = if (!d.versions.isEmpty()) d.versions.elementAt(0) else ""
                println("$sb     ${d.description}, $version, ${d.homepageUrl}")
                d.licenses.forEach {
                    println("$sb     ${it.name}, ${it.url}")
                }
                printDependencies(d.dependencies, level + 1)
            }

    private fun createApiClient(): RestClient {
        val apiClientConfig = createApiClientConfig()
        val userAgent = "${pluginProperties.name}/${pluginProperties.version}"
        return RestClient(apiClientConfig, userAgent)
    }

    internal fun createApiClientConfig(): JsonProperties {
        val apiConfig: JsonProperties
        try {
            apiConfig = if (config?.credentials != null) JsonProperties(config?.credentials) else createEmptyJsonProperties()
        } catch (e: IOException) {
            error("Evaluation of user credentials failed", e)
            throw RuntimeException("Exception while evaluating user credentials", e)
        }

        if (config?.userName != null) apiConfig.setUserName(config?.userName)
        if (config?.apiKey != null) apiConfig.setApiKey(config?.apiKey)
        if (config?.baseUrl != null) apiConfig.setBaseUrl(config?.baseUrl)
        if (config?.apiPath != null) apiConfig.setApiPath(config?.apiPath)

        val missingConfigKeys = apiConfig.validate()
        if (missingConfigKeys.isNotEmpty()) {
            throw InvalidConfigurationException("Missing keys for api-configuration: $missingConfigKeys") // TODO use reduce
        }

        return apiConfig
    }

    private fun createEmptyJsonProperties(): JsonProperties =
            JsonProperties(ByteArrayInputStream(emptyJsonObject.toByteArray(StandardCharsets.UTF_8)))

    internal fun transferScan(scan: Scan) {
        try {
            val body = apiClient?.transferScan(scan)
            println("$prefixPluginLog Response: code: ${apiClient?.responseStatus}, message: $body")

            if (apiClient?.responseStatus != 201) println("Failed : HTTP error code : ${apiClient?.responseStatus}")
        } catch (e: Exception) {
            error("Transfer failed", e)
        }
    }
}
