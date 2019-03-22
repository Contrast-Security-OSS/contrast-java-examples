# How to Add Contrast to a Maven Project

This how-to walks a Java developer through the necessary steps to add the
Contrast Java Agent to an existing Maven project and use integration testing to
find vulnerabilities. It guides you though making changes to an
[example servlet project](https://github.com/Contrast-Security-OSS/contrast-java-examples/maven-cargo).
A completed version of the project
[exists in a git branch](https://github.com/Contrast-Security-OSS/contrast-java-examples/tree/maven-cargo-complete/maven-cargo).

## Prerequisites

1. Install
   [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
1. Install [git](https://git-scm.com/)
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

## Step 2 - Create a Contrast Configuration File

The Contrast configuration file is a [YAML file](https://yaml.org/). Since it
contains API credentials we recommend you create it in a location that only you
have access to. For most developers, somewhere in their home directory is a good
choice. Choose a filename that will make sense to you - e.g. `.contrast.yml`.

Create the file and open it in your preferred text editor. Paste in the
following contents:

```yaml
api:
  url: https://app.contrastsecurity.com/Contrast
  api_key: <your_api_key>
  user_name: <agent_user_name>
  service_key: <agent_user_service_key>
```

Replace `<your_api_key>`, `<agent_user_name>` and `<agent_user_service_key>`
with the values you
[obtained from the Contrast UI](#step-1---obtain-contrast-account-information).
If you are using an on-premises or private cloud instance of Contrast UI,
replace the above URL with the correct one for your installation.

Save the file.

<details><summary><b>Configuration Tip</b></summary>
<p>You can
<a href="https://docs.contrastsecurity.com/installation-javaconfig.html">configure the Contrast Java agent</a>
using a file, Java system properties or environment variables. For developers,
we recommend putting only the variables that are shared across multiple
applications (e.g. credentials and Contrast UI connection details) in this
file. That way you can manage them in one place. <b>Step 9</b> shows you how to
set application specific configurations.</p>
</details>

## Step 3 - Clone this Repository

Open a command prompt and run the following command to clone the examples
repository:

```console
$ git clone https://github.com/Contrast-Security-OSS/contrast-java-examples.git
Cloning into 'contrast-java-examples'...
remote: Enumerating objects: 12, done.
remote: Counting objects: 100% (12/12), done.
remote: Compressing objects: 100% (9/9), done.
remote: Total 12 (delta 1), reused 9 (delta 1), pack-reused 0
Unpacking objects: 100% (12/12), done.
```

Then make your working directory the root of the `maven-cargo` example:

```console
$ cd contrast-java-examples/maven-cargo
```

The rest of this how-to assumes that you've kept your command prompt open and
the working directory is the root of the `maven-cargo` example.

## Step 4 - Test and Build the Project

Check to make sure everything starts off in a working state by running the unit
tests.

```console
$ ./mvnw clean verify
[INFO] Scanning for projects...
[INFO]
[INFO] ---------< com.contrastsecurity.examples:contrast-maven-cargo >---------
[INFO] Building Contrast Maven Cargo Example 1.0
[INFO] --------------------------------[ war ]---------------------------------
[INFO]
    ... omitting some output ...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.467 s
[INFO] Finished at: 2019-03-21T22:57:22-04:00
[INFO] ------------------------------------------------------------------------
```

On Windows run `mvnw.cmd clean verify` instead.

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

Create an integration test class in the `com.contrastsecurity.examples.servlet`
package called `EchoServletIT`. The code for this class is:

```java
package com.contrastsecurity.examples.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

final class EchoServletIT {
  @Test
  void it_echoes_a_message() throws IOException {
    final URLConnection connection =
        new URL("http://localhost:8080/echo?message=foo").openConnection();

    try (final InputStream responseStream = connection.getInputStream();
        final Scanner scanner = new Scanner(responseStream)) {
      assertEquals("<body><h1>foo</h1></body>", scanner.nextLine());
      assertFalse(scanner.hasNext());
    }
  }
}
```

The contents of the `src/test` directory should now look like:

```
src/test
└── java
    └── com
        └── contrastsecurity
            └── examples
                └── servlet
                    ├── EchoServletIT.java
                    └── EchoServletTest.java
```

## Step 5 - Enable Integration Testing

Enable integration testing by configuring the
[Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/).
Add the following to the `build.plugins` element of the project POM (`pom.xml`):

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>2.22.1</version>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Run the tests again. You should an additional integration test phase. The
integration test should fail at this point because the application is never
started.

```console
$ ./mvnw clean verify
[INFO] Scanning for projects...
[INFO]
[INFO] ---------< com.contrastsecurity.examples:contrast-maven-cargo >---------
[INFO] Building Contrast Maven Cargo Example 1.0
[INFO] --------------------------------[ war ]---------------------------------
[INFO]
    ... omitting some output ...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  9.836 s
[INFO] Finished at: 2019-03-21T23:05:06-04:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-failsafe-plugin:2.22.1:verify (default) on project contrast-maven-cargo: There are test failures.
```

On Windows run `mvnw.cmd clean verify` instead.

## Step 6 - Manage the Application Server

We recommend using the
[Maven Cargo Plugin](https://codehaus-cargo.github.io/cargo/Maven2+plugin.html)
to manage the application server during integration tests. Add the following to
the `build.plugins` element of the project POM (`pom.xml`):

```xml
<plugin>
  <groupId>org.codehaus.cargo</groupId>
  <artifactId>cargo-maven2-plugin</artifactId>
  <version>1.7.3</version>
  <configuration>
    <container>
      <containerId>jetty9x</containerId>
    </container>
    <deployables>
      <deployable>
        <artifactId>contrast-maven-cargo</artifactId>
        <properties>
          <context>/</context>
        </properties>
      </deployable>
    </deployables>
  </configuration>
  <executions>
    <execution>
      <id>start-app-before-IT</id>
      <goals>
        <goal>start</goal>
      </goals>
      <phase>pre-integration-test</phase>
      <!-- Add Contrast Agent configuration here in Step 9. -->
    </execution>
    <execution>
      <id>stop-app-after-IT</id>
      <goals>
        <goal>stop</goal>
      </goals>
      <phase>post-integration-test</phase>
    </execution>
  </executions>
</plugin>
```

Run the tests again. This time the Cargo Plugin will start the application
before and stop it after the integration tests. All of the tests should now
pass.

```
$ ./mvnw clean verify
[INFO] Scanning for projects...
[INFO]
[INFO] ---------< com.contrastsecurity.examples:contrast-maven-cargo >---------
[INFO] Building Contrast Maven Cargo Example 1.0
[INFO] --------------------------------[ war ]---------------------------------
[INFO]
    ... omitting some output ...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  16.644 s
[INFO] Finished at: 2019-03-21T22:57:22-04:00
[INFO] ------------------------------------------------------------------------
```

On Windows run `mvnw.cmd clean verify` instead.

## Step 7 - Download the Contrast Agent

Use the
[Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/)
to download the agent into the project build directory.

First, add the following to the `properties` element of the project POM
(`pom.xml`):

```xml
<contrast.version>3.6.2</contrast.version>
<contrast.build>7445</contrast.build>
```

You can replace the version and build numbers with those from any Contrast Java
Agent
[released to Maven Central](https://search.maven.org/artifact/com.contrastsecurity/java-agent).

Then, add the following to the `build.plugins` element of the project POM
(`pom.xml`):

```xml
<plugin>
  <artifactId>maven-dependency-plugin</artifactId>
  <version>3.1.1</version>
  <executions>
    <execution>
      <phase>prepare-package</phase>
      <goals>
        <goal>copy</goal>
      </goals>
      <configuration>
        <artifactItems>
          <artifactItem>
            <groupId>com.contrastsecurity</groupId>
            <artifactId>java-agent</artifactId>
            <version>${contrast.version}.${contrast.build}</version>
            <destFileName>java-agent-${contrast.version}.jar</destFileName>
          </artifactItem>
        </artifactItems>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Step 9 - Enable the Contrast Agent

Modify the Maven Cargo Plugin configuration that was added in
[Step 6](#step-6---manage-the-application-server) by adding the following
`configuration` element to the `start-app-before-IT` execution:

```xml
<configuration>
  <configuration>
    <properties>
      <cargo.jvmargs>
        -javaagent:${project.build.directory}/dependency/java-agent-${contrast.version}.jar
        -Dcontrast.config.path=${user.home}/.contrast.yml
        -Dcontrast.application.name=maven-cargo-how-to
      </cargo.jvmargs>
    </properties>
  </configuration>
</configuration>
```

Change the value of `-Dcontrast.config.path` to match the path to the
configuration file you created in
[Step 2](#step-2---create-a-contrast-configuration-file).

Now, re-run the tests. When the application container initializes you'll see
output indicating that Contrast has started.

```
$ ./mvnw clean verify
[INFO] Scanning for projects...
[INFO]
[INFO] ---------< com.contrastsecurity.examples:contrast-maven-cargo >---------
[INFO] Building Contrast Maven Cargo Example 1.0
[INFO] --------------------------------[ war ]---------------------------------
[INFO]
    ... omitting some output ...
[INFO] [talledLocalContainer] [Contrast] Thu Mar 21 23:20:40 EDT 2019 Starting Contrast (build 3.6.2.BACKGROUND) Pat. 8,458,789 B2
    ... omitting more output...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  16.644 s
[INFO] Finished at: 2019-03-21T22:57:22-04:00
[INFO] ------------------------------------------------------------------------
```

On Windows run `mvnw.cmd clean verify` instead.

During the integration test, the agent will detect and report the vulnerable
servlet to the Contrast UI. To see the vulnerability report, login to the
Contrast UI, navigate to the vulnerabilities grid and filter your view by the
application name `maven-cargo-how-to`.
