#!/bin/bash

# Make this script executable from terminal:
# chmod 755 update-versions.sh
set -e # Any subsequent(*) commands which fail will cause the shell script to exit immediately

ROOT_DIR=$(git rev-parse --show-toplevel)
LINE="================================================================================"
VERSION_REGEX="[0-9]+\.[0-9]+\.[0-9]+"

GRADLE_PROPERTIES_FILE=$ROOT_DIR"/code/gradle.properties"

CORE_CONSTANTS_FILE=$ROOT_DIR"/code/core/src/main/java/com/adobe/marketing/mobile/internal/CoreConstants.kt"
CORE_EXTENSION_VERSION_REGEX="^ +const val VERSION *= *"

LIFECYCLE_CONSTANTS_FILE=$ROOT_DIR"/code/lifecycle/src/phone/java/com/adobe/marketing/mobile/Lifecycle.java"
LIFECYCLE_EXTENSION_VERSION_REGEX="^.*String EXTENSION_VERSION *= *"

IDENTITY_CONSTANTS_FILE=$ROOT_DIR"/code/identity/src/phone/java/com/adobe/marketing/mobile/Identity.java" 
IDENTITY_EXTENSION_VERSION_REGEX="^.*String EXTENSION_VERSION *= *"

SIGNAL_CONSTANTS_FILE=$ROOT_DIR"/code/signal/src/phone/java/com/adobe/marketing/mobile/Signal.java"
SIGNAL_EXTENSION_VERSION_REGEX="^.*String EXTENSION_VERSION *= *"


help()
{
   echo ""
   echo "Usage: $0 -n EXTENSION_NAME -v NEW_VERSION"
   echo ""
   echo -e "    -n\t- Name of the extension getting a version update. \n\t Either core, identity, lifecycle or signal\n"
   echo -e "    -v\t- New version to use for the extension. \n\t  Example: 3.0.2\n"
   echo -e "    -u\t- Updates the version. If this flag is absent, the script verifies if the version is correct\n"
   exit 1 # Exit script after printing help
}

sed_platform() {
    # Ensure sed works properly in linux and mac-os.
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "$@"
    else
        sed -i "$@"
    fi
}

update() {
    echo "Changing $NAME version to $VERSION"

    # Replace version in constants file
    echo "Changing 'EXTENSION_VERSION' to '$VERSION' in '$CONSTANTS_FILE'"
    sed_platform -E "/$EXTENSION_VERSION_REGEX/{s/$VERSION_REGEX/$VERSION/;}" $CONSTANTS_FILE

    # Replace version in gradle.properties file
    echo "Changing $GRADLE_TAG to '$VERSION' in '$GRADLE_PROPERTIES_FILE'"
    sed_platform -E "/^$GRADLE_TAG/{s/$VERSION_REGEX/$VERSION/;}" $GRADLE_PROPERTIES_FILE
}

verify() {    
    echo "Verifing $NAME version is $VERSION"

    if ! grep -E "$EXTENSION_VERSION_REGEX\"$VERSION\"" "$CONSTANTS_FILE" >/dev/null; then
        echo "'EXTENSION_VERSION' does not match '$VERSION' in '$CONSTANTS_FILE'"            
        exit 1
    fi

    if ! grep -E "^$GRADLE_TAG=.*$VERSION" "$GRADLE_PROPERTIES_FILE" >/dev/null; then
        echo "'$GRADLE_TAG' does not match '$VERSION' in '$GRADLE_PROPERTIES_FILE'"            
        exit 1
    fi
    
    echo "Success"
}


while getopts "n:v:u" opt
do
   case "$opt" in
      n ) NAME="$OPTARG" ;;
      v ) VERSION="$OPTARG" ;;
      u ) UPDATE="true" ;;   
      ? ) help ;; # Print help in case parameter is non-existent
   esac
done

# Print help in case parameters are empty
if [ -z "$NAME" ] || [ -z "$VERSION" ]
then
   echo "********** USAGE ERROR **********"
   echo "Some or all of the parameters are empty. See usage below:";
   help
fi

NAME_LC=$(echo "$NAME" | tr '[:upper:]' '[:lower:]')
NAME_UC=$(echo "$NAME" | tr '[:lower:]' '[:upper:]')

eval CONSTANTS_FILE=\$$"$NAME_UC"_CONSTANTS_FILE
eval EXTENSION_VERSION_REGEX=\$$"$NAME_UC"_EXTENSION_VERSION_REGEX
GRADLE_TAG="$NAME_LC"ExtensionVersion

echo "$LINE"
if [[ ${UPDATE} = "true" ]];
then
    update 
else 
    verify
fi
echo "$LINE"





