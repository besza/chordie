package com.besza;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChordSchemeElementTest {

    @Test
    void nameRequired() {
        var expected = "\\chordscheme[fret-number = 6, name = Cmaj, mute = {6}, ring = {3}, finger = {3/5:C, 2/4:E, 1/2:C}]";
        assertEquals(expected,
            new ChordSchemeElement.ChordSchemeElementBuilder("Cmaj")
                    .mutedStrings(Set.of(6))
                    .openStrings(Set.of(3))
                    .fingerings(List.of(new Fingering(3, 5, "C"),
                            new Fingering(2, 4, "E"),
                            new Fingering(1, 2, "C")))
                    .build()
                    .toString()
        );
    }
}
