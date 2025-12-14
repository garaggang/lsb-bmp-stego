package com.example.stego;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

/**
 * Формат данных, которые встраиваются в BMP:
 * [4 байта длина текста][4 байта CRC32 от текста][N байт текста в кодировке]
 */
public final class Payload {
    private Payload() {}

    /**
     * Упаковывает строку в массив байт с длиной и CRC32.
     * @param message текст
     * @param charset кодировка
     * @return payload
     */
    public static byte[] pack(String message, Charset charset) {
        if (message == null) message = "";
        byte[] msgBytes = message.getBytes(charset);

        CRC32 crc = new CRC32();
        crc.update(msgBytes);
        int crc32 = (int) crc.getValue();

        ByteBuffer bb = ByteBuffer.allocate(4 + 4 + msgBytes.length);
        bb.putInt(msgBytes.length);
        bb.putInt(crc32);
        bb.put(msgBytes);
        return bb.array();
    }

    /**
     * Распаковывает payload: проверяет CRC32 и возвращает строку.
     * @param payload payload
     * @param charset кодировка
     * @return извлечённый текст
     */
    public static String unpack(byte[] payload, Charset charset) {
        if (payload == null || payload.length < 8) {
            throw new IllegalArgumentException("Недостаточно данных для распаковки.");
        }
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int len = bb.getInt();
        int crcExpected = bb.getInt();

        if (len < 0 || len > payload.length - 8) {
            throw new IllegalArgumentException("Некорректная длина сообщения: " + len);
        }

        byte[] msgBytes = new byte[len];
        bb.get(msgBytes);

        CRC32 crc = new CRC32();
        crc.update(msgBytes);
        int crcActual = (int) crc.getValue();

        if (crcActual != crcExpected) {
            throw new IllegalArgumentException("CRC не совпал: данные повреждены или не содержат сообщение.");
        }

        return new String(msgBytes, charset);
    }
}
