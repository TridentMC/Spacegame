#version 150 core

out vec4 outColor;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform vec3 gScreenSize;

uniform vec3 lightCol;
uniform vec3 lightDirection;

void main()
{
    vec2 TexCoords = gl_FragCoord.xy / gScreenSize.xy;
    vec3 FragPos = texture(gPosition, TexCoords).rgb;
    vec3 Normal = texture(gNormal, TexCoords).rgb;
    vec3 Albedo = texture(gAlbedo, TexCoords).rgb;

    vec3 result = Albedo * 0.1;
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(-lightDirection);

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * Albedo * lightCol;
    result += diffuse;

    outColor = vec4(result, 1.0);
}