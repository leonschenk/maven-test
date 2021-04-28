FROM jenkins/agent:latest

ARG MAVEN_VERSION="3.8.1"

RUN curl https://ftp.nluug.nl/internet/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o maven-bin.tar.gz && tar xzvf maven-bin.tar.gz && rm maven-bin.tar.gz

ENV PATH="${PATH}:${PWD}/apache-maven-${MAVEN_VERSION}/bin"
