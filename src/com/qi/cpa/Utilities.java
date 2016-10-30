package com.qi.cpa;

import java.util.Arrays;

/**
 * Created by root on 30/10/16.
 */

/* Utilities class created to provide some generic functions required during the processing

 */
public  class Utilities {


    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static byte[] concat(byte[] base, byte[] addendum) {
        byte[] result = Arrays.copyOf(base, base.length + addendum.length);
        System.arraycopy(addendum, 0, result, base.length, addendum.length);
        return result;
    }

    public static byte[] padding(byte[] data) {

        data = concat(data, new byte[] { (byte) 0x80 });

        int blockSize = 8;

        if (data.length % blockSize == 0)
            return data;

        byte[] paddedData = Arrays.copyOf(data, data.length + blockSize - (data.length % blockSize));

        return paddedData;
    }


    public static byte[] xor(byte[] a, byte[] b) throws IllegalArgumentException {
        if (b.length != a.length) {
            throw new IllegalArgumentException("a and b do not have the same length");
        }
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }

}
