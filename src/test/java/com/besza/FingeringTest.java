package com.besza;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FingeringTest {

    @Test
    void thirdFretOfTheFifthString() {
        assertEquals("3/5:C", new Fingering(3, 5, "C").toString());
    }
}
