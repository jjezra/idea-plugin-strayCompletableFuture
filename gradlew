#!/bin/sh

#
# Standard Gradle wrapper launcher (Gradle 8.7).
#

APP_HOME=$( cd "${0%/*}" && pwd -P ) || exit

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ] ; then
    echo "ERROR: $CLASSPATH is missing." >&2
    echo "Generate it once with a system Gradle:  gradle wrapper --gradle-version 8.7" >&2
    echo "or simply open this project in IntelliJ IDEA, which provisions Gradle automatically." >&2
    exit 1
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
