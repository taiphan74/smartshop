package com.ptithcm.smartshop;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class UiLayoutTemplateTests {

    private static String readResource(String relativePath) throws IOException {
        return Files.readString(Path.of("src", "main", "resources").resolve(relativePath));
    }

    @Test
    void productHomeUsesSharedLayoutFragments() throws IOException {
        String template = readResource("templates/product/home.html");

        assertThat(template).contains("fragments/head :: head('SmartShop - Sản phẩm')");
        assertThat(template).contains("fragments/header :: header");
        assertThat(template).contains("fragments/footer :: footer");
        assertThat(template).doesNotContain("<style>");
    }

    @Test
    void sharedHeaderExposesThemeToggle() throws IOException {
        String header = readResource("templates/fragments/header.html");

        assertThat(header).contains("data-theme-toggle");
        assertThat(header).contains("localStorage.setItem('smartshop-theme'");
        assertThat(header).contains("document.documentElement.setAttribute('data-theme'");
    }

    @Test
    void sharedStylesDefineLightAndDarkThemeTokens() throws IOException {
        String css = readResource("static/css/app.css");

        assertThat(css).contains(":root");
        assertThat(css).contains("[data-theme=\"dark\"]");
        assertThat(css).contains("--color-bg");
        assertThat(css).contains(".site-header");
        assertThat(css).contains(".theme-toggle");
    }
}
