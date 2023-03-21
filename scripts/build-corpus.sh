#!/bin/bash

# Build a corpus of Java projects from GitHub fulfilling inclusion and exclusion criteria

shopt -s nullglob

projects_file=projects.txt
if [ ! -f "$projects_file" ]; then
  echo "Please place $projects_file with a list of repositories to the mounted data directory ($DATA_DIR)."
  exit 1
fi

gh_token_file=ghtoken.txt
if [ -f "$gh_token_file" ]; then
  gh_auth="Authorization: Bearer $(cat "$gh_token_file")"
else
  echo "GitHub API access rate will be limited as file $gh_token_file was not found in $DATA_DIR."
  echo "To increase the limits, supply a GitHub personal access token with public-only access (no scopes)."
fi

corpus_dir=$(realpath corpus)
mkdir -p "$corpus_dir"

build_project() {
  repo=$1

  curl -LsSIfo /dev/null "https://api.github.com/repos/$repo/contents/pom.xml" -H "$gh_auth" || return
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

    find "$corpus_dir" -path '*/jars/*' -name "$(basename "$jarfile")" | [ "$(wc -l)" -ge 2 ] \
      && { echo "Duplicate JAR!"; return 1; };
  done

  for jarfile in ../jars/*.jar ../deps/*.jar; do
    jar -tf "$jarfile" | grep '^META-INF/versions/[0-9]\+' \
      && rm -rf META-INF \
      && jar -xf "$jarfile" META-INF/versions/ \
      && for f in META-INF/versions/{9..17}; do
           [ -d "$f" ] && jar -uf "$jarfile" -C "$f" .
         done
  done

  for jarfile in ../jars/*.jar ../deps/*.jar; do
    read_classes "$jarfile"
    [ ${#CLASSES[@]} -eq 0 ] && { rm "$jarfile"; continue; }
    javap -cp "$jarfile" --multi-release 17 -p "${CLASSES[@]}" 2>&1 | grep -e ' native ' \
      -e '^Warning: ' -e '^Error: ' && { echo "JNI used/javap failed!"; return 1; }
  done

  entry_point=0
  for jarfile in ../jars/*.jar; do
    read_classes "$jarfile"
    javap -cp "$jarfile" --multi-release 17 -public -v "${CLASSES[@]}" \
      | grep -qFm1 -e 'public static void main(java.lang.String[]);' -e 'junit.framework.TestCase' \
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
    [[ ! "$repo" ]] && continue
    echo "---------- $repo ----------"

    dir="$corpus_dir/${repo/\//__}"
    [ -e "$dir" ] && { echo "$dir already exists, deleting."; rm -rf "$dir"; }
    mkdir -p "$dir/source"
    pushd "$dir/source" > /dev/null || exit 1

    build_project "$repo"

    result=$?
    popd > /dev/null || exit 1

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

build_all 3< <(cat "$projects_file"; echo)
exit 0
