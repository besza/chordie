package com.besza;

import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                     String labels) {
        if ((name == null || name.isBlank()) && (notation == null || notation.isBlank())) {
            throw new IllegalArgumentException("Chord name and its shorthand notation must be provided");
        }

        // assume `position` to be the lowest fret number minus one (unless it is already 1)
        int position = Integer.MAX_VALUE;
        for (var ch : notation.toCharArray()) {
            if (Character.isDigit(ch) && ch != '0') {
                if (ch - '0' <= position) {
                    position = ch - '0';
                }
            }
        }
        position = (position == Integer.MAX_VALUE || position == 1) ? 1 : position - 1;


        var mutedStrings = new HashSet<Integer>();
        var openStrings = new HashSet<Integer>();
        var fingerings = new ArrayList<Fingering>();
        for (var i = 0; i < notation.length(); i++) {
            switch (notation.charAt(i)) {
                case 'x', 'X' -> mutedStrings.add(6 - i);
                case '0' -> openStrings.add(6 - i);
                default -> fingerings.add(new Fingering(notation.charAt(i) - '0' - (position - 1), 6 - i, ""));
            }
        }

        var chordScheme = new ChordSchemeElement.ChordSchemeElementBuilder(name)
                .fingerings(fingerings)
                .mutedStrings(mutedStrings)
                .openStrings(openStrings)
                .position(position)
                .build();

        Log.info(chordScheme);

        try {
            Path source = Files.createTempFile(Path.of(System.getProperty("user.dir")), "chordie_temp", ".tex");
            Log.infov("Created new TeX source file {0}", source.getFileName());
            try (var output = Files.newBufferedWriter(source)) {
                output.write(chordSchemeTemplate.formatted(chordScheme));
                output.flush();
                var latex = new ProcessBuilder("latex", "-interaction=nonstopmode", "-halt-on-error", source.getFileName().toString())
                        .redirectErrorStream(true)
                        .start();
                var latexExitCode = latex.waitFor();
                if (latexExitCode == 0) {
                    var dvisvgm = new ProcessBuilder("dvisvgm", "--exact", "--stdout", "--scale=2,2", source.getFileName().toString().replace(".tex", ".dvi"))
                            .start();
                    var dvisvgmExitCode = dvisvgm.waitFor();
                    if (dvisvgmExitCode == 0) {
                        return dvisvgm.getInputStream().readAllBytes();
                    }
                }
            } catch (InterruptedException e) {
                Log.error("Thread interrupted while waiting for pdflatex", e);
            }
        } catch (IOException e) {
            Log.error("Failed to create temp file for the TeX source", e);
        }

        Log.error("Returning an empty byte array, something is wrong");
        return new byte[]{};
    }

    void cleanUp() {
        Log.info("Starting clean up process");
        try (var listing = Files.newDirectoryStream(
                Path.of(System.getProperty("user.dir")),
                path -> path.getFileName().toString().startsWith("chordie_temp"))
        ) {
            for (var path : listing) {
                Log.infov("Deleting temp file {0}", path.getFileName());
                Files.delete(path);
            }
        } catch (IOException ex) {
            Log.error("Failed to clean up temporary files", ex);
        }
    }
}
