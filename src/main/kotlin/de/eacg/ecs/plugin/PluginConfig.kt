package de.eacg.ecs.plugin

/**
 * Holds values for plugin configuration.
 */
class PluginConfig(var credentials: String? = null,
                   var apiKey: String? = null,
                   var userName: String? = null,
                   var projectName: String? = null,
                   var skip: Boolean = false,
                   var skipTransfer: Boolean = false,
                   var verbose: Boolean = false,
                   var proxyUrl: String? = null,
                   var proxyPort: String? = "8080",
                   var proxyUser: String? = null,
                   var proxyPass: String? = null,
                   var baseUrl: String = "https://app.trustsource.io",
                   var apiPath: String = "/api/v1")