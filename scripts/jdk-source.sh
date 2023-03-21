#!/bin/bash

# Prepare JDK source code for manual inspection

jdk_url=https://github.com/adoptium/temurin17-binaries/releases/download

wget $jdk_url/jdk-17.0.6+10/OpenJDK17U-jdk-sources_17.0.6_10.tar.gz
tar xzf OpenJDK17U-jdk-sources_17.0.6_10.tar.gz

mv jdk-17.0.6+10-src jdk-17-src
find jdk-17-src \( -name aix -o -name bsd -o -name macosx -o -name windows \) -exec rm -rf {} +
rm -r jdk-17-src/{make,src/{demo,sample,utils},test}
