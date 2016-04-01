package com.github.atomsponge.skyblockmp.util;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author AtomSponge
 */
@UtilityClass
public class UuidUtils {
    public static UUID getUuidFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public static byte[] getBytesFromUuid(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }
}
