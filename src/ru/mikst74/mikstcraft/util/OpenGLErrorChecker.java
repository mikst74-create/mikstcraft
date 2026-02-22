package ru.mikst74.mikstcraft.util;

import static org.lwjgl.opengl.GL11.*;

public class OpenGLErrorChecker {
/*
GL_NO_ERROR (0): No error has been recorded.
GL_INVALID_ENUM (1280): An unacceptable value is specified for an enumerated argument.
GL_INVALID_VALUE (1281): A numeric argument is outside the acceptable range.
GL_INVALID_OPERATION (1282): The specified operation is not allowed in the current state.
GL_INVALID_FRAMEBUFFER_OPERATION (1286): The command is not allowed with the current framebuffer configuration.
GL_OUT_OF_MEMORY (1285): There is not enough memory left to execute the command.
LWJGL Forum
LWJGL Forum
 +4
 */
    // Helper function to check for and print OpenGL errors
    public static void check(int i) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            System.err.println(i+": OpenGL error: " + error);
            // You can use a utility function or a switch statement
            // to get a descriptive string for the error code
            // Example: "OpenGL error: GL_INVALID_ENUM"
        }
    }

//    // Example usage within your rendering/setup code
//    public void setup() {
//        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
//        check(); // Check for errors after glClearColor
//
//        // Example of a call that causes an error (using invalid constant GL_COLOR)
//        // glClear(GL_COLOR); // This would generate GL_INVALID_ENUM
//        glClear(GL_COLOR_BUFFER_BIT); // Correct usage
//        check(); // Check for errors after glClear
//    }
}