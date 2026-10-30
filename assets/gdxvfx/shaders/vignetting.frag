#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
	precision PRECISION int;
#else
	#define PRECISION
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture0;
uniform float u_vignetteIntensity;
uniform float u_vignetteX;
uniform float u_vignetteY;
uniform float u_centerX;
uniform float u_centerY;

uniform float u_time;
uniform vec2 u_resolution;

#ifdef CONTROL_SATURATION
	const vec3 grayscale = vec3(0.3, 0.59, 0.11);

	uniform float u_saturation;
	uniform float u_saturationMul;

	// 0 = totally desaturated
	// 1 = saturation unchanged
	// higher = increase saturation
	vec3 adjustSaturation(vec3 color, float saturation) {
		vec3 grey = vec3(dot(color, grayscale));
		return mix(grey, color, saturation);	// correct
	}
#endif

// Более качественная функция шума для дизеринга (Interleaved Gradient Noise + Time)
float ditherNoise(vec2 position, float time) {
// Подмешиваем время к позиции пикселя
vec2 uv = position + vec2(time * 13.0, time * 37.0);
return fract(52.9829189 * fract(dot(uv, vec2(0.0605, 0.00597))));
}

void main() {
    vec3 rgb = texture2D(u_texture0, v_texCoords).xyz;
    float d = distance(v_texCoords, vec2(u_centerX, u_centerY));
    float factor = smoothstep(u_vignetteX, u_vignetteY, d);
    rgb = rgb * factor + rgb * (1.0 - factor) * (1.0 - u_vignetteIntensity);

#ifdef CONTROL_SATURATION
    rgb = adjustSaturation(rgb, u_saturation) * u_saturationMul;
#endif

    // --- МОДЕРНИЗИРОВАННЫЙ ДИЗЕРИНГ ---
    // Используем координаты пикселей экрана вместо v_texCoords
    vec2 pixelPos = v_texCoords * u_resolution;

    // Получаем шум в диапазоне [-0.5, 0.5]
    float noise = ditherNoise(pixelPos, u_time) - 0.5;

    // Применяем к 8-битному цветовому пространству
    rgb += noise * (1.0 / 255.0);
    // ----------------------------------

    gl_FragColor = vec4(rgb, 1.0);
}
