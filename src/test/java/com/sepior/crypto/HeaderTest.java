package com.sepior.crypto;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeaderTest {

    @Test
    public void testEncryptDecryptCorrespondence() throws IOException {
        SepiorCryptHeader header = new SepiorCryptHeader("resourceId", 1234567324, 845768349);
        byte[] bytes = header.serialize();
        byte[] expected = new byte[] {1, 0, 0, 0, -99, 102, 105, 50, -100, 0, -106, 73, 10, 0, 0, 0, 114, 101, 115, 111, 117, 114, 99, 101, 73, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 69, 80, 67, 82, 89, 80, 84, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        assertTrue(Arrays.equals(expected, bytes));
        assertEquals(SepiorCryptConstants.HEADER_SIZE, bytes.length);
    }
}
