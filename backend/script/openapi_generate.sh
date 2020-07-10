# Notes on running the script
# See .swagger-codegen-ignore to see which files are not generated. A lot of the common classes
# were moved into common/, however when generating the models, the import paths need to be manually
# corrected:
# 1. util comes from 'common' not 'PACKAGE_NAME' (eg. referral)
# 2. Fix imports for the models
# 3. Do not regenerate util.py as it breaks python 3.7 (see https://github.com/zalando/connexion/issues/739)

PROJECT_PATH=$(pwd)/..

PLATFORM=python-flask
CODE_GEN_CMD="swagger-codegen generate -l ${PLATFORM}"

# Name of python package inside generated project
# Package name is defined in the config file
PACKAGE_NAME=referral

# Path of the source API spec
API_SPEC_PATH=${PROJECT_PATH}/${PACKAGE_NAME}/api.yaml

# Output directory for generated project
OUTPUT_PATH=${PROJECT_PATH}

# Keep the naming of the target package ignostic to Swagger 2.0 or OpenAPI 3.0
#rm -rf ${PACKAGE_TARGET_PATH:?}/${PACKAGE_NAME}

eval "${CODE_GEN_CMD} -i ${API_SPEC_PATH} -o ${OUTPUT_PATH} -DpackageName=${PACKAGE_NAME}"
