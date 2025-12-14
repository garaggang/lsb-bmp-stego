package com.example.stego;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

/**
 * Реализация НЗБ/LSB: встраивание битов в младшие биты каналов R,G,B.
 */
public final class LsbStego {
    private LsbStego() {}

    /**
     * Оценка максимальной ёмкости (в байтах) для изображения w*h при использовании 3 бит на пиксель.
     * Без учёта служебных байтов заголовка payload.
     */
    public static long capacityBytes(int w, int h) {
        long capacityBits = (long) w * h * 3;
        return capacityBits / 8;
    }


    public static BufferedImage embed(BufferedImage src, byte[] payload) {
        int w = src.getWidth();
        int h = src.getHeight();

        long capacityBits = (long) w * h * 3;
        long needBits = (long) payload.length * 8;

        if (needBits > capacityBits) {
            throw new IllegalArgumentException(
                    "Сообщение слишком большое. Нужно бит: " + needBits + ", доступно бит: " + capacityBits);
        }

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        int[] bitIndex = {0}; // mutable for lambda

        // StreamAPI по требованию: обходим пиксели плоским индексом.
        IntStream.range(0, w * h).forEach(i -> {
            int x = i % w;
            int y = i / w;

            int rgb = img.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int gr = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            if (bitIndex[0] < needBits) r  = (r  & 0xFE) | getPayloadBit(payload, bitIndex[0]++);
            if (bitIndex[0] < needBits) gr = (gr & 0xFE) | getPayloadBit(payload, bitIndex[0]++);
            if (bitIndex[0] < needBits) b  = (b  & 0xFE) | getPayloadBit(payload, bitIndex[0]++);

            int newRgb = (r << 16) | (gr << 8) | b;
            img.setRGB(x, y, newRgb);
        });

        return img;
    }


    public static byte[] extract(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        // Сначала читаем 8 байт заголовка (len + crc)
        byte[] header = extractBytes(img, 8);
        int len = java.nio.ByteBuffer.wrap(header).getInt();
        if (len < 0) throw new IllegalArgumentException("Некорректная длина сообщения: " + len);

        // Затем читаем полностью: 8 + len
        return extractBytes(img, 8 + len);
    }

    /**
     * Визуализация младших битов: если LSB=1 => 255, иначе 0 (по каждому каналу).
     */
    public static BufferedImage buildLsbVisualization(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int rr = ((r & 1) == 1) ? 255 : 0;
                int gg = ((g & 1) == 1) ? 255 : 0;
                int bb = ((b & 1) == 1) ? 255 : 0;

                out.setRGB(x, y, (rr << 16) | (gg << 8) | bb);
            }
        }
        return out;
    }

    private static byte[] extractBytes(BufferedImage img, int byteCount) {
        int w = img.getWidth();
        int h = img.getHeight();
        long capacityBits = (long) w * h * 3;
        long needBits = (long) byteCount * 8;

        if (needBits > capacityBits) {
            throw new IllegalArgumentException("Запрошено больше данных, чем возможно извлечь из изображения.");
        }

        byte[] out = new byte[byteCount];
        int bitIndex = 0;

        outer:
        for (int i = 0; i < w * h; i++) {
            int x = i % w;
            int y = i / w;
            int rgb = img.getRGB(x, y);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            bitIndex = setBit(out, bitIndex++, r & 1);
            if (bitIndex >= needBits) break;
            bitIndex = setBit(out, bitIndex++, g & 1);
            if (bitIndex >= needBits) break;
            bitIndex = setBit(out, bitIndex++, b & 1);
            if (bitIndex >= needBits) break;
        }

        return out;
    }

    private static int getPayloadBit(byte[] payload, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int inByte = 7 - (bitIndex % 8);
        return (payload[byteIndex] >> inByte) & 1;
    }

    private static int setBit(byte[] out, int bitIndex, int bitValue) {
        int byteIndex = bitIndex / 8;
        int inByte = 7 - (bitIndex % 8);
        if (bitValue == 1) {
            out[byteIndex] |= (1 << inByte);
        }
        return bitIndex + 1;
    }
}
