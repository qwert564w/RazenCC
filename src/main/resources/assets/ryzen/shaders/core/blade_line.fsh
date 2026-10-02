#version 330 core

in vec4 vertexColor;

out vec4 finalColor;

void main() {
    if (vertexColor.a < 0.01) {
        discard;
    }
    finalColor = vertexColor;
}
