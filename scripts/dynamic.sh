#!/bin/bash

: "${1?Missing argument: dacapo_jar}"
dacapo=$1

[ ! "$JAVA_HOME" ] && echo "Please set JAVA_HOME to the custom JRE from prepare-jdk.sh." && exit 1
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

benchmarks=$(java -jar "$dacapo" -l 2>&1 | tail -1)
exclude=(cassandra eclipse)
for e in "${exclude[@]}"; do benchmarks=${benchmarks/$e}; done

port=8000
debugger=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:$port
db_dir=$(dirname "$dacapo")

for benchmark in $benchmarks; do
  echo "---------- $benchmark ----------"
  java -jar iostudy.jar dynamic "$benchmark" $port "$db_dir" &
  timeout -k1m 2h java "$debugger" -jar "$dacapo" -s small "$benchmark"
  [ $? -ge 124 ] && echo "Benchmark $benchmark timed out!"
done
