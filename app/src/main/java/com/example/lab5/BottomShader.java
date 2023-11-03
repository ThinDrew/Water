package com.example.lab5;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BottomShader {
    public int programHandle;

    float[] modelViewProjectionMatrix;

    FloatBuffer floatBuffer;
    FloatBuffer av_Buffer;

    ShortBuffer indexBuffer;

    private int vertex_location;
    private int modelview_location;
    private int avtext_location;
    private int texture_location;

    int[] textures = new int[1];
    public BottomShader(float[] modelViewProjectionMatrix, FloatBuffer floatBuffer, Bitmap bitmap) {

        float[] f = {
                1,0,
                1,1,
                0,1,
                0,0
        };

        short index[] = {
                0, 1, 2, 0, 1, 3
        };



        ByteBuffer bf = ByteBuffer.allocateDirect(f.length*4);
        bf.order(ByteOrder.nativeOrder());


        av_Buffer = bf.asFloatBuffer();
        av_Buffer.put(f);
        av_Buffer.position(0);

        bf = ByteBuffer.allocateDirect(index.length*2);
        bf.order(ByteOrder.nativeOrder());

        indexBuffer = bf.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

        this.modelViewProjectionMatrix = modelViewProjectionMatrix;
        this.floatBuffer = floatBuffer;
        int vertex = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        String vertex_code = "varying vec3 p;\n" +
                "varying vec2 av_text;\n" +
                "uniform mat4 modelViewProjectionMatrix;\n" +
                "attribute vec3 a_vertex;\n" +
                "attribute vec2 a_texture;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "p = a_vertex;\n" +
                "av_text = a_texture;\n" +
                "gl_Position = (modelViewProjectionMatrix * vec4(a_vertex, 1.0));\n" +
                "}";
        GLES20.glShaderSource(vertex, vertex_code);
        GLES20.glCompileShader(vertex);

        int fragment = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        String fragment_code =
                "precision mediump float;\n"+
                "varying vec2 av_text;\n" +
                        "uniform sampler2D u_texture;\n" +
                        "void main()\n" +
                        "{\n" +
                        "gl_FragColor = texture2D(u_texture, av_text);\n" +
                        "}";
        GLES20.glShaderSource(fragment, fragment_code);
        GLES20.glCompileShader(fragment);

        int compiled[] = new int[1];
        GLES20.glGetShaderiv(fragment, GLES20.GL_COMPILE_STATUS,compiled, 0);
        if(compiled[0]==0) {
            String error = GLES20.glGetShaderInfoLog(fragment);
            throw new RuntimeException(error);
        }

        programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertex);
        GLES20.glAttachShader(programHandle, fragment);
        GLES20.glLinkProgram(programHandle);

        GLES20.glUseProgram(programHandle);

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        vertex_location = GLES20.glGetAttribLocation(programHandle, "a_vertex");
        modelview_location = GLES20.glGetUniformLocation(programHandle, "modelViewProjectionMatrix");
        avtext_location = GLES20.glGetAttribLocation(programHandle, "a_texture");
        texture_location = GLES20.glGetUniformLocation(programHandle, "u_texture");


    }

    void draw() {

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glUseProgram(programHandle);
        GLES20.glEnableVertexAttribArray(avtext_location);
        GLES20.glVertexAttribPointer(avtext_location, 2,GLES20.GL_FLOAT, false, 0, av_Buffer);

        GLES20.glUniform1i(texture_location, 0);

        GLES20.glUseProgram(programHandle);
        GLES20.glUniformMatrix4fv(modelview_location, 1, false, modelViewProjectionMatrix, 0);

        GLES20.glUseProgram(programHandle);
        GLES20.glEnableVertexAttribArray(vertex_location);
        GLES20.glVertexAttribPointer(vertex_location, 3,GLES20.GL_FLOAT, false, 0, floatBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0,4);

        GLES20.glDisableVertexAttribArray(vertex_location);
        GLES20.glDisableVertexAttribArray(avtext_location);
    }
}
