#!/bin/sh

VN=1.3.8.4

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
    -DrepositoryId=sonatype-nexus-staging -DuseAgent=true \
    -DpomFile=pom.xml -Dfile=target/org.semanticweb.hermit-$VN.jar

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
    -DrepositoryId=sonatype-nexus-staging -Dclassifier=javadoc \
    -DuseAgent=true \
    -DpomFile=pom.xml -Dfile=target/org.semanticweb.hermit-$VN-javadoc.jar

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
    -DrepositoryId=sonatype-nexus-staging -Dclassifier=sources \
    -DuseAgent=true \
    -DpomFile=pom.xml -Dfile=target/org.semanticweb.hermit-$VN-sources.jar
