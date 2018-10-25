# TrustSource-Kobalt-Plugin
TrustSource (https://www.trustsource.io) is a legal resolver and OpenChain compliant workflow engine that allows you to manage your open source dependencies, provide legal compliance and create bill of materials.

The Kobalt plugin provides TrustSource integration with theh [Kobalt](https://github.com/cbeust/Kobalt) build tool. It uses the [TrustSource Java client](https://github.com/eacg-gmbh/ecs-java-client/) to transfer package manager based dependency information to TrustSource-Server via its REST-API. 

There are also several other plugins available to integrate with different build tools.

Please see the following links for more details on the corresponding package manager: 
* [Maven (Java)](https://github.com/eacg-gmbh/ecs-mvn-plugin)
* [Gradle (Java)](https://github.com/eacg-gmbh/ecs-gradle-plugin)
* [Kobalt (Java)](https://github.com/eacg-gmbh/TrustSource-Kobalt-Plugin)
* [Ant/Ivy (Java)](https://github.com/eacg-gmbh/TrustSource-Ant-Plugin)
* [Node (JScript)](https://github.com/eacg-gmbh/ecs-node-client)
* [Grunt (JScript)](https://github.com/eacg-gmbh/ecs-grunt-plugin) could also be used for gulp ([see here](https://support.trustsource.io/hc/en-us/articles/115003209085-How-to-integrate-TrustSource-with-npm-via-gulp))
* [PIP (Python)](https://github.com/eacg-gmbh/ecs-pip-plugin)
* [Bundler (Ruby)](https://github.com/eacg-gmbh/ecs-bundler)
* [Composer (PHP)](https://github.com/eacg-gmbh/ecs-composer)
* [SPM (Swift)](https://github.com/eacg-gmbh/ecs-spm-plugin)
* [nuget (.NET)](https://github.com/eacg-gmbh/ecs-nuget)

# Quick Setup
It is pretty simple to include the TrustSource scan into your existing Kobalt projects. Make use of the ecs-kobalt-plugin by declaring it in your Build-file under the ``buildScript`` directive in the ``plugins`` section as seen below.

```
val bs = buildScript {
    ...
    plugins("de.eacg:ecs-kobalt-plugin:0.1")
}
```

Then configure the plugin with your security credentials by using the ``ecsConfig`` directive in your Build-file.

```
val ecs = ecsConfig {
    apiKey = "YOUR API KEY"
    userName = "YOUR LOGIN NAME (email)"
    projectName = "YOUR PROJECT NAME"
}
```

Finally you can execute Kobalt with the goal ``dependency-scan`` to scan your project and upload the result to TrustSource: ``./kobaltw dependency-scan``

# Advanced Setup
## Use properties file for credentials
The Kobalt-plugin is able to read the TrustSource access credentials (userName, apiKey) from a properties file in JSON format. This allows sharing of the TrustSource credentials with other projects and also with other build tools.

**properties file ‘ecs-settings.json’ in your home directory:**

```
{
    "userName": "YOUR LOGIN NAME (email)",
    "apiKey": "YOUR API KEY"
}
```

Adjust the configuration of the Kobalt-plugin by specifying an additional credentials variable under the ``ecsConfig`` directive. In the variable define the path to your properties file and the Kobalt-plugin will then read the properties from this file. The tilde, ‘~’, represents your user home directory, the dot, ‘.’ stands for the current working directory and forward slashes ‘/’ are used to separate sub-directories.

**The modified Build-file:**

```
val ecs = ecsConfig {
    credentials = "~/ecs-settings.json"
    projectName = "YOUR PROJECT NAME"
}
```

# Reference
All configuration properties

* *credentials* (Optional): Path to a JSON file which holds ‘userName’ and ‘apiKey’ credentials. Use ‘~’ as shortcut to your home directory and ‘.’ for the current working directory. A forward slash ‘/’ separates directories. *Default:* apiKey and userName are expected to be set in the plugin configuration

* *apiKey* (Required, if not specified in credentials file): This key permits the access to TrustSource. Create or retrieve the key from your profile settings of the ECS web application.
        
* *userName* (Required, if not specified in credentials file.): Identifies the initiator of the data transfer.
    
* *projectName* (Required): For which project is the dependency information transferred.
    
* *skip* (Optional): Set to true to disable the ecs-kobalt-plugin. *Default:* false
    
* *skipTransfer* (Optional): Set to true to execute a dry run and do not transfer anything. *Default:* false

* *verbose* (Optional): Increases the output produced by the plugin to get additional information. *Default:* false

# How to obtain a TrustSource API Key
TrustSource provide a free version. You may tregister and select the egar wheel on the upper right side and select API keys from the menu. Then select API-Key and generate the key. Paste user & API key into your local settings file and run your scan. Be compliant ;-)

# How to obtain Support
Write us an email to support@trustsurce.io. We will be happy to hear from you. Or visit our knowledgebase at https://support.trustsource.io for more insights and tutorials.
