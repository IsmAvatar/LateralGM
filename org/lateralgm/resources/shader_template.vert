/**
 * A simple passthrough vertex shader for GLSLES version 100.
 * @author John Doe
 */

attribute vec2 in_Position;       //< (x,y)
attribute vec2 in_TextureCoord;   //< (u,v)
attribute vec4 in_Colour;         //< (r,g,b,a)

varying vec2 v_TextureCoord;
varying vec4 v_Colour;

void main() {
	// transform the vertex coordinates to screen space
	vec4 model_pos = vec4(in_Position.x, in_Position.y, 1.0, 1.0);
	gl_Position = gm_Matrices[MATRIX_WORLD_VIEW_PROJECTION] * model_pos;

	// pass the texture coords and color through to the fragment shader
	v_TextureCoord = in_TextureCoord;
	v_Colour = in_Colour;
}
