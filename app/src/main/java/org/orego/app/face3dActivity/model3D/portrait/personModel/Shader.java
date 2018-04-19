package org.orego.app.face3dActivity.model3D.portrait.personModel;

import android.opengl.GLES31;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class Shader {

    private static final Logger LOG = Logger.getLogger(Shader.class.getName());

    private boolean isReady;

    private String vertexShaderSource;

    private String fragmentShaderSource;

    private int program;

    public Shader(final InputStream vertexShaderInputStream
            , final InputStream fragmentShaderInputStream) {
        //Подключаемся к шейдерам:
        try {
            //Преобразуем в строку:
            this.vertexShaderSource = this.loadShader(vertexShaderInputStream);
            this.fragmentShaderSource = this.loadShader(fragmentShaderInputStream);

            //Дальше идет работа с OpenGL (когда перенесете на Android раскомментируйте эти строчки):
            int success = 0, errors = 0;
            char infoLog[];

            //Вершинный шейдер:
            int vertexProgram = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
            GLES31.glShaderSource(vertexProgram, vertexShaderSource);
            GLES31.glCompileShader(vertexProgram);

            //Фрагментный шейдер:
            int fragmentProgram = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
            GLES31.glShaderSource(fragmentProgram,fragmentShaderSource);
            GLES31.glCompileShader(fragmentProgram);

            //Шейдерная программа:
            this.program = GLES31.glCreateProgram();
            GLES31.glAttachShader(this.program, vertexProgram);
            GLES31.glAttachShader(this.program, fragmentProgram);
            GLES31.glLinkProgram(this.program);

            this.isReady = true;
        } catch (final FileNotFoundException e) {
            this.isReady = false;
            this.vertexShaderSource = "NULL";
            this.fragmentShaderSource = "NULL";
            LOG.info("Не удалось найти указанные шейдеры");
        }
    }

    private String loadShader(final InputStream isShader) throws FileNotFoundException {
        final List<String> shaderStrings = new ArrayList<>();
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(isShader))) {
            while ((line = bufferedReader.readLine()) != null) {
                shaderStrings.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException();
        }
        return convertToString(shaderStrings);
    }

    private String convertToString(final List<String> shaderStrings) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final String shaderString : shaderStrings) {
            stringBuilder.append(shaderString);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public final boolean isReady() {
        return isReady;
    }

    public final int getProgramm() {
        //Раскомментируйте эту строчку, когда подключите OpenGL:
        return program;
    }

    @Override
    public final String toString() {
        return "\nVertex shader:\n\n" + vertexShaderSource + "\nFragment shader\n\n" + fragmentShaderSource;
    }
}
