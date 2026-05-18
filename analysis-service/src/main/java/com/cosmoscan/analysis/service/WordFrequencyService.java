package com.cosmoscan.analysis.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WordFrequencyService {

    private static final int DEFAULT_TOP_TERMS = 100;
    private static final int MIN_WORD_LENGTH = 3;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\p{L}[\\p{L}\\p{M}\\p{N}]*",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "been", "but", "by", "for", "from", "had",
            "has", "have", "he", "her", "his", "in", "is", "it", "its", "of", "on", "or", "that",
            "the", "their", "there", "they", "this", "to", "was", "were", "will", "with", "you",
            "your",
            "а", "без", "более", "больше", "будет", "будто", "бы", "был", "была", "были", "было",
            "быть", "в", "вам", "вас", "весь", "во", "вот", "все", "всего", "всех", "вы", "где",
            "да", "даже", "для", "до", "его", "ее", "ей", "ему", "если", "есть", "еще", "же",
            "за", "здесь", "и", "из", "или", "им", "их", "к", "как", "ко", "когда", "кто", "ли",
            "либо", "мне", "может", "мы", "на", "надо", "наш", "не", "него", "нее", "нет", "ни",
            "них", "но", "ну", "о", "об", "однако", "он", "она", "они", "оно", "от", "очень",
            "по", "под", "при", "про", "с", "со", "так", "также", "такой", "там", "те", "тем",
            "то", "того", "тоже", "той", "только", "том", "ты", "у", "уже", "хотя", "чего", "чем",
            "через", "что", "чтобы", "чуть", "эта", "эти", "этого", "этой", "этом", "этот", "эту",
            "я"
    );

    public List<WeightedTerm> topTerms(String text) {
        return topTerms(text, DEFAULT_TOP_TERMS);
    }

    public List<WeightedTerm> topTerms(String text, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        Map<String, Integer> frequencies = new HashMap<>();
        var matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = normalizeToken(matcher.group());
            if (token.length() < MIN_WORD_LENGTH || STOP_WORDS.contains(token)) {
                continue;
            }
            frequencies.merge(token, 1, Integer::sum);
        }

        return frequencies.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> new WeightedTerm(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String normalizeToken(String token) {
        return token.replace('ё', 'е');
    }

    public record WeightedTerm(String text, int weight) {
    }
}
