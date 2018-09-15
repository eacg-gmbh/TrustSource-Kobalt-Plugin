import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.*
// import de.eacg.ecs.plugin.ecsConfig // uncomment for development

val bs = buildScript {
    repos()
    // uncomment for development
//    plugins(file("PATH_TO_LOCAL_PLUGIN_JAR"))
}

// uncomment for development
//val ecs = ecsConfig {
//    credentials = "~/.ecsrc.json"
//    projectName = "My Plugin Project"
//}

val p = project {

    name = "ecs-kobalt-plugin"
    group = "de.eacg"
    artifactId = name
    version = "0.1"

    sourceDirectories {
        path("src/main/kotlin")
    }

    sourceDirectoriesTest {
        path("src/test/kotlin")
    }

    dependencies {
        // dependency for production build
        compile("com.beust:kobalt-plugin-api:")
        // dependency for development and debugging
        // compile("com.beust:kobalt:")

        compile("de.eacg:ecs-java-client:0.1.0")
    }

    dependenciesTest {
        compile("org.testng:testng:6.10")
    }

    assemble {
        jar {
        }
    }
}
