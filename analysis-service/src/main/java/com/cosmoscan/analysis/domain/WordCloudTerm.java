package com.cosmoscan.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class WordCloudTerm {

    @Column(name = "term", nullable = false)
    private String text;

    @Column(name = "weight", nullable = false)
    private int weight;

    protected WordCloudTerm() {
    }

    public WordCloudTerm(String text, int weight) {
        this.text = text;
        this.weight = weight;
    }

    public String getText() {
        return text;
    }

    public int getWeight() {
        return weight;
    }
}
