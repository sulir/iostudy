#!/bin/bash

# Build a corpus of Java projects from GitHub fulfilling inclusion and exclusion criteria.

: "${1?Missing argument: output_directory}"
corpus="$1"
gh_auth="Authorization: Bearer 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"
shopt -s nullglob

build_project() {
  repo=$1

  curl -IfLsSo /dev/null "https://api.github.com/repos/$repo/contents/pom.xml" -H "$gh_auth" || return
  curl -LsS "https://api.github.com/repos/$repo/tarball" -H "$gh_auth" \
    | tar -xz --strip-components 1 || return

  timeout -k1m 30m mvn -B -q package \
    -DskipTests -Dmaven.javadoc.skip -Dassembly.skipAssembly=true -Dmdep.skip || return
  mvn -B -q jar:test-jar || return
  mvn -B -q dependency:copy-dependencies -DoutputDirectory="$(pwd)/../deps" || return

  mkdir ../jars
  find . -regex '.*/target/[^/]+\.jar' ! -regex '.*-\(sources\|with-dependencies\)\.jar' \
    -exec mv {} ../jars \;
  
  for f in ../jars/original-*.jar; do mv "$f" "${f/original-/}"; done
  for f in ../jars/*-without-dependencies.jar; do mv "$f" "${f/-without-dependencies/}"; done

  for jarfile in ../jars/*.jar; do
    jar -tf "$jarfile" | grep '^BOOT-INF/classes/' \
      && rm -rf BOOT-INF \
      && jar -xf "$jarfile" BOOT-INF/classes \
      && rm "$jarfile" \
      && jar -cf "$jarfile" -C BOOT-INF/classes .

    find "$corpus" -path '*/jars/*' -name "$(basename "$jarfile")" | [ "$(wc -l)" -ge 2 ] \
      && { echo "Duplicate JAR!"; return 1; };
  done

  for jarfile in ../jars/*.jar ../deps/*.jar; do
    read_classes "$jarfile"
    [ ${#CLASSES[@]} -eq 0 ] && { rm "$jarfile"; continue; }
    javap -cp "$jarfile" --multi-release 17 -p "${CLASSES[@]}" | grep " native " \
      && { echo "JNI used!"; return 1; }
  done

  entry_point=0
  for jarfile in ../jars/*.jar; do
    read_classes "$jarfile"
    javap -cp "$jarfile" --multi-release 17 -public -v "${CLASSES[@]}" \
      | grep -F -m1 -e 'public static void main(java.lang.String[]);' -e 'junit.framework.TestCase' \
      -e'org.junit.Test' -e 'org.junit.jupiter.api.Test' && { entry_point=1; break; }
  done
  [ $entry_point -eq 0 ] && { echo "No entry points!"; return 1; }

  mvn -B -q dependency:tree -Dtokens=whitespace -DoutputFile=../mvn.txt
  jdeps_classpath=$(find ../deps -name '*.jar' | tr '\n' ':')
  [ ! "$jdeps_classpath" ] && jdeps_classpath=../jars
  jdeps --multi-release 17 -R -s -cp "$jdeps_classpath" ../jars/*.jar > ../jdeps.txt \
    || { echo "jdeps failed!"; return 1; }
  grep -i '\-> jdk[. ]' ../jdeps.txt && { echo "JDK used!"; return 1; }
  grep '\-> not found' ../jdeps.txt && { echo "Missing dependencies!"; return 1; }

  stat --printf '' ../jars/*.jar || { echo "No JARs!"; return 1; }
}

read_classes() {
  readarray -t CLASSES < <(jar -tf "$1" | grep '\.class$' \
    | sed -e 's#^META-INF/versions/[0-9]\+/##' -e 's#\.class##')
}

build_all() {
  while read -ru3 repo; do
    echo "---------- $repo ----------"
    dir="$corpus/${repo/\//__}"
    mkdir -p "$dir/source"
    pushd "$dir/source" > /dev/null || exit
    build_project "$repo"
    result=$?
    popd > /dev/null || exit

    if [ $result -eq 0 ]; then
      rm -rf "$dir/source"
    else
      rm -rf "$dir"
    fi

    if [ "$(df -m --output=avail ~ | tail -n 1)" -lt 8192 ]; then
      read -rt 30 -p $'Low disk space, erasing Maven dir ~/.m2. Press Enter to cancel\n' || rm -rf ~/.m2
    fi
  done
}

build_all 3< projects.txt
