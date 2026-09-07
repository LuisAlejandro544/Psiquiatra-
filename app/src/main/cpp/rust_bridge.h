#pragma once

#ifdef __cplusplus
extern "C" {
#endif

float rust_calculate_sanity_decay(
    float current_sanity,
    bool flashlight_on,
    float flicker_level,
    float delta_time_sec
);

float rust_distance_to_target(
    float px,
    float py,
    float tx,
    float ty
);

#ifdef __cplusplus
}
#endif
