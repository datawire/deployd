FROM datawire/ubuntu-java8:6d3e7adaa2
MAINTAINER Datawire <dev@datawire.io>
LABEL PROJECT_REPO_URL         = "git@github.com:datawire/cloud-service-pipeline-example.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/datawire/cloud-service-pipeline-example" \
      DESCRIPTION              = "Datawire Deployd" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io/"

# Install System Dependencies
#
#

# Set WORKDIR to /service which is the root of all our apps.
WORKDIR /service

# Install application dependencies
#
#

# COPY the app code and configuration into place then perform any final configuration steps.
COPY build/deployd-*-all.jar
     dev.yaml
     src/main/sh/entrypoint.sh
     .

EXPOSE 5100
ENTRYPOINT ["./entrypoint.sh"]
