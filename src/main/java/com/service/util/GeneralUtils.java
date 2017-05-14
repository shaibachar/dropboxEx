package com.service.util;

public final class GeneralUtils {

    public static String formatHebrew(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder ret = new StringBuilder();

        if (num >= 400) {
            String string = "ת";
            ret.append(string.charAt(num / 400 - 1));
            num %= 400;
        } else {
            num %= 400;
        }
        if (num >= 100) {
            String string = "קרש";
            ret.append(string.charAt(num / 100 - 1));
            num %= 100;
        }
        switch (num) {
        case 16:
            ret.append("טז");
            break;
        case 15:
            ret.append("טו");
            break;
        default:
            if (num >= 10) {
                String string = "יכלמנסעפצ";
                ret.append(string.charAt(num / 10 - 1));
                num %= 10;
            }
            if (num > 0) {
                String string = "אבגדהוזחט";
                ret.append(string.charAt(num - 1));
            }
            break;
        }
        return ret.toString();
    }
}