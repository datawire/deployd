FROM datawire/base-ubuntu:f57de33dfe
MAINTAINER Datawire <dev@datawire.io>
LABEL PROJECT_REPO_URL         = "git@github.com:datawire/cloud-service-pipeline-example.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/datawire/cloud-service-pipeline-example" \
      DESCRIPTION              = "Datawire Hello" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io/"

#
# README
# ------
#
# This is a Docker image of Ubuntu with Python 2.7, uwsgi, and nginx installed.
#
# This Dockerfile is structured to optimally use the Docker cache as much as possible without being inflexible. The
# general order of events is:
#
# 1. Install OS dependencies which do not change too much
# 2. Install application dependencies (e.g. Python requirements.txt)
# 3. Install the application code
# 4. Configure things that need to be configured (e.g. nginx)
#
# The service and configuration will be installed into /datawire/config and /datawire/<module>. If you modify <module>
# in your program source tree (e.g. rename 'hello' to 'foobar' then update config/uwsgi.ini to the new module name.
#
# ASK if you do not know the answer before blindly hacking up this file!
#

# Install System dependencies
#
# NOTE: build-essential and python-dev MUST BE installed because we need them for uwsgi which is a C source distribution
#       that needs to be built before use.
#
# NOTE: DO NOT remove the uwsgi install from below. It's better to let it be compiled and installed during the system
#       setup process because compilation is slow. If it's moved to requirements.txt then everytime a dependency is
#       modified uwsgi will be recompiled as well.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
            nginx \
            build-essential \
            python-dev && \
    pip install -U uwsgi==2.0.14 && \
    rm -f /etc/nginx/sites-enabled/default && \
    rm -f /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Set WORKDIR to /datawire which is the root of all our apps then COPY only requirements.txt to avoid screwing up Docker
# caching and causing a full reinstall of all dependencies when dependencies are not changed.
WORKDIR /datawire
COPY requirements.txt .

# Install application dependencies
RUN pip install -r requirements.txt

# COPY the app code and configuration into place then perform any final configuration steps.
COPY . ./
RUN ln -sf /datawire/config/hello.conf /etc/nginx/sites-enabled/ && \
    chmod +x entrypoint.sh

EXPOSE 5100
ENTRYPOINT ["./entrypoint.sh"]