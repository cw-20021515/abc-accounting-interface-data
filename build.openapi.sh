#!/usr/bin/env bash

OPENAPI_DIR="api-specification/api-specs"
COMMON_GENERATED_DIR="api-specification/src/main/kotlin/com/abc/us/generated"

# openapi 디렉토리가 존재하는지 확인
if [ -d "$OPENAPI_DIR" ]; then
    # openapi 디렉토리가 존재하면 삭제
    rm -rf "$OPENAPI_DIR"
    echo "$OPENAPI_DIR 디렉토리가 삭제되었습니다."
else
    echo "$OPENAPI_DIR 디렉토리가 존재하지 않습니다."
fi


# 현재 디렉토리에 COMMON_GENERATED_DIR 디렉토리가 존재하는지 확인
if [ -d "$COMMON_GENERATED_DIR" ]; then
    # COMMON_GENERATED_DIR 디렉토리가 존재하면 삭제
    rm -rf "$COMMON_GENERATED_DIR"
    echo "$COMMON_GENERATED_DIR 디렉토리가 삭제되었습니다."
else
    echo "$COMMON_GENERATED_DIR 디렉토리가 존재하지 않습니다."
fi


git clone --recurse-submodules https://github.com/abc-us/abc-api-specs.git api-specification/api-specs
./gradlew :api-specification:openApiGenerate