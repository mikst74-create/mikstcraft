package ru.mikst74.mikstcraft.render.opengl;

import ru.mikst74.mikstcraft.settings.GameProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;

public class ShaderCreator {
    /**
     * Create and compile a shader object of the given type, with source from the given classpath
     * resource.
     * <p>
     * Also, add <code>#define</code>s which can be added to the code at a given location using the
     * custom <code>#pragma {{DEFINES}}</code>.
     */
    public static int createShader(String resource, int type, Map<String, String> defines)   {
        int shader = glCreateShader(type);
        try (InputStream is = ShaderCreator.class.getClassLoader().getResourceAsStream(resource); InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(isr)) {
            String lines = br.lines().collect(Collectors.joining("\n"));
            lines = lines.replace("#pragma {{DEFINES}}", defines.entrySet().stream().map(e -> "#define " + e.getKey() + " " + e.getValue()).collect(Collectors.joining("\n")));
            glShaderSource(shader, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        glCompileShader(shader);
        if (GameProperties.DEBUG || GameProperties.GLDEBUG) {
            int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
            String log = glGetShaderInfoLog(shader);
            if (log.trim().length() > 0) {
                System.err.println(log);
            }
            if (compiled == 0) {
                throw new AssertionError("Could not compile shader: " + resource);
            }
        }
        return shader;
    }

}
