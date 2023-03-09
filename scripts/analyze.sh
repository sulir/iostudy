#!/bin/bash

: "${1?Missing argument: static|info}"
kind=$1

[ ! "$JAVA_HOME" ] && echo "Please set JAVA_HOME to the custom JRE from prepare-jdk.sh." && exit 1
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

while read -ru3 project; do
  echo "---------- $project ----------"
  dir="${project/\//__}"
  timeout -k1m 1h java -mx14g -jar iostudy.jar "$kind" corpus "$dir"
done 3< projects.txt
