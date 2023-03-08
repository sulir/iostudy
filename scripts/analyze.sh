#!/bin/bash

: "${1?Missing argument: static|info}"
kind=$1

while read -ru3 project; do
  echo "---------- $project ----------"
  dir="${project/\//__}"
  timeout -k1m 1h java -mx14g -jar iostudy.jar "$kind" corpus "$dir"
done 3< projects.txt
