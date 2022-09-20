package com.besza;

import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.IntStream;


@ApplicationScoped
public class Chordie {

    private final String chordSchemeTemplate = """
            \\documentclass{article}
            \\usepackage{guitarchordschemes}
            \\thispagestyle{empty}
            \\begin{document}
                        
            \\setchordscheme{
            tuning = {e,B,G,D,A,E}
            }
                        
            %s
                        
            \\end{document}
            """;

    byte[] transform(String name,
                     String notation,
                     int position,
                     String labels) {
        if ((name == null || name.isBlank()) && (notation == null || notation.isBlank())) {
            throw new IllegalArgumentException("Chord name and its shorthand notation must be provided");
        }

        var mutedStrings = new HashSet<Integer>();
        var openStrings = new HashSet<Integer>();
        var fingerings = new ArrayList<Fingering>();

        IntStream.range(0, notation.length()).forEach(i -> {
            switch (notation.charAt(i)) {
                case 'x', 'X' -> mutedStrings.add(6 - i);
                case '0' -> openStrings.add(6 - i);
                default -> fingerings.add(new Fingering(notation.charAt(i) - 48 - (position - 1), 6 - i, ""));
            }
        });

        var chordScheme = new ChordSchemeElement.ChordSchemeElementBuilder(name)
                .fingerings(fingerings)
                .mutedStrings(mutedStrings)
                .openStrings(openStrings)
                .position(position)
                .build();

        Log.debug(chordScheme);

        try {
            Path p = Files.createTempFile("temp", ".tex");
            Log.infov("Created new TeX source file {0}", p.getFileName());
            try (var output = Files.newBufferedWriter(p)) {
                output.write(chordSchemeTemplate.formatted(chordScheme));
                output.flush();
                var process = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", "-halt-on-error", p.getFileName().toString())
                        .directory(new File("/tmp"))
                        .inheritIO()
                        .start();
                process.waitFor();
                var result = Paths.get("/tmp", p.getFileName().toString().replace(".tex", ".pdf"));
                var inputStream = Files.newInputStream(result);
                return inputStream.readAllBytes();
            } catch (InterruptedException e) {
                Log.error("Thread interrupted while waiting for pdflatex", e);
            }
        } catch (IOException e) {
            Log.error("Failed to create temp file for the TeX source", e);
        }

        return new byte[]{};
    }
}
