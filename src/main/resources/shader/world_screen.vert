#version 150 core

in vec3 position;
in vec2 texCoord;
out vec3 Pos;
out vec2 TexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;

void main()
{
    Pos = position;
    TexCoord = texCoord;
    gl_Position = proj * view * model * vec4(position, 1.0);
}