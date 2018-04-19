uniform mat4 u_MVPMatrix;
attribute vec4 a_Position;
attribute vec3 vColor;
uniform mat4 u_MVMatrix;
uniform vec3 u_LightPos;
attribute vec3 a_Normal;
varying vec4 v_Color;
void main() {
   v_Color[0] = vColor[0];
   v_Color[1] = vColor[1];
   v_Color[2] = vColor[2];
   v_Color[3] = 1.0;
  gl_Position = u_MVPMatrix * a_Position;
  gl_PointSize = 0.5;
}