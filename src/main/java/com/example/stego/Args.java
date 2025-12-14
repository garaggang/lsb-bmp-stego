package com.example.stego;

/**
 * Простой парсер аргументов командной строки.
 */
public record Args(String command, String in, String out, String text) {

    public static Args parse(String[] args) {
        if (args == null || args.length == 0) return new Args(null, null, null, null);

        String cmd = args[0];
        String in = null, out = null, text = null;

        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            if ("--in".equals(a) && i + 1 < args.length) in = args[++i];
            else if ("--out".equals(a) && i + 1 < args.length) out = args[++i];
            else if ("--text".equals(a) && i + 1 < args.length) text = args[++i];
        }

        return new Args(cmd, in, out, text);
    }
}
