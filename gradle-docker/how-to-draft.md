# How to Use Contrast with Gradle and Docker

This how-to walks a Java developer through the necessary steps to add the
Contrast Java Agent and build a Docker container with the instrumented
application using the
[Application Plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
and the [Docker Plugin](https://github.com/bmuschko/gradle-docker-plugin). It
guides you though making changes to an
[example Grizzly project](https://github.com/Contrast-Security-OSS/contrast-java-examples/gradle-docker).
A completed version of the project
[exists in a git branch](https://github.com/Contrast-Security-OSS/contrast-java-examples/tree/gradle-docker-complete/gradle-docker).

Note that any part of the procedures described here that refer to any form of
packaging or distribution are meant for internal use. Please do not distribute
Contrast with your application or Docker container outside of your organization.
[See our License for more information](todo-link to license).

## Prerequisites

1. Install
   [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
1. Install [git](https://git-scm.com/)
1. Install [Docker](https://docs.docker.com/install/)
1. Obtain a [Contrast Account](https://app.contrastsecurity.com)

## Step 1 - Obtain Contrast Account Information

The Contrast Agent requires some configuration to communicate with the Contrast
UI. You will need to obtain these four properties:

1. The base URL of the Contrast API where the agent will send data
1. Your API key
1. The agent username for your organization
1. The service key for your organization's agent user

The API URL is `https://app.contrastsecurity.com/Contrast` or the URL of your
on-premises or private cloud instance. The other three properties can be
obtained from the Contrast UI in the **API** section of the **Organization
Settings** page.

## Step 2 - Configure Your Environment

If you're using a Unix like operating system, create a file for exporting the
above values as [Contrast environment variables](todo-link-to-open-docs).

Open a command prompt and run the following command, Replacing `<your_api_key>`,
`<agent_user_name>` and `<agent_user_service_key>` with the values you
[obtained from the Contrast UI](#step-1---obtain-contrast-account-information).
If you are using an on-premises or private cloud instance of Contrast UI,
replace the above URL with the correct one for your installation. Press `CTRL+D`
when you've finished typing all four lines of input.

```console
$ tee -a ~/.contrastrc > /dev/null
export CONTRAST__API__URL=https://app.contrastsecurity.com/Contrast
export CONTRAST__API__API_KEY=<your_api_key>
export CONTRAST__API__USER_NAME=<agent_user_name>
export CONTRAST__API__SERVICE_KEY=<agent_user_service_key>
```

Next run the `.contrastrc` script in the current shell to export the variables
to your environment:

```console
$ . ~/.contrastrc
```

If you're using Windows, refer to
[Microsoft's Documentation](https://docs.microsoft.com/en-us/windows/desktop/shell/user-environment-variables)
for how to set environment variables.

<details><summary><b>Configuration Tips</b></summary>
<p>You can
<a href="https://docs.contrastsecurity.com/installation-javaconfig.html">configure the Contrast Java agent</a>
using a file, Java system properties or environment variables. We recommend
using environment variables for credentials and Contrast UI connection
details. These values aren't likely to change across the projects in your local
development environment. And for the credentials, it's safer than putting them
directly in your build script.</p>

<p>If you close the terminal where you ran the <code>.contrastrc</code> script
you'll have to re-run it when you open a new terminal. You can make the
configuration persist across terminal sessions by adding `~/.contrastrc` to the
script that initializes your interactive shell. For example if your shell
program is Bash, you can do this by running
<code>echo ~/.contrastrc` | tee -a ~/.bashrc</code></p>
</details>

The rest of this how-to assumes that you'll execute further commands from the
same terminal as this step.

## Step 3 - Clone this Repository

Open a command prompt and run the following command to clone the examples
repository:

```console
$ git clone https://github.com/Contrast-Security-OSS/contrast-java-examples.git
```

Then make your working directory the root of the `gradle-docker` example:

```console
$ cd contrast-java-examples/gradle-docker
```

The rest of this how-to assumes that you'll execute subsequent commands with
`gradle-docker` as the working directory.

## Step 4 - Test and Build the Project

Check to make sure everything starts off in a working state by running the tests
for this module.

```console
$ ./gradlew check

BUILD SUCCESSFUL in 3s
4 actionable tasks: 3 executed, 1 up-to-date
```

On Windows run `gradlew.bat check` instead.

If this doesn't work check to make sure you have Java 8 correctly installed.

```console
$ java -version
java version "1.8.0_131"
Java(TM) SE Runtime Environment (build 1.8.0_131-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.131-b11, mixed mode)
```

If you had to correct something about your Java installation, try running the
tests again. If it still doesn't work please
[open an issue](https://github.com/Contrast-Security-OSS/contrast-java-examples/issues/new)
explaining the problem.

## Step 5 - Create an Integration Test

Create another source directory called `intTest` with the
`com.contrastsecurity.examples.grizzly` package structure:

```console
$ mkdir -p src/intTest/com/contrastsecurity/examples/grizzly
```

On Windows run `mkdir src/intTest/com/contrastsecurity/examples/grizzly`
instead.

Create an integration test class in the `com.contrastsecurity.examples.grizzly`
package called `AppIntegrationTest`. The code for this class is:

```java
package com.contrastsecurity.examples.grizzly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

final class AppIntegrationTest {
  @Test
  void it_echoes_a_message_within_ten_tries() throws Exception {
    final URL url = new URL("http://localhost:8080/echo?message=foo");

    for (int i = 0; i < 10; i++) {
      try (final InputStream responseStream = url.openStream();
          final Scanner scanner = new Scanner(responseStream)) {
        assertEquals("<body><h1>foo</h1></body>", scanner.nextLine());
        assertFalse(scanner.hasNext());
        return;
      } catch (final IOException e) {
        mustSleep(Duration.ofSeconds(1));
      }
    }

    fail("Failed to connect to application within 10 tries");
  }

  private static void mustSleep(final Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}

```

The contents of the `src` directory should now look like:

```
src
├── intTest
│   └── java
│       └── com
│           └── contrastsecurity
│               └── examples
│                   └── grizzly
│                       └── AppIntegrationTest.java
├── main
│   └── java
│       └── com
│           └── contrastsecurity
│               └── examples
│                   └── grizzly
│                       └── App.java
└── test
    └── java
        └── com
            └── contrastsecurity
                └── examples
                    └── grizzly
                        └── AppTest.java
```

## Step 6 - Enable Integration Testing

Configure a new source set and dependency configuration for the integration test
by adding the following to `build.gradle`:

```groovy
sourceSets {
    intTest {}
}

configurations {
    intTestImplementation.extendsFrom implementation
    intTestRuntimeOnly.extendsFrom runtimeOnly
}
```

Next, add the appropriate test dependencies to the `dependencies` block of
`build.gradle`:

```groovy
dependencies {
    // ... rest of dependencies omitted
    intTestImplementation "org.junit.jupiter:junit-jupiter-api:5.4.1"
    intTestRuntimeOnly "org.junit.platform:junit-platform-launcher:1.4.1"
    intTestImplementation "org.junit.jupiter:junit-jupiter-engine:5.4.1"
}
```

Then, add an integration test task to `build.gradle` and make it a dependency of
the `assemble` task.

```groovy
task integrationTest(type: Test) {
    description = "Test the application distributable."

    testClassesDirs = sourceSets.intTest.output.classesDirs
    classpath = sourceSets.intTest.runtimeClasspath

    useJUnitPlatform()

    // add task dependencies here in Step 7
}

assemble.dependsOn integrationTest
```

Finally, check that everything is working as intended to this point by building
the project:

```console
$ ./gradlew build
```

On Windows run `gradlew.bat build` instead.

Since, we haven't yet handled starting/stopping the application, the integration
test will fail. This is expected.

# Step 7 - Run the Application During Integration Tests Using Docker

Add the Docker Gradle Plugin to the `plugins` block of `build.gradle`:

```groovy
plugins {
    // ... other plugins omitted
    id "com.bmuschko.docker-remote-api" version "4.6.2"
}
```

Next, add the following `import` statements to `build.gradle`:

```groovy
import java.nio.file.Paths
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
```

Then, add the following tasks to `build.gradle` to create a Dockerfile, build an
image, create/start the container and remove the container:

```groovy
task createDockerfile(type: Dockerfile) {
    dependsOn assembleDist
    destFile = Paths.get("${buildDir}", "Dockerfile").toFile()
    from("openjdk:jre-alpine")
    workingDir("/")
    environmentVariable("CONTRAST_GRADLE_BIND_ADDR", "0.0.0.0")
    copyFile("distributions/${project.name}.zip", "dist.zip")
    runCommand("unzip dist.zip")
    workingDir("${project.name}")
    entryPoint("bin/${project.name}")
    exposePort(8080)
}

task buildDockerImage(type: DockerBuildImage) {
    dependsOn createDockerfile
    inputDir = buildDir
}

task createContainer(type: DockerCreateContainer) {
    dependsOn buildDockerImage
    targetImageId buildDockerImage.getImageId()
    portBindings = ['8080:8080']

    // add Contrast configuration here in Step 10
}

task startContainer(type: DockerStartContainer) {
    dependsOn createContainer
    targetContainerId createContainer.getContainerId()
}

task removeContainer(type: DockerRemoveContainer) {
    removeVolumes = true
    force = true
    targetContainerId startContainer.getContainerId()
}
```

Then, configure the `integrationTest` task created in
[Step 6](#step-6---enable-integration-testing) so that the Docker container is
started before the tests run and removed when they're finished:

```groovy
task integrationTest(type: Test) {
    // ... rest of task configuration omitted

    dependsOn startContainer
    finalizedBy removeContainer
}
```

Finally, build the module again:

```console
$ ./gradlew build
```

On Windows run `gradlew.bat build` instead.

Now, a Docker image is built as part of the the `assemble` task which is used to
start the application container before the integration test task. The
integration test should now pass.

## Step 8 - Download the Contrast Java Agent

First, add a dependency configuration and declare a dependency on the Contrast
Java Agent:

```groovy
configurations {
    contrastAgent
}

def contrast_version = "3.6.1"
def contrast_build = "7075"

dependencies {
    // ... other dependencies
    contrastAgent "com.contrastsecurity:java-agent:${contrast_version}.${contrast_build}"
}
```

Then, add a task for copying the agent into a directory of the project:

```groovy
task("copyAgent", type: Copy) {
    from configurations.contrastAgent
    into "${projectDir}/lib"
    rename "java-agent-*.*.*.jar", "java-agent-${contrast_version}.jar"
}
```

## Step 9 - Running the Application with Contrast

Add the `-javaagent` property to the JVM arguments passed to the application by
modifying the `application` block of `build.gradle`:

```groovy
application {
  // ... rest of block omitted
  def agentBuildPath = "lib/java-agent-${contrast_version}.jar"
  def agentProjectPath = Paths.get(getProjectDir().toURI()).resolve(agentBuildPath)


  applicationDefaultJvmArgs = [
          "-javaagent:${agentProjectPath.toString()}"
  ]
}
```

Now, run the application:

```console
$ ./gradlew run
```

On Windows run `gradlew.bat run` instead.

The application will now start up with Contrast. If you do any manual testing
here, any security related findings will be reported to the Contrast UI. You can
stop the application by pressing `CTRL+C`.

## Step 10 - Include Contrast in the Distributable

First, configure the Distribution Plugin to include the Contrast Agent jar in
the bundles that are created when you run gradle build (the Distribution Plugin
was included when you included the Application Plugin):

```groovy
application {

  // ... rest of application config omitted

  distributions {
    main {
      contents {
        from("${projectDir}/lib") {
            into "lib"
        }
      }
    }
  }
}
```

When Gradle generates the start scripts it will include the JVM args when
starting the application. However, `-javaagent` points to a jar file in your
project directory, not the jar file in the application bundle. Fix this by
configuring the startScripts task to perform some string surgery on the scripts
after they're generated:

```groovy
application {

  // ... rest of application config omitted

  startScripts {
    doLast {
      def shFile = new File(getOutputDir(), project.name)
      shFile.text = shFile.text.replace(agentProjectPath.toString(), "\$APP_HOME/${agentBuildPath}")
      def batFile = new File(getOutputDir(), "${project.name}.bat")
      batFile.text = batFile.text.replace(agentProjectPath.toString(), "%APP_HOME%\\lib\\java-agent-${contrast_version}.jar")
    }
  }
}
```

Next, pass the configuration variables into the Container by adding the
following to the `createContainer` task in `build.gradle`:

```groovy
task createContainer(type: DockerCreateContainer) {
    // ... rest of the config omitted

    envVars = [
        CONTRAST__API__URL: System.getenv("CONTRAST__API__URL"),
        CONTRAST__API__USER_NAME: System.getenv("CONTRAST__API__USER_NAME"),
        CONTRAST__API__SERVICE_KEY: System.getenv("CONTRAST__API__SERVICE_KEY"),
        CONTRAST__API__API_KEY: System.getenv("CONTRAST__API__API_KEY"),
        CONTRAST__APPLICATION__NAME: "${project.name}-how-to"
    ]
}
```

Finally, run the build again:

```console
./gradlew clean build
```

On Windows run `gradlew.bat clean build` instead.

Now the Docker container will run the application with Contrast enabled. When
the integration test runs, it will detect the vulnerable endpoint and report it
to the Contrast UI. To see the vulnerability report, login to the Contrast UI,
navigate to the vulnerabilities grid and filter your view by the application
name `gradle-docker-how-to`.
