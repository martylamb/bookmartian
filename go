#!/bin/bash

# tells webserver to pick up live changes to static content while running
export BOOM_DEBUG=1

mvn exec:java -Dexec.mainClass="com.martiansoftware.bookmartian.App" -Dexec.classpathScope="compile" -Dexec.args="$*"
