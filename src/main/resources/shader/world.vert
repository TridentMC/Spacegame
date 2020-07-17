#version 330 core

in vec3 position;
in vec3 normal;

out vec2 TexCoords;
out vec3 Normal;
out vec3 FragPos;

uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;

void main()
{
    vec4 worldPos = model * vec4(position, 1.0);
    TexCoords = vec2(0.0, 0.0);
    Normal = mat3(transpose(inverse(model))) * normal;
    FragPos = worldPos.xyz;

    gl_Position = proj * view * worldPos;
}