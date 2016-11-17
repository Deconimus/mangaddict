#version 420
 
uniform sampler2D uTexture;
uniform vec2 uShift;

const int gaussianRadius = 5;
const lowp float gaussianKernel[gaussianRadius] = float[gaussianRadius](0.1732, 0.2128, 0.2279, 0.2128, 0.1732);
 
void main() {

	lowp vec2 texCoord = gl_TexCoord[0].xy - float(int(gaussianRadius/2)) * uShift;
	lowp vec3 color = vec3(0.0, 0.0, 0.0);

	for (int i = 0; i < gaussianRadius; ++i) { 

		color += gaussianKernel[i] * texture2D(uTexture, texCoord).xyz;
		texCoord += uShift;

	}

	gl_FragColor = vec4(color,1.0);
	
}