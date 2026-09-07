#![no_std]
use core::panic::PanicInfo;

#[panic_handler]
fn panic(_info: &PanicInfo) -> ! {
    loop {}
}

#[no_mangle]
pub extern "C" fn rust_eh_personality() {}

/// Calculates distance and sanity drain factor with high-precision math in Rust
#[no_mangle]
pub extern "C" fn rust_calculate_sanity_decay(
    current_sanity: f32,
    flashlight_on: bool,
    flicker_level: f32,
    delta_time_sec: f32,
) -> f32 {
    let base_drain = if !flashlight_on && flicker_level < 0.35 {
        // In deep dark asylum areas sanity drops fast
        7.5
    } else if !flashlight_on {
        // In dim flickering room
        3.0
    } else {
        // Linterna encendida calma al investigador
        -2.5
    };

    let new_sanity = current_sanity - (base_drain * delta_time_sec);
    if new_sanity < 0.0 {
        0.0
    } else if new_sanity > 100.0 {
        100.0
    } else {
        new_sanity
    }
}

/// Fast Rust vector distance check for horror jump triggers
#[no_mangle]
pub extern "C" fn rust_distance_to_target(
    px: f32,
    py: f32,
    tx: f32,
    ty: f32,
) -> f32 {
    let dx = px - tx;
    let dy = py - ty;
    // Approximated fast hypot without libm in no_std
    let sq = (dx * dx) + (dy * dy);
    // Square root approximation (Newton-Raphson)
    if sq <= 0.0 {
        return 0.0;
    }
    let mut x = sq;
    let mut y = 1.0;
    let e = 0.0001;
    while (x - y) > e || (y - x) > e {
        x = (x + y) / 2.0;
        y = sq / x;
    }
    x
}
