package com.cosmoscan.e2e.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Test files. Fixtures in {@code src/test/resources/files} each have one clearly dominant word,
 * so the word cloud can be checked:
 * <ul>
 *   <li>{@code cosmos-notes.txt}: "cosmos" x4</li>
 *   <li>{@code satellite-report.docx}: "satellite" x4</li>
 *   <li>{@code telescope-log.pdf}: "telescope" x4</li>
 * </ul>
 */
public final class TestFiles {

    public static final long MAX_FILE_SIZE_BYTES = 1_048_576L;

    public static final String TXT_CONTENT_TYPE = "text/plain";
    public static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String PDF_CONTENT_TYPE = "application/pdf";

    private TestFiles() {
    }

    public static byte[] fixture(String name) {
        try (InputStream in = TestFiles.class.getResourceAsStream("/files/" + name)) {
            if (in == null) {
                throw new IllegalArgumentException("Fixture not found: " + name);
            }
            return in.readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] text(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    /** Plain-text content of exactly the given size. */
    public static byte[] ofSize(long sizeBytes) {
        byte[] content = new byte[Math.toIntExact(sizeBytes)];
        Arrays.fill(content, (byte) 'a');
        return content;
    }
}
