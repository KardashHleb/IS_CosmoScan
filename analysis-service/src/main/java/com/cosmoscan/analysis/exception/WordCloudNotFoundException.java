package com.cosmoscan.analysis.exception;

import java.util.UUID;

public class WordCloudNotFoundException extends RuntimeException {

    public WordCloudNotFoundException(UUID workId) {
        super("Word cloud not found for work: " + workId);
    }
}
