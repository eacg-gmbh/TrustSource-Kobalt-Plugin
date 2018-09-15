package de.eacg.ecs.plugin

import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.BasePlugin
import com.beust.kobalt.api.IClasspathDependency
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

    override val name = "ecs-kobalt-plugin"
    var config: PluginConfig? = null

    /**
     * Collects all the dependencies for the given project and sends them to the TrustSource-API.
     * @param project project to scan
     */
    @Task(name = "dependency-scan", description = "Scans the project and sends the result to TrustSource", runBefore = arrayOf("compile"))
    fun dependencyScan(project: Project): TaskResult {
        if (config?.skip == true) {
            println("$prefixPluginLog Skipping execution")
            return TaskResult()
        }

        validateConfig()

        val apiClientConfig = createApiClientConfig()
        val userAgent = "${project.name}/${project.version}"
        val projectId = "${project.group}:${project.name}"
        val dependencies = mapDependencies(project.compileDependencies)

        if (config?.verbose == true) printDependencies(dependencies)

        if (config?.skipTransfer == true) {
            println("$prefixPluginLog Skipping transfer")
        } else {
            val apiClient = RestClient(apiClientConfig, userAgent)
            val scan = Scan(config?.projectName, project.name, projectId, dependencies)
            transferScan(apiClient, scan)
        }

        return TaskResult()
    }

    private fun validateConfig() {
        // TODO: implement
    }

    private fun createApiClientConfig(): JsonProperties {
        val apiConfig: JsonProperties
        try {
            apiConfig = if (config?.credentials != null) JsonProperties(config?.credentials) else createEmptyJsonProperties()
        }
        catch (e: IOException) {
            error("Evaluation of user credentials failed", e)
            throw RuntimeException("Exception while evaluating user credentials", e)
        }

        if (config?.userName != null) apiConfig.setUserName(config?.userName)
        if (config?.apiKey != null) apiConfig.setApiKey(config?.apiKey)
        if (config?.baseUrl != null) apiConfig.setBaseUrl(config?.baseUrl)
        if (config?.apiPath != null) apiConfig.setApiPath(config?.apiPath)
        return apiConfig
    }

    private fun createEmptyJsonProperties(): JsonProperties =
        JsonProperties(ByteArrayInputStream(emptyJsonObject.toByteArray(StandardCharsets.UTF_8)))

    private fun mapDependencies(dependencies: List<IClasspathDependency>): List<Dependency> {
        // TODO: just basic implementation, needs to collect more information (sub-dependencies)
        return dependencies.filter { it.isMaven }.map {
            val mvnIt = it.toMavenDependencies()
            val builder = Dependency.Builder()
            builder.setKey("mvn:${mvnIt.groupId}:${mvnIt.artifactId}")
            builder.setName(mvnIt.artifactId)
            builder.addVersion(mvnIt.version)
            builder.buildDependency()
        }
    }

    private fun printDependencies(dependencies: List<Dependency>) {
        // TODO: implement
    }

    private fun transferScan(client: RestClient, scan: Scan) {
        try {
            val body = client.transferScan(scan)
            println("$prefixPluginLog Response: code: ${client.responseStatus}, message: $body")

            if (client.responseStatus != 201) println("Failed : HTTP error code : ${client.responseStatus}")
        } catch(e: Exception) {
            error("Transfer failed", e)
        }
    }
}