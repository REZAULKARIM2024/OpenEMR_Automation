# OpenEMR_Automation - containerized test run
#
# Build:  docker build -t openemr-automation .
# Run (full suite, headless):
#   docker run --rm openemr-automation
# Run a specific suite:
#   docker run --rm openemr-automation mvn test -DsuiteXmlFile=testng-smoke.xml -Dheadless=true
#
# Chrome runs headless inside the container, so no host display/VNC is
# required. WebDriverManager downloads a matching chromedriver at run time.

FROM maven:3.9.6-eclipse-temurin-11

# Install Google Chrome (stable) for headless execution.
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget gnupg ca-certificates \
    && wget -q -O /tmp/chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get install -y --no-install-recommends /tmp/chrome.deb \
    && rm -rf /tmp/chrome.deb /var/lib/apt/lists/*

WORKDIR /workspace

# Warm the local Maven repository with project dependencies before copying
# source, so source-only changes don't invalidate the dependency cache layer.
COPY pom.xml .
RUN mvn -q dependency:go-offline || true

COPY . .

ENV CI=true
ENTRYPOINT ["mvn", "-Dheadless=true", "test"]
