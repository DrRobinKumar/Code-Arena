package com.codearena.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private SlugUtil() {
    }

    /** "Two Sum!" -> "two-sum"; "Café Ünïcode" -> "cafe-unicode". */
    public static String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // strip accents
                .toLowerCase();
        String slug = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-");
        return EDGE_HYPHENS.matcher(slug).replaceAll("");
    }
}
