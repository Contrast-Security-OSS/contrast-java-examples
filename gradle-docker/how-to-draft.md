# How to Use Contrast with Gradle and Docker

This how-to walks a Java developer through the necessary steps to add the
Contrast Java agent to an existing Gradle project. This example Gradle project
uses the
[Application Plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
and the [Docker Plugin](https://github.com/bmuschko/gradle-docker-plugin) to
build a Java web application and run JUnit 5 integration tests that verify the
web application's behavior. This article walks you through how to include
Contrast in the Docker image used for testing so that Contrast Assess analyzes
your code during integration testing. A completed version of the project
[exists in a git branch](https://github.com/Contrast-Security-OSS/contrast-java-examples/tree/gradle-docker-complete/gradle-docker).

Note that any part of the procedures described here that refer to any form of
packaging or distribution are meant for internal use. Please do not distribute
Contrast with your application or Docker container outside of your organization.
[See our License for more information](https://www.contrastsecurity.com/enduser-terms-0317a).

## Prerequisites

1. Install
   [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
1. Install [git](https://git-scm.com/)
1. Install [Docker](https://docs.docker.com/install/)
1. Obtain a [Contrast Account](https://app.contrastsecurity.com). If you do not
   have a Contrast account, create a
   [free Community Edition](https://www.contrastsecurity.com/contrast-community-edition)
   account.

## Step 1 - Obtain Contrast Account Information

The Contrast Agent requires some configuration to communicate with the Contrast
UI. You will need to obtain these four properties from Contrast UI:

1. Contrast URL
1. Your Contrast API key
1. The agent username for your Contrast organization
1. The service key for your Contrast organization's agent user

The Contrast URL is `https://app.contrastsecurity.com/Contrast` or the URL of
your on-premises or private cloud instance. The other three properties can be
obtained from the Contrast UI in the **API** section of the **Organization
Settings** page.

## Step 2 - Configure Your Environment

Contrast supports multiple means for configuring its agents. Since environment
variables work well with Docker containers, you will supply the Contrast agent's
configuration using environment variables.

If you're using a Unix like operating system, create a file for exporting the
above values as
[Contrast environment variables](https://docs.contrastsecurity.com/installation-javaconfig.html#java-yaml).

Open a command prompt and run the following command, replacing `<contrast_url>`,
`<your_api_key>`, `<agent_user_name>` and `<agent_user_service_key>` with the
values you
[obtained from the Contrast UI](#step-1---obtain-contrast-account-information).
Press `CTRL+D` when you've finished typing all four lines of input to save the
values to the `$HOME/.contrastrc` file.

```console
$ tee -a ~/.contrastrc > /dev/null
export CONTRAST__API__URL=<contrast_url>
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
$ ./gradlew build

BUILD SUCCESSFUL in 3s
4 actionable tasks: 3 executed, 1 up-to-date
```

On Windows run `gradlew.bat build` instead.

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

## Step 5 - Download the Contrast Java Agent

First, add a dependency configuration and declare a dependency on the Contrast
Java Agent:

```groovy
configurations {
    contrastAgent
}
```

```groovy
def contrast_version = "3.6.3.7830"

dependencies {
    // ... other dependencies omitted
    contrastAgent "com.contrastsecurity:contrast-agent:${contrast_version}"
}
```

Then, add a task for copying the agent into a directory of the project:

```groovy
task("copyAgent", type: Copy) {
    from configurations.contrastAgent
    into "${projectDir}/lib"
}

run.dependsOn copyAgent
assemble.dependsOn copyAgent
```

## Step 6 - Running the Application with Contrast

Add the `-javaagent` property to the JVM arguments passed to the application by
modifying the `application` block of `build.gradle`:

```groovy
application {
  // ... rest of block omitted
  def agentBuildPath = "lib/contrast-agent-${contrast_version}.jar"
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

## Step 7 - Include Contrast in the Distributable

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
      batFile.text = batFile.text.replace(agentProjectPath.toString(), "%APP_HOME%\\lib\\contrast-agent-${contrast_version}.jar")
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
