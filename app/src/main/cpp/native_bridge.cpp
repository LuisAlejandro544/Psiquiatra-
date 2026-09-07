#include <jni.h>
#include <string>
#include <sstream>
#include <vector>
#include <cmath>
#include <android/log.h>
#include <GLES3/gl32.h>
#include <EGL/egl.h>

#define LOG_TAG "SanatorioNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Pure C Lua 5.4 headers (Original C Lua, no wrappers)
extern "C" {
#include "lua/lua.h"
#include "lua/lauxlib.h"
#include "lua/lualib.h"
}

// Rust bridge
#include "rust_bridge.h"

static lua_State* g_lua_state = nullptr;

// Custom C function exposed to Lua scripts
static int lua_native_log_horror_event(lua_State* L) {
    const char* event_msg = luaL_checkstring(L, 1);
    LOGI("[Lua Horror Event]: %s", event_msg);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_sanatorio_nativebridge_NativeEngineBridge_initNativeEngine(
    JNIEnv* env,
    jobject /* this */
) {
    LOGI("Initializing Native C++ Engine, Lua 5.4, and Rust components...");

    if (g_lua_state != nullptr) {
        lua_close(g_lua_state);
        g_lua_state = nullptr;
    }

    // Initialize original C Lua 5.4 state
    g_lua_state = luaL_newstate();
    if (g_lua_state != nullptr) {
        luaL_openlibs(g_lua_state);

        // Register C++ function inside Lua environment
        lua_register(g_lua_state, "logHorrorEvent", lua_native_log_horror_event);

        // Run default horror world script in Lua
        const char* init_script =
            "sanatorio_version = '1.0.0'\n"
            "asylum_threat_level = 1\n"
            "function onPatientEncounter(patientId, sanityLevel)\n"
            "    logHorrorEvent('Evaluando encuentro con paciente ' .. patientId)\n"
            "    if sanityLevel < 40 then\n"
            "        return 'PANIC: El investigador sufre alucinaciones auditivas'\n"
            "    else\n"
            "        return 'CALM: El investigador mantiene el pulso firme'\n"
            "    end\n"
            "end\n"
            "function getAsylumLoreSummary()\n"
            "    return 'Sanatorio San Gabriel: Clausurado en 1984 tras incidentes en pabellon C.'\n"
            "end\n";

        if (luaL_dostring(g_lua_state, init_script) != LUA_OK) {
            const char* err = lua_tostring(g_lua_state, -1);
            LOGE("Error evaluating Lua initial script: %s", err);
            lua_pop(g_lua_state, 1);
        } else {
            LOGI("Lua 5.4 script environment ready.");
        }
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_sanatorio_nativebridge_NativeEngineBridge_executeLuaScript(
    JNIEnv* env,
    jobject /* this */,
    jstring script_str
) {
    if (g_lua_state == nullptr) {
        return env->NewStringUTF("Error: Lua engine no inicializado");
    }

    const char* script = env->GetStringUTFChars(script_str, nullptr);
    std::string result_message;

    if (luaL_dostring(g_lua_state, script) != LUA_OK) {
        const char* err = lua_tostring(g_lua_state, -1);
        result_message = std::string("Error Lua: ") + (err ? err : "desconocido");
        lua_pop(g_lua_state, 1);
    } else {
        // If script left a string on stack, return it
        if (lua_isstring(g_lua_state, -1)) {
            result_message = lua_tostring(g_lua_state, -1);
            lua_pop(g_lua_state, 1);
        } else {
            result_message = "Lua OK";
        }
    }

    env->ReleaseStringUTFChars(script_str, script);
    return env->NewStringUTF(result_message.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_sanatorio_nativebridge_NativeEngineBridge_getEngineStatus(
    JNIEnv* env,
    jobject /* this */
) {
    std::ostringstream oss;
    oss << "C++ NDK: OK (Clang LLVM)\n";
    oss << "OpenGL ES: 3.2 Hardware Accelerated (GLESv3/EGL)\n";
    oss << "Lua: " << LUA_RELEASE << " (" << LUA_COPYRIGHT << ")\n";

    // Test Rust call
    float test_dist = rust_distance_to_target(0.0f, 0.0f, 3.0f, 4.0f);
    oss << "Rust Core: OK (dist(0,0 -> 3,4) = " << test_dist << ")";

    return env->NewStringUTF(oss.str().c_str());
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_sanatorio_nativebridge_NativeEngineBridge_calculateSanityRust(
    JNIEnv* env,
    jobject /* this */,
    jfloat current_sanity,
    jboolean flashlight_on,
    jfloat flicker_level,
    jfloat delta_time_sec
) {
    return rust_calculate_sanity_decay(
        current_sanity,
        flashlight_on,
        flicker_level,
        delta_time_sec
    );
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_sanatorio_nativebridge_NativeEngineBridge_calculateDistanceRust(
    JNIEnv* env,
    jobject /* this */,
    jfloat px,
    jfloat py,
    jfloat tx,
    jfloat ty
) {
    return rust_distance_to_target(px, py, tx, ty);
}
