#version 150 core

in vec2 position;
in vec2 texCoord;

out vec2 Pos;
out vec2 TexCoord;

void main()
{
    Pos = position;
    TexCoord = texCoord;
    gl_Position = vec4(position, 0.0, 1.0);
}