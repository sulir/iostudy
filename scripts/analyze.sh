#!/bin/bash

: "${1?Missing argument: projects_file}"

while read -ru3 project; do
  dir="${project/\//__}"
  timeout -k1m 1h java -jar iostudy.jar static corpus "$dir"
done 3< "$1"
