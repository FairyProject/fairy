/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.util;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CC {

    public static final String BLUE;
    public static final String AQUA;
    public static final String YELLOW;
    public static final String RED;
    public static final String GRAY;
    public static final String GOLD;
    public static final String GREEN;
    public static final String WHITE;
    public static final String BLACK;
    public static final String BOLD;
    public static final String ITALIC;
    public static final String UNDER_LINE;
    public static final String STRIKE_THROUGH;
    public static final String RESET;
    public static final String MAGIC;
    public static final String DARK_BLUE;
    public static final String DARK_AQUA;
    public static final String DARK_GRAY;
    public static final String DARK_GREEN;
    public static final String DARK_PURPLE;
    public static final String DARK_RED;
    public static final String PINK;
    public static final String MENU_BAR;
    public static final String CHAT_BAR;
    public static final String SB_BAR;

    public static final char CODE = '§';

    static {

        BLUE = CODE + "9";
        AQUA = CODE + "b";
        YELLOW = CODE + "e";
        RED = CODE + "c";
        GRAY = CODE + "7";
        GOLD = CODE + "6";
        GREEN = CODE + "a";
        WHITE = CODE + "f";
        BLACK = CODE + "0";
        BOLD = CODE + "l";
        ITALIC = CODE + "o";
        UNDER_LINE = CODE + "n";
        STRIKE_THROUGH = CODE + "m";
        RESET = CODE + "r";
        MAGIC = CODE + "k";
        DARK_BLUE = CODE + "1";
        DARK_AQUA = CODE + "3";
        DARK_GRAY = CODE + "8";
        DARK_GREEN = CODE + "2";
        DARK_PURPLE = CODE + "5";
        DARK_RED = CODE + "4";
        PINK = CODE + "d";
        MENU_BAR = CC.GRAY + CC.STRIKE_THROUGH + "------------------------";
        CHAT_BAR = CC.GRAY + CC.STRIKE_THROUGH + "------------------------------------------------";
        SB_BAR = CC.GRAY + CC.STRIKE_THROUGH + "----------------------";
    }

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(CODE + "x(?>" + CODE + "[0-9a-f]){6}", Pattern.CASE_INSENSITIVE);

    public static List<String> translate(List<String> lines) {
        List<String> toReturn = new ArrayList<>();

        for (String line : lines) {
            toReturn.add(translate(line));
        }

        return toReturn;
    }

    public static List<String> translate(String[] lines) {
        List<String> toReturn = new ArrayList<>();

        for (String line : lines) {
            if (line != null) {
                toReturn.add(translate(line));
            }
        }

        return toReturn;
    }

    public static String translate(String textToTranslate) {
        if (textToTranslate == null) {
            return null;
        }
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static String getLastColors(@NotNull String input) {
        Validate.notNull(input, "Cannot get last colors from null text");

        StringBuilder result = new StringBuilder();
        int length = input.length();

        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == CODE && index < length - 1) {
                if (index > 11 && input.charAt(index - 12) == CODE && (input.charAt(index - 11) == 'x' || input.charAt(index - 11) == 'X')) {
                    String color = input.substring(index - 12, index + 2);
                    if (HEX_COLOR_PATTERN.matcher(color).matches()) {
                        result.insert(0, color);
                        break;
                    }
                }
                char color = input.charAt(index + 1);

                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(color) > -1) {
                    result.insert(0, new String(new char[]{CODE, color}));

                    // Once we find a color or reset we can stop searching
                    if ("klmno".indexOf(color) == -1 || color == 'r') {
                        break;
                    }
                }
            }
        }

        return result.toString();
    }

    public static char getColorCharByIndex(int index) {
        return "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".charAt(index);
    }

}
