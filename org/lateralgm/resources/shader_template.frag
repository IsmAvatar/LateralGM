/**
 * A simple passthrough fragment shader for GLSLES version 100.
 * @author John Doe
 */

varying vec2 v_TextureCoord;
varying vec4 v_Colour;

void main() {
    // write the fragment color by combining vertex color and texture
    gl_FragColor = v_Colour * texture2D(gm_BaseTexture, v_TextureCoord);
}
