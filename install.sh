#!/bin/sh

VERSION=1.0.0-SNAPSHOT
JAR=db-${VERSION}-jar-with-dependencies.jar

mvn install
cat src/main/bash/stub.sh target/${JAR} > db
chmod a+x db

sudo mv db /usr/bin
