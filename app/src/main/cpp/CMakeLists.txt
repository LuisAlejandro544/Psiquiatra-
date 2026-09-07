cmake_minimum_required(VERSION 3.22.1)
project("sanatorio_native" C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Explicit Lua 5.4.7 C Sources
set(LUA_SOURCES
    lua/lapi.c
    lua/lauxlib.c
    lua/lbaselib.c
    lua/lcode.c
    lua/lcorolib.c
    lua/lctype.c
    lua/ldblib.c
    lua/ldebug.c
    lua/ldo.c
    lua/ldump.c
    lua/lfunc.c
    lua/lgc.c
    lua/linit.c
    lua/liolib.c
    lua/llex.c
    lua/lmathlib.c
    lua/lmem.c
    lua/loadlib.c
    lua/lobject.c
    lua/lopcodes.c
    lua/loslib.c
    lua/lparser.c
    lua/lstate.c
    lua/lstring.c
    lua/lstrlib.c
    lua/ltable.c
    lua/ltablib.c
    lua/ltm.c
    lua/lundump.c
    lua/lutf8lib.c
    lua/lvm.c
    lua/lzio.c
)

# Target architecture mapping for Rust static library
if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(RUST_TARGET "aarch64-linux-android")
elseif(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(RUST_TARGET "armv7-linux-androideabi")
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(RUST_TARGET "x86_64-linux-android")
else()
    set(RUST_TARGET "aarch64-linux-android")
endif()

set(RUST_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../rust")
set(RUST_LIB_PATH "${RUST_DIR}/target/${RUST_TARGET}/release/libasylum_core_rust.a")

find_program(CARGO_EXECUTABLE NAMES cargo PATHS "$ENV{HOME}/.cargo/bin" "/root/.cargo/bin" "/usr/bin" "/usr/local/bin")

if(CARGO_EXECUTABLE)
    add_custom_command(
        OUTPUT "${RUST_LIB_PATH}"
        COMMAND ${CMAKE_COMMAND} -E env "PATH=$ENV{PATH}:$ENV{HOME}/.cargo/bin"
                "${CARGO_EXECUTABLE}" build --release --target "${RUST_TARGET}" --manifest-path "${RUST_DIR}/Cargo.toml"
        WORKING_DIRECTORY "${RUST_DIR}"
        COMMENT "Building Rust static library for ${RUST_TARGET}"
        VERBATIM
    )
    add_custom_target(rust_target DEPENDS "${RUST_LIB_PATH}")
elseif(EXISTS "${RUST_LIB_PATH}")
    message(STATUS "Cargo no encontrado en PATH, utilizando libreria Rust precompilada en: ${RUST_LIB_PATH}")
    add_custom_target(rust_target)
else()
    message(FATAL_ERROR "No se encontro Cargo ni la libreria estatica de Rust en ${RUST_LIB_PATH}")
endif()

# Native Shared Library
add_library(
    sanatorio_native
    SHARED
    native_bridge.cpp
    ${LUA_SOURCES}
)

add_dependencies(sanatorio_native rust_target)

target_include_directories(
    sanatorio_native
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/lua
)

target_compile_definitions(
    sanatorio_native
    PRIVATE
    LUA_COMPAT_5_3
)

target_link_libraries(
    sanatorio_native
    PRIVATE
    "${RUST_LIB_PATH}"
    log
)
