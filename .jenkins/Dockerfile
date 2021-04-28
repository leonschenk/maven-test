FROM jenkins/agent:latest

ARG MAVEN_VERSION="3.8.1"

# Install maven 3 (version from argument MAVEN_VERSION)
# 1) download from mirror
# 2) unpack
# 3) remove zipped file
RUN curl https://ftp.nluug.nl/internet/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o maven-bin.tar.gz && tar xzvf maven-bin.tar.gz && rm maven-bin.tar.gz

# 4) include maven binary directory in PATH
ENV PATH="${PATH}:/home/jenkins/apache-maven-${MAVEN_VERSION}/bin"
