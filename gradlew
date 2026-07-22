#!/bin/sh

#
# Standard Gradle wrapper launcher script.
# NOTE: gradle/wrapper/gradle-wrapper.jar is intentionally NOT committed to
# this repository (binary jars don't belong in template-generated diffs).
# Run `gradle wrapper` once locally (any local Gradle install works) to
# regenerate it, or rely on the bundled GitHub Actions workflow, which
# builds using gradle/actions/setup-gradle and does not need this file.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_HOME=$(cd "$(dirname "$0")" >/dev/null && pwd)

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: $CLASSPATH not found."
  echo "Run 'gradle wrapper' with any local Gradle installation to generate it,"
  echo "or use a system-installed 'gradle' command directly."
  exit 1
fi

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
