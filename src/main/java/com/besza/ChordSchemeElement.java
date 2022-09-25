package com.besza;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChordSchemeElement {
    private final String name;
    private final Integer position;
    private final int drawnFrets = 6;
    private final Set<Integer> mutedStrings;
    private final Set<Integer> openStrings;
    private final List<Fingering> fingerings;

    private ChordSchemeElement(ChordSchemeElementBuilder builder) {
        this.name = builder.name;
        this.position = builder.position;
        this.mutedStrings = builder.mutedStrings;
        this.openStrings = builder.openStrings;
        this.fingerings = builder.fingerings;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("\\chordscheme[");
        builder.append("fret-number = %d, ".formatted(drawnFrets));
        builder.append("name = %s, ".formatted(name));
        if (position != null) {
            builder.append("position = %d, ".formatted(position));
        }
        if (mutedStrings != null) {
            builder.append("mute = {%s}, ".formatted(collectionToString(mutedStrings)));
        }

        if (openStrings != null) {
            builder.append("ring = {%s}, ".formatted(collectionToString(openStrings)));
        }

        if (fingerings != null) {
            builder.append("finger = {%s}".formatted(collectionToString(fingerings)));
        }
        return builder.append("]").toString();
    }

    private String collectionToString(Collection<?> col) {
        return col.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public static class ChordSchemeElementBuilder {
        private final String name;
        private Integer position;
        private Set<Integer> mutedStrings;
        private Set<Integer> openStrings;
        private List<Fingering> fingerings;

        public ChordSchemeElementBuilder(String name) {
            this.name = name;
        }

        public ChordSchemeElementBuilder position(Integer position) {
            this.position = position;
            return this;
        }

        public ChordSchemeElementBuilder mutedStrings(Set<Integer> mutedStrings) {
            this.mutedStrings = mutedStrings;
            return this;
        }

        public ChordSchemeElementBuilder fingerings(List<Fingering> fingerings) {
            this.fingerings = fingerings;
            return this;
        }

        public ChordSchemeElementBuilder openStrings(Set<Integer> openStrings) {
            this.openStrings = openStrings;
            return this;
        }
    
        public ChordSchemeElement build() {
            validate();
            return new ChordSchemeElement(this);
        }

        private void validate() {
            if (position != null && (position < 1 || position > 8))
                throw new IllegalArgumentException("Chord diagram position must be between 1 and 8 inclusive but it is " + position);
        }
    }
}
