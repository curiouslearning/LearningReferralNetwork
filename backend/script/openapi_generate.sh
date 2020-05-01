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
