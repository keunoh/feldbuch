#!/bin/bash

H2_JAR=$(find ~/.gradle -name "h2-*.jar" | head -1)

java -cp "$H2_JAR" \
org.h2.tools.Server \
-tcp \
-tcpAllowOthers \
-ifNotExists \
-baseDir "/Users/kaltz/Documents/프로젝트/feldbuch"