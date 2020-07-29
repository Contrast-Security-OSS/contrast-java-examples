# Contrast Java Agent, Docker Example

This module contains example code to compliment the
[Install the Agent in Docker article](https://docs.contrastsecurity.com/en/docker.html)
which demonstrates how to add the Contrast Java agent to an application deployed in Docker.

The example application used is WebGoat 7 deployed as a self-hosted runnable JAR.  The
JAR file can be downloaded here [WebGoat 7 Runnable Jar](https://github.com/WebGoat/WebGoat/releases/download/7.0.1/webgoat-container-7.0.1-war-exec.jar)

The agent is configured primarily through the use of environment variables specified at
run time in run.sh, but a YAML configuration file specifying additional configurations
and made accessible to the agent via a volume bind-mount is also used.

To try it out, run.sh will need to be modified to provide appropriate authentication 
configurations, and the API URL specified in the contrast_security.yaml file should be 
updated as well.  In a BASH shell on a docker enabled host system, run build.sh to create the
docker image, and run run.sh to start the container.  

Run.sh will pipe the container application standard output to the console on
the host, but it could easily be modified to save the output to a log file on the host.

https://docs.contrastsecurity.com/en/docker.html
