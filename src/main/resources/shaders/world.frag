#version 150 core

in vec3 Pos;
in vec3 Normal;
in vec3 FragPos;
out vec4 outColor;

uniform vec3 lightPos;

void main()
{
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * (lightPos / 10.0);
    float distance = length(lightPos - FragPos);
    float attenuation = 1.0 / (1.0f + (0.022 * distance) + (0.0019 * (distance * distance)));
    diffuse *= attenuation;
    vec3 result = diffuse;
    outColor = vec4(result, 1.0);
}