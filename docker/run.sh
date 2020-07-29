#! /bin/bash

# Replace <NEEDS_TO_BE_SET> values with appropriate authentication values.
# Also, update the API URL value in contrast_security.yaml as necessary.
CONTAINER_ID="$(docker run \
 --rm \
 -v "$(pwd)"/contrast_working_dir:/var/lib/contrast \
 -e CONTRAST__AGENT__CONTRAST_WORKING_DIR=/var/lib/contrast \
 -e CONTRAST__AGENT__LOGGER__STDOUT=true \
 -e CONTRAST__API__API_KEY=<NEEDS_TO_BE_SET> \
 -e CONTRAST__API__SERVICE_KEY=<NEEDS_TO_BE_SET> \
 -e CONTRAST__API__USER_NAME=<NEEDS_TO_BE_SET> \
 -e CONTRAST_CONFIG_PATH=/var/lib/contrast/contrast_security.yaml \
 -e CONTRAST__AGENT__JAVA__STANDALONE_APP_NAME=webgoat7-app \
 -p 8080:8080 \
 -d \
 contrast-docker-example:v1)"
docker logs --details --follow $CONTAINER_ID
