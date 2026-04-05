package com.ptithcm.smartshop.shared.util;

import java.text.Normalizer;
import java.util.Locale;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

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

    public static String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }

        return sb.toString();
    }
}

