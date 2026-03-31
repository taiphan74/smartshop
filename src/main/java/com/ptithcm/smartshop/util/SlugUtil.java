package com.ptithcm.smartshop.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");

    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Bước 1: normalize (bỏ dấu tiếng Việt) và thay thế 'đ'/'Đ'
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{M}", "");
        slug = slug.replace("đ", "d").replace("Đ", "D");

        // Bước 2: lowercase
        slug = slug.toLowerCase(Locale.ENGLISH);

        // Bước 3: thay space -> "-"
        slug = WHITESPACE.matcher(slug).replaceAll("-");

        // Bước 4: remove ký tự đặc biệt (chỉ giữ lại chữ cái, số và dấu gạch ngang)
        slug = NON_LATIN.matcher(slug).replaceAll("-");

        // Bước 5: remove multiple "-"
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");

        // Bước 6: trim "-"
        if (slug.startsWith("-")) {
            slug = slug.substring(1);
        }
        if (slug.endsWith("-")) {
            slug = slug.substring(0, slug.length() - 1);
        }

        return slug;
    }
}
