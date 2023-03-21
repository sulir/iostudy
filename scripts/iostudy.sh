#!/bin/bash

# The main IOStudy entry point script

shopt -s nullglob
trap 'pkill -P $$; exit 1' TERM INT

[ ! "$JDK_HOME" ] && echo "Please set JDK_HOME to Temurin JDK 17.0.6+10 Linux x64." && exit 1
[ ! "$JRE_HOME" ] && echo "Please set JRE_HOME to the custom JRE with only SE modules." && exit 1
[ ! "$APP_HOME" ] && echo "Please set APP_HOME to the directory containing iostudy.jar." && exit 1

iostudy_jar=$APP_HOME/iostudy.jar

natives() {
  java -jar "$iostudy_jar" natives
}

corpus() {
  "$(dirname "$0")"/build-corpus.sh
}

static() {
  [ ! -d corpus ] && echo "The corpus of projects does not exist." && return 1

  for project_dir in corpus/*/; do
    project=$(basename "$project_dir")
    echo "---------- ${project/__/\/} ----------"
    timeout -k1m 1h java -mx14g -jar "$iostudy_jar" static "$project"
    [ $? -eq 124 ] && echo "Project ${project/__/\/} timed out!"
  done

  return 0
}

dynamic() {
  [ ! "$DACAPO_JAR" ] && echo "Please set DACAPO_JAR to the DaCapo benchmark path." && return 1

  benchmarks=$(java -jar "$DACAPO_JAR" -l 2>&1 | tail -1)
  exclude=(cassandra eclipse)
  for e in "${exclude[@]}"; do benchmarks=${benchmarks/$e}; done

  port=8000
  debugger=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:$port

  for benchmark in $benchmarks; do
    echo "---------- $benchmark ----------"
    java -jar "$iostudy_jar" dynamic "$benchmark" $port . &
    timeout -k1m 2h java "$debugger" -jar "$DACAPO_JAR" -s small "$benchmark"
    [ $? -ge 124 ] && echo "Benchmark $benchmark timed out!"
  done

  return 0
}

results() {
  java -jar "$iostudy_jar" results
}

all_phases=(natives corpus static results)
default_phases=(corpus static results)

if [ $# -eq 0 ]; then
  phases=("${default_phases[@]}")
else
  phases=("$@")
fi

for phase in "${phases[@]}"; do
  [[ " ${all_phases[*]} " != *" $phase "* ]] && echo "Unknown study phase: $phase" && exit 1
  echo "========== $phase =========="

  old_path=$PATH
  PATH=$(echo "$PATH" | tr ':' '\n' | grep -vFx "$JDK_HOME" | tr '\n' ':')
  if [ "$phase" = corpus ]; then
    PATH="$JDK_HOME/bin:$PATH"
  else
    PATH="$JRE_HOME/bin:$PATH"
  fi
  export PATH

  $phase || { echo "Phase \"$phase\" failed, stopping."; exit 1; }
  export PATH=$old_path
done

echo "IO study finished."
