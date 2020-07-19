#version 150 core

in vec2 Pos;
in vec2 TexCoord;
uniform sampler2D tex;
out vec4 outColor;

void main()
{
    float gamma = 2.2;
    vec4 color = texture(tex, TexCoord);
    outColor.rgb = pow(color.rgb, vec3(1.0/gamma));
    outColor.a = color.a;
}