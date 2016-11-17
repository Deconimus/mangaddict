#version 420
 
uniform sampler2D uTexture;
//uniform vec2 uRenderSize;

/*
float bellFunc(float x) {
	
	float f = ( x / 2.0 ) * 1.5; // Converting -2 to +2 to -1.5 to +1.5
	
	if( f > -1.5 && f < -0.5 ) {
		
		return( 0.5 * pow(f + 1.5, 2.0));
	}
	
	if( f > -0.5 && f < 0.5 ) {
		
		return 3.0 / 4.0 - ( f * f );
	}
	
	if( ( f > 0.5 && f < 1.5 ) ) {
		
		return( 0.5 * pow(f - 1.5, 2.0));
	}
	
	return 0.0;
}

float catMullRom(float x) {
	
    const float B = 0.0;
    const float C = 0.5;
    
    float f = x;
    
    if( f < 0.0 ) {
    	
        f = -f;
    }
    
    if( f < 1.0 ) {
    	
        return ( ( 12 - 9 * B - 6 * C ) * ( f * f * f ) +
            ( -18 + 12 * B + 6 *C ) * ( f * f ) +
            ( 6 - 2 * B ) ) / 6.0;
        
    } else if( f >= 1.0 && f < 2.0 ) {
    	
        return ( ( -B - 6 * C ) * ( f * f * f )
            + ( 6 * B + 30 * C ) * ( f *f ) +
            ( - ( 12 * B ) - 48 * C  ) * f +
            8 * B + 24 * C)/ 6.0;
        
    } else {
    	
        return 0.0;
    }
    
}

vec4 bicubic(sampler2D textureSampler, vec2 TexCoord, vec2 renderSize) {
	
	float fWidth = renderSize.x * 2.0;
	float fHeight = renderSize.y * 2.0;
	
    float texelSizeX = 1.0 / fWidth; //size of one texel 
    float texelSizeY = 1.0 / fHeight; //size of one texel 
    
    vec4 nSum = vec4( 0.0, 0.0, 0.0, 0.0 );
    vec4 nDenom = vec4( 0.0, 0.0, 0.0, 0.0 );
    
    float a = fract( TexCoord.x * fWidth ); // get the decimal part
    float b = fract( TexCoord.y * fHeight ); // get the decimal part
    
    for(int m = -1; m <=2; m++ ) {
        for(int n =-1; n<= 2; n++) {
        	
			vec4 vecData = texture2D(textureSampler, 
                           		TexCoord + vec2(texelSizeX * float( m ), 
								texelSizeY * float( n )));
			
			float f = catMullRom( float( m ) - a );
			vec4 vecCooef1 = vec4( f,f,f,f );
			float f1 = catMullRom( -( float( n ) - b ) );
			vec4 vecCoeef2 = vec4( f1, f1, f1, f1 );
			
            nSum = nSum + ( vecData * vecCoeef2 * vecCooef1  );
            nDenom = nDenom + (( vecCoeef2 * vecCooef1 ));
            
        }
    }
    
    return nSum / nDenom;
}
*/

vec4 cubic(float v){
    
    vec4 n = vec4(1.0, 2.0, 3.0, 4.0) - v;
    vec4 s = n * n * n;
    
    float x = s.x;
    float y = s.y - 4.0 * s.x;
    float z = s.z - 4.0 * s.y + 6.0 * s.x;
    float w = 6.0 - x - y - z;
    
    return vec4(x, y, z, w) / 6.0;
}

vec4 bicubic(sampler2D sampler, vec2 texCoords){

   vec2 texSize = textureSize(sampler, 0) * 2.0;
   vec2 invTexSize = 1.0 / texSize;
   
   texCoords = texCoords * texSize - 0.5;

   
    vec2 fxy = fract(texCoords);
    texCoords -= fxy;

    vec4 xcubic = cubic(fxy.x);
    vec4 ycubic = cubic(fxy.y);

    vec4 c = texCoords.xxyy + vec2(-0.5, +1.5).xyxy;
    
    vec4 s = vec4(xcubic.xz + xcubic.yw, ycubic.xz + ycubic.yw);
    vec4 offset = c + vec4(xcubic.yw, ycubic.yw) / s;
    
    offset *= invTexSize.xxyy;
    
    vec4 sample0 = texture(sampler, offset.xz);
    vec4 sample1 = texture(sampler, offset.yz);
    vec4 sample2 = texture(sampler, offset.xw);
    vec4 sample3 = texture(sampler, offset.yw);

    float sx = s.x / (s.x + s.y);
    float sy = s.z / (s.z + s.w);

    return mix(mix(sample3, sample2, sx), mix(sample1, sample0, sx), sy);
}

void main() {

	if (texture2D(uTexture, gl_TexCoord[0].xy).a < 0.2) { return; }

	gl_FragColor = bicubic(uTexture, gl_TexCoord[0].xy);
}