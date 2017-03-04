# Deployd

# User Testing Instructions

A Docker container can be started by executing `make docker-run`

A Docker container that boots into a shell session (plain ol' `/bin/sh`) can be accessed via `make docker-sh`

# Developer Instructions

Developers will likely be using a familiar Java IDE to interact with the codebase rather than relying on the Docker image.

## IntelliJ IDEA

Add a new `Application` Run/Debug Configuration `Run > Edit Configurations`. Then click the plus symbol to create a new Application configuration.

```text
Main-Class: io.vertx.core.Launcher
VM Options:
  -Dlogback.configurationFile=config/logback.xml
  -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory
Program Arguments: run io.datawire.deployd.ServiceVerticle -conf config/deployd.json
Working Directory: $MODULE_DIR$
Use Classpath of Module: deployd-web
```