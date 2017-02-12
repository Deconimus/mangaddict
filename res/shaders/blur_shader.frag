#version 420
 
uniform sampler2D uTexture;
uniform vec2 uShift;

const int gaussianRadius = 11;
const lowp float gaussianKernel[gaussianRadius] = float[gaussianRadius](0.0402,0.0623,0.0877,0.1120,0.1297,0.1362,0.1297,0.1120,0.0877,0.0623,0.0402);
 
void main() {

	lowp vec2 texCoord = gl_TexCoord[0].xy - float(int(gaussianRadius/2)) * uShift;
	lowp vec3 color = vec3(0.0, 0.0, 0.0);

	for (int i = 0; i < gaussianRadius; ++i) { 

		color += gaussianKernel[i] * texture2D(uTexture, texCoord).xyz;
		texCoord += uShift;

	}

	gl_FragColor = vec4(color,1.0);
	
}
