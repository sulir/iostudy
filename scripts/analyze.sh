#!/bin/bash

: "${1?Missing argument: projects_file}"

while read -ru3 project; do
  echo "---------- $project ----------"
  dir="${project/\//__}"
  timeout -k1m 1h java -mx14g -jar iostudy.jar static corpus "$dir"
done 3< "$1"
