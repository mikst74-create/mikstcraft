package ru.mikst74.mikstcraft.settings;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class OpenGLProperties {
    /* All the different features we are using */
    public static boolean useMultiDrawIndirect;
    public static boolean useBufferStorage;
    public static boolean useClearBuffer;
    public static boolean drawPointsWithGS;
    public static boolean useInverseDepth;
    public static boolean useNvMultisampleCoverage;
    public static boolean canGenerateDrawCallsViaShader;
    public static boolean useOcclusionCulling;
    public static boolean useTemporalCoherenceOcclusionCulling;
    public static boolean canSourceIndirectDrawCallCountFromBuffer;
    public static boolean useRepresentativeFragmentTest;
    public static boolean canUseSynchronousDebugCallback;


    /* other*/
    public static int uniformBufferOffsetAlignment;
    public static int currentDynamicBufferIndex;

}
