#!/bin/bash

: "${1?Missing argument: dacapo_jar}"
dacapo=$1

benchmarks=$(java -jar "$dacapo" -l 2>&1 | tail -1)
port=8000
debugger=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:$port
db_dir=$(basename "$dacapo")

for benchmark in $benchmarks; do
  echo "---------- $benchmark ----------"
  java -jar "$dacapo" --sizes "$benchmark" 2>&1 | tail -1 | grep -q small && size=(-s small)
  java -jar iostudy.jar dynamic "$benchmark" $port "$db_dir" &
  timeout -k1m 2h java "$debugger" -jar "$dacapo" "${size[@]}" "$benchmark"
done
