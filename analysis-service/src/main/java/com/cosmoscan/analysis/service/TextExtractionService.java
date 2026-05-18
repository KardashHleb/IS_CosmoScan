package com.cosmoscan.analysis.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TextExtractionService {

    public String extract(byte[] fileContent, String originalFileName)
            throws IOException, SAXException, TikaException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        if (originalFileName != null && !originalFileName.isBlank()) {
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFileName);
        }

        try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
            parser.parse(inputStream, handler, metadata);
        }
        return handler.toString();
    }
}
