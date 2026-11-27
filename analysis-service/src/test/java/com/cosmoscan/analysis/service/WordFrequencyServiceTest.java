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
    void topTerms_supportsNonAsciiLetters() {
        String text = "Café café CAFÉ naïve naïve résumé";

        List<WordFrequencyService.WeightedTerm> terms = service.topTerms(text, 5);

        assertThat(terms).containsExactly(
                new WordFrequencyService.WeightedTerm("café", 3),
                new WordFrequencyService.WeightedTerm("naïve", 2),
                new WordFrequencyService.WeightedTerm("résumé", 1));
    }

    @Test
    void topTerms_blankText_returnsEmptyList() {
        assertThat(service.topTerms("   ")).isEmpty();
    }

    @Test
    void topTerms_nullText_returnsEmptyList() {
        assertThat(service.topTerms(null)).isEmpty();
    }

    @Test
    void topTerms_dropsShortWordsAndStopWords() {
        List<WordFrequencyService.WeightedTerm> terms = service.topTerms("go to it with orbit");

        assertThat(terms).extracting(WordFrequencyService.WeightedTerm::text).containsExactly("orbit");
    }

    @Test
    void topTerms_equalWeights_areSortedAlphabeticallyAndLimited() {
        List<WordFrequencyService.WeightedTerm> terms = service.topTerms("zeta beta alpha", 2);

        assertThat(terms).extracting(WordFrequencyService.WeightedTerm::text).containsExactly("alpha", "beta");
    }
}
