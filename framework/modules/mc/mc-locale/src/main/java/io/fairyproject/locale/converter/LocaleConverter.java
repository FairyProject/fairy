package io.fairyproject.locale.converter;

import io.fairyproject.config.Converter;
import net.kyori.adventure.translation.Translator;

import java.util.Locale;

public class LocaleConverter implements Converter<Locale, String> {
    @Override
    public String convertTo(Locale element, ConversionInfo info) {
        return element.toString();
    }

    @Override
    public Locale convertFrom(String element, ConversionInfo info) {
        return Translator.parseLocale(element);
    }
}
