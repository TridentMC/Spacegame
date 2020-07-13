#version 150 core

in vec3 Pos;
in vec3 Normal;
in vec3 FragPos;
out vec4 outColor;

uniform vec3 lightPos;
uniform vec3 lightCol;

void main()
{
    float ambientStrength = 0.1;
    vec3 ambient = ambientStrength * lightCol;

    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightCol;
    float distance = length(lightPos - FragPos);
    float attenuation = 1.0 / (1.0f + (0.07 * distance) + (0.017 * (distance * distance)));
    diffuse *= attenuation;
    vec3 result = ambient + diffuse;
    outColor = vec4(result, 1.0);
}