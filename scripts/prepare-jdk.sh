#!/bin/bash

jdk_url=https://github.com/adoptium/temurin17-binaries/releases/download
jdk_version=17.0.6+10
jdk_filename=17.0.6_10

# Download and extract Temurin JDK binary and source distributions.
wget $jdk_url/jdk-$jdk_version/OpenJDK17U-jdk-sources_$jdk_filename.tar.gz
tar xzf OpenJDK17U-jdk-sources_$jdk_filename.tar.gz

wget $jdk_url/jdk-$jdk_version/OpenJDK17U-jdk_x64_linux_hotspot_$jdk_filename.tar.gz
tar xzf OpenJDK17U-jdk_x64_linux_hotspot_$jdk_filename.tar.gz

# Remove non-Linux files from the source distribution.
mv jdk-$jdk_version-src jdk-17-src
find jdk-17-src \( -name aix -o -name bsd -o -name macosx -o -name windows \) -exec rm -rf {} +
rm -r jdk-17-src/{make,src/{demo,sample,utils},test}

# Prepare JRE with only java.* modules.
# This is used for exporting uncategorized src/main/resources/natives.tsv
# and running programs for dynamic analysis.
jre_modules=java.se,java.smartcardio,jdk.jdwp.agent
jdk-$jdk_version/bin/jlink --module-path jdk-$jdk_version/jmods --add-modules $jre_modules \
  --no-header-files --no-man-pages --output jre-17

# Create rt.jar and JARs for individual modules.
jdk-$jdk_version/bin/jimage extract --dir=jre-17/mods jre-17/lib/modules
for dir in jre-17/mods/*; do
  jar -c -f "$dir.jar" -C "$dir" .
  pushd "$dir" || exit
  zip -rq ../../lib/rt.jar ./*/ > /dev/null
  popd || exit
done
rm -r jre-17/mods/*/
