package io.idml.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * This class exists because all the fun parts of java.util.UUID are private and I can't copy paste the logic into scala
 * reliably.
 */
public class PtolemyUUID {
    public static UUID nameUUIDFromBytes5(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("SHA-1 not supported", nsae);
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] &= 0x0f;  /* clear version        */
        md5Bytes[6] |= 0x50;  /* set to version 5     */
        md5Bytes[8] &= 0x3f;  /* clear variant        */
        md5Bytes[8] |= 0x80;  /* set to IETF variant  */
        return create(md5Bytes);
    }

    public static UUID create(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }
}
