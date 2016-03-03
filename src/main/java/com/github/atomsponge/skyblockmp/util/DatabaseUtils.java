package com.github.atomsponge.skyblockmp.util;

import lombok.experimental.UtilityClass;

/**
 * @author AtomSponge
 */
@UtilityClass
public class DatabaseUtils {
    public static void closeAll(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to close " + closeable.getClass().getSimpleName(), e);
            }
        }
    }
}
