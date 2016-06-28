#version 150 core


in vec4 pass_Color;

out vec4 out_Color;

void main(void) {
	out_Color = pass_Color;
	// Override out_Color with our texture pixel
//	out_Color = texture(texture_diffuse, pass_TextureCoord);
}