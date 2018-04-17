package org.orego.app.face3dActivity.model3D.portrait.personModel;

import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * @author Igor Gulkin 16.04.2018
 * <p>
 * Класс Shader создает из шейдерную программу и хранит её в себе,
 * Эта программа будет непосредственно использоваться OpenGL
 * для отрисовки графического изображения.
 */

public final class Shader {

    private static final Logger LOG = Logger.getLogger(Shader.class.getName());

    /**
     * Поле isReady указывает готовность сборки данного шейдера.
     */

    private boolean isReady;

    /**
     * Поля vertexShaderSource и fragmentShaderSource хранят в себе
     * шейдерные программы в виде одной строки.
     */

    private String vertexShaderSource;

    private String fragmentShaderSource;

    /**
     * Поле program хранит шейдерную программу,
     * к которой OpenGL будет обращаться через тип int.
     */

    private int program;

    /**
     * Конструктор при помощи OpenGL собирает шейдерную программу
     *
     * @param vertexShaderInputStream   указывает путь к файлу вершинного шейдера
     * @param fragmentShaderInputStream указывает путь к файлу фрагментного шейдера.
     */

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
            int vertexProgram = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexProgram, vertexShaderSource);
            GLES20.glCompileShader(vertexProgram);

            //Фрагментный шейдер:
            int fragmentProgram = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentProgram,fragmentShaderSource);
            GLES20.glCompileShader(fragmentProgram);

            //Шейдерная программа:
            this.program = GLES20.glCreateProgram();
            GLES20.glAttachShader(this.program, vertexProgram);
            GLES20.glAttachShader(this.program, fragmentProgram);
            GLES20.glLinkProgram(this.program);

            //Удаляем шейдеры:
            GLES20.glDeleteShader(vertexProgram);
            GLES20.glDeleteShader(fragmentProgram);
            this.isReady = true;
        } catch (final FileNotFoundException e) {
            this.isReady = false;
            this.vertexShaderSource = "NULL";
            this.fragmentShaderSource = "NULL";
            LOG.info("Не удалось найти указанные шейдеры");
        }
    }

    /**
     * @param isShader inputStream файла
     * @return шейдерную программу в виде строки
     * @throws FileNotFoundException, если путь к файлу не найден.
     */

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

    /**
     * convertToString() преобразует массив одну строку
     *
     * @param shaderStrings указывает массив строк List<String>
     * @return строку.
     */

    private String convertToString(final List<String> shaderStrings) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final String shaderString : shaderStrings) {
            stringBuilder.append(shaderString);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


    /**
     * @return готовность шейдерной программы.
     */

    public final boolean isReady() {
        return isReady;
    }

    /**
     * use() - указывает OpenGL использовать текущую программу.
     */

    public final void use() {
        //Раскомментируйте эту строчку, когда подключите OpenGL:
        GLES20.glUseProgram(this.program);
    }

    /**
     * toString() разработан для отладки программы,
     * если Вам нужно посмотреть исходный код шейдеров.
     *
     * @return исходный код шейдеров. Примечание: этот метод разработан для вывода на консоль.
     * Используйте System.out.println(), JUL, Log4j or slf4j.
     */

    @Override
    public final String toString() {
        return "\nVertex shader:\n\n" + vertexShaderSource + "\nFragment shader\n\n" + fragmentShaderSource;
    }
}
