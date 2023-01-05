package com.janboerman.invsee.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UUIDTest {

    @Test
    public void testDashing() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid, UUIDHelper.dashed(UUIDHelper.unDashed(uuid)));
    }

    @Test
    public void testCopy() {
        UUID original = UUID.randomUUID();
        UUID copy = UUIDHelper.copy(original);

        assertEquals(original, copy);
        assertNotSame(original, copy);
    }

}
