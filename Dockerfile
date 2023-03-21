FROM eclipse-temurin:17.0.6_10-jdk

ENV JDK_HOME=$JAVA_HOME \
    JRE_HOME=/opt/java/jre \
    APP_HOME=/opt/app \
    DATA_DIR=/opt/data
RUN JRE_MODULES=java.se,java.smartcardio,jdk.jdwp.agent,jdk.jdi && \
    jlink --add-modules $JRE_MODULES --no-header-files --no-man-pages --output $JRE_HOME && \
    jimage extract --dir=$JRE_HOME/mods $JRE_HOME/lib/modules && \
    for dir in $JRE_HOME/mods/*; do \
      jar -cf "$dir.jar" -C "$dir" .; \
      rm -r "$dir"; \
    done

RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

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
