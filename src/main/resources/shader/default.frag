#version 150 core

in vec2 Pos;
in vec2 TexCoord;
uniform sampler2D tex;
out vec4 outColor;

void main()
{
    outColor = texture(tex, TexCoord);
}