ARG NEXUS_VERSION=3.16.1

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.16.1
ARG NEXUS_BUILD=02

COPY . /nexus-repository-conda/
RUN cd /nexus-repository-conda/; sed -i "s/3.16.1-02/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.16.1
ARG NEXUS_BUILD=02
ARG CONDA_VERSION=0.0.2
ARG TARGET_DIR=/opt/sonatype/nexus/system/org/sonatype/nexus/plugins/nexus-repository-conda/${CONDA_VERSION}/
USER root
RUN mkdir -p ${TARGET_DIR}; \
    sed -i 's@nexus-repository-maven</feature>@nexus-repository-maven</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-conda</feature>@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-maven"@<feature name="nexus-repository-conda" description="org.sonatype.nexus.plugins:nexus-repository-conda" version="0.0.2">\n        <details>org.sonatype.nexus.plugins:nexus-repository-conda</details>\n        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-conda/0.0.2</bundle>\n   </feature>\n    <feature name="nexus-repository-maven"@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-conda/target/nexus-repository-conda-${CONDA_VERSION}.jar ${TARGET_DIR}
USER nexus
