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
                   var baseUrl: String = "https://ecs-app.eacg.de",
                   var apiPath: String = "/api/v1")