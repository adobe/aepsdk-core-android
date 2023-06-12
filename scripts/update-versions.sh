#!/bin/bash

# make this script executable from terminal:
# chmod 755 update-versions.sh

set -e # Any subsequent(*) commands which fail will cause the shell script to exit immediately

ROOT_DIR=$(git rev-parse --show-toplevel)
LINE="================================================================================"
VERSION_REGEX="[0-9]+\.[0-9]+\.[0-9]+"

help()
{
   echo ""
   echo "Usage: $0 -n EXTENSION_NAME -v NEW_VERSION"
   echo ""
   echo -e "    -n\t- Name of the extension getting a version update. \n\t  Example: Edge, Analytics\n"
   echo -e "    -v\t- New version to use for the extension. \n\t  Example: 3.0.2\n"
   exit 1 # Exit script after printing help
}

while getopts "n:v:d:" opt
do
   case "$opt" in
      n ) NAME="$OPTARG" ;;
      v ) NEW_VERSION="$OPTARG" ;;
      ? ) help ;; # Print help in case parameter is non-existent
   esac
done

# Print help in case parameters are empty
if [ -z "$NAME" ] || [ -z "$NEW_VERSION" ]
then
   echo "********** USAGE ERROR **********"
   echo "Some or all of the parameters are empty. See usage below:";
   help
fi

# Begin script in case all parameters are correct
echo ""
echo "$LINE"
echo "Changing version of $NAME to $NEW_VERSION"
echo "$LINE"

GRADLE_PROPERTIES_FILE=$ROOT_DIR"/code/gradle.properties"
# Replace version in Constants file
if [ "$NAME" == "Core" ]; then
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$GRADLE_PROPERTIES_FILE'"
    sed -i '' -E "/^coreExtensionVersion/{s/$VERSION_REGEX/$NEW_VERSION/;}" $GRADLE_PROPERTIES_FILE  

    CONSTANTS_FILE=$ROOT_DIR"/code/core/src/main/java/com/adobe/marketing/mobile/internal/CoreConstants.kt"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$CONSTANTS_FILE'"
    sed -i '' -E "/^ +const val VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $CONSTANTS_FILE

    API_TEST_FILE=$ROOT_DIR"/code/core/src/test/java/com/adobe/marketing/mobile/MobileCoreTests.kt"    
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$API_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $API_TEST_FILE

    EXTENSION_TEST_FILE=$ROOT_DIR"/code/core/src/test/java/com/adobe/marketing/mobile/internal/configuration/ConfigurationExtensionTests.kt"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$EXTENSION_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $EXTENSION_TEST_FILE
    
elif [ "$NAME" == "Lifecycle" ]; then
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$GRADLE_PROPERTIES_FILE'"
    sed -i '' -E "/^lifecycleExtensionVersion/{s/$VERSION_REGEX/$NEW_VERSION/;}" $GRADLE_PROPERTIES_FILE  

    CONSTANTS_FILE=$ROOT_DIR"/code/lifecycle/src/phone/java/com/adobe/marketing/mobile/Lifecycle.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$CONSTANTS_FILE'"
    sed -i '' -E "/^ +.*String EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $CONSTANTS_FILE

    API_TEST_FILE=$ROOT_DIR"/code/lifecycle/src/test/java/com/adobe/marketing/mobile/LifecycleAPITests.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$API_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $API_TEST_FILE

    EXTENSION_TEST_FILE=$ROOT_DIR"/code/lifecycle/src/test/java/com/adobe/marketing/mobile/lifecycle/LifecycleExtensionTests.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$EXTENSION_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $EXTENSION_TEST_FILE

elif [ "$NAME" == "Identity" ]; then
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$GRADLE_PROPERTIES_FILE'"
    sed -i '' -E "/^identityExtensionVersion/{s/$VERSION_REGEX/$NEW_VERSION/;}" $GRADLE_PROPERTIES_FILE  

    CONSTANTS_FILE=$ROOT_DIR"/code/identity/src/phone/java/com/adobe/marketing/mobile/Identity.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$CONSTANTS_FILE'"
    sed -i '' -E "/^ +.*String EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $CONSTANTS_FILE

    API_TEST_FILE=$ROOT_DIR"/code/identity/src/test/java/com/adobe/marketing/mobile/IdentityAPITests.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$API_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $API_TEST_FILE

    EXTENSION_TEST_FILE=$ROOT_DIR"/code/identity/src/test/java/com/adobe/marketing/mobile/identity/IdentityExtensionTests.kt"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$EXTENSION_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $EXTENSION_TEST_FILE

elif [ "$NAME" == "Signal" ]; then
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$GRADLE_PROPERTIES_FILE'"
    sed -i '' -E "/^signalExtensionVersion/{s/$VERSION_REGEX/$NEW_VERSION/;}" $GRADLE_PROPERTIES_FILE  

    CONSTANTS_FILE=$ROOT_DIR"/code/signal/src/phone/java/com/adobe/marketing/mobile/Signal.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$CONSTANTS_FILE'"
    sed -i '' -E "/^ +.*String EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $CONSTANTS_FILE

    API_TEST_FILE=$ROOT_DIR"/code/signal/src/test/java/com/adobe/marketing/mobile/SignalAPITests.java"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$API_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $API_TEST_FILE

    EXTENSION_TEST_FILE=$ROOT_DIR"/code/signal/src/test/java/com/adobe/marketing/mobile/signal/internal/SignalExtensionTests.kt"
    echo "Changing value of 'EXTENSION_VERSION' to '$NEW_VERSION' in '$EXTENSION_TEST_FILE'"
    sed -i '' -E "/^.+EXTENSION_VERSION/{s/$VERSION_REGEX/$NEW_VERSION/;}" $EXTENSION_TEST_FILE
fi
