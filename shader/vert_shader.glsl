#version 150 core

in vec4 in_Position;

out vec4 pass_Color;

void main(void) {
	gl_Position = in_Position;

	pass_Color = vec4(1f, 0.3f, 0.4f, 1);
}