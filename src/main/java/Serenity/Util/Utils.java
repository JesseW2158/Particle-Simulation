package Serenity.Util;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Utils {
    public static FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data).flip();
        return buffer;
    }
}
