package com.besza;

public record Fingering(int fretNumber, int string, String label) {

    @Override
    public String toString() {
        return "%d/%d:%s".formatted(fretNumber, string, label);
    }
}
