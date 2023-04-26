FROM eclipse-temurin:17.0.6_10-jdk

ENV JDK_HOME=$JAVA_HOME \
    JRE_HOME=/opt/java/jre \
    DACAPO_JAR=/opt/dacapo/dacapo-mini-0413279.jar \
    APP_HOME=/opt/app \
    DATA_DIR=/opt/data

RUN jre_modules=java.se,java.smartcardio,jdk.jdwp.agent,jdk.jdi,jdk.unsupported && \
    jlink --add-modules $jre_modules --no-header-files --no-man-pages --output $JRE_HOME && \
    jimage extract --dir=$JRE_HOME/mods $JRE_HOME/lib/modules && \
    for dir in $JRE_HOME/mods/*; do \
      jar -cf "$dir.jar" -C "$dir" .; \
      rm -r "$dir"; \
    done

RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

RUN dacapo_dir=$(dirname $DACAPO_JAR) && \
    mkdir -p $dacapo_dir && \
    curl -L https://osf.io/download/2jqpv/ | tar --no-same-owner -xzC $dacapo_dir

COPY LICENSE.txt pom.xml $APP_HOME/
COPY src $APP_HOME/src
RUN cd $APP_HOME && \
    mvn -B -q package && \
    mv target/iostudy.jar . && \
    rm -rf target

VOLUME $DATA_DIR
WORKDIR $DATA_DIR

COPY scripts $APP_HOME/scripts
ENTRYPOINT ["bash", "/opt/app/scripts/iostudy.sh"]
