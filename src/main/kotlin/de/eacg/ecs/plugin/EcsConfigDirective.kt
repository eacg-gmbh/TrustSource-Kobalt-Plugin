package de.eacg.ecs.plugin

import com.beust.kobalt.api.Kobalt
import com.beust.kobalt.api.annotation.Directive

/**
 * Directive to add a configuration for the plugin in the buildfile.
 */
@Directive
public fun ecsConfig(init: PluginConfig.() -> Unit) = PluginConfig().apply {
    init()
    (Kobalt.findPlugin("ecs-kobalt-plugin") as EcsPlugin).config = this
}
