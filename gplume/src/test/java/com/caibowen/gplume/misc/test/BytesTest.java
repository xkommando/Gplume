package com.caibowen.gplume.misc.test;

import com.caibowen.gplume.misc.Bytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class BytesTest {

    @Test
    public void testBytes2ints() throws Exception {
        int[] is = new int[]{1, 2, 3, 4, 5, Integer.MAX_VALUE, Integer.MIN_VALUE, -1};
        byte[] bs = Bytes.ints2bytes(is);

        int[] nis = Bytes.bytes2ints(bs);
        for (int i = 0; i < nis.length; i++) {
            System.out.println(nis[i]);
        }
    }
}