package com.example.stego;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * CLI-режим для демонстрации запуска из консоли.
 */
public final class Cli {

    public static void main(String[] args) throws Exception {
        Args a = Args.parse(args);

        if (a.command() == null) {
            printHelp();
            return;
        }

        switch (a.command()) {
            case "embed" -> embed(a);
            case "extract" -> extract(a);
            default -> printHelp();
        }
    }

    private static void embed(Args a) throws Exception {
        if (a.in() == null || a.out() == null) {
            printHelp();
            return;
        }
        BufferedImage src = ImageIO.read(new File(a.in()));
        if (src == null) throw new IllegalArgumentException("Не удалось прочитать BMP: " + a.in());

        byte[] payload = Payload.pack(a.text() == null ? "" : a.text(), StandardCharsets.UTF_8);
        BufferedImage stego = LsbStego.embed(src, payload);

        boolean ok = ImageIO.write(stego, "bmp", new File(a.out()));
        if (!ok) throw new IllegalStateException("Не удалось записать BMP: " + a.out());

        System.out.println("OK. Saved: " + a.out());
    }

    private static void extract(Args a) throws Exception {
        if (a.in() == null) {
            printHelp();
            return;
        }
        BufferedImage img = ImageIO.read(new File(a.in()));
        if (img == null) throw new IllegalArgumentException("Не удалось прочитать BMP: " + a.in());

        byte[] payload = LsbStego.extract(img);
        String msg = Payload.unpack(payload, StandardCharsets.UTF_8);
        System.out.println(msg);
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  embed   --in <input.bmp> --out <output.bmp> --text \"message\"");
        System.out.println("  extract --in <stego.bmp>");
    }
}
