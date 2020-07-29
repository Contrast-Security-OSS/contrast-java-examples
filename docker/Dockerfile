# Base image is OpenJDK Java 11 JRE in Debian OS
FROM adoptopenjdk:11-jre-hotspot
# Install WebGoat runnable JAR
RUN mkdir /webgoat && curl -L -H "Accept: application/zip" https://github.com/WebGoat/WebGoat/releases/download/7.0.1/webgoat-container-7.0.1-war-exec.jar -o /webgoat/webgoat-container-7.0.1-war-exec.jar
# create directory for contrast working dir volume bind target
RUN mkdir /var/lib/contrast
# install dependencies for retrieving Contrast Java agent from Debian repository
# some inconsequential warning messages display in an alarming red text
#  2> /dev/null suppresses display of these to allay concern.
RUN apt-get -q update && apt-get -y -q install apt-utils 2> /dev/null
RUN apt-get -y -q install gnupg2 2> /dev/null
RUN curl https://pkg.contrastsecurity.com/api/gpg/key/public | apt-key add -
RUN echo "deb https://pkg.contrastsecurity.com/debian-public/ all contrast" | tee /etc/apt/sources.list.d/contrast-all.list
# install the Contrast Java agent
RUN apt-get -q update && apt-get -y -q install contrast-java-agent 2> /dev/null
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/opt/contrast/contrast-agent.jar", "-jar", "/webgoat/webgoat-container-7.0.1-war-exec.jar"]
