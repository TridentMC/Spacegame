#version 150 core

out vec4 outColor;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform vec3 gScreenSize;

uniform vec3 lightPos;
uniform vec3 lightCol;
uniform vec3 lightAtten;

void main()
{
    vec2 TexCoords = gl_FragCoord.xy / gScreenSize.xy;
    vec3 FragPos = texture(gPosition, TexCoords).rgb;
    vec3 Normal = texture(gNormal, TexCoords).rgb;
    vec3 Albedo = texture(gAlbedo, TexCoords).rgb;

    vec3 ambient = Albedo * 0.1;

    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * Albedo * lightCol;
    float distance = length(lightPos - FragPos);
    float attenuation = 1.0 / (lightAtten.x + (lightAtten.y * distance) + (lightAtten.z * (distance * distance)));
    diffuse *= attenuation;
    vec3 result = ambient + diffuse;

    outColor = vec4(result, 1.0);
}