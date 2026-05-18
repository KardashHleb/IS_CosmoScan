package com.cosmoscan.analysis.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WordFrequencyServiceTest {

    private final WordFrequencyService service = new WordFrequencyService();

    @Test
    void topTerms_countsRepeatedWordsAndIgnoresStopWords() {
        String text = "The cosmos cosmos orbit orbit orbit space and the stars";

        List<WordFrequencyService.WeightedTerm> terms = service.topTerms(text, 10);

        assertThat(terms).extracting(WordFrequencyService.WeightedTerm::text)
                .contains("cosmos", "orbit", "space", "stars");
        assertThat(terms).extracting(WordFrequencyService.WeightedTerm::text)
                .doesNotContain("the", "and");
        assertThat(terms.get(0).text()).isEqualTo("orbit");
        assertThat(terms.get(0).weight()).isEqualTo(3);
    }

    @Test
    void topTerms_supportsCyrillic() {
        String text = "космос космос орбита звезды звезды звезды";

        List<WordFrequencyService.WeightedTerm> terms = service.topTerms(text, 5);

        assertThat(terms.get(0).text()).isEqualTo("звезды");
        assertThat(terms.get(0).weight()).isEqualTo(3);
        assertThat(terms).extracting(WordFrequencyService.WeightedTerm::text)
                .contains("космос", "орбита");
    }

    @Test
    void topTerms_blankText_returnsEmptyList() {
        assertThat(service.topTerms("   ")).isEmpty();
    }
}
