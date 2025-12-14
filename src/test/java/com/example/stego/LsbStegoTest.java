package com.example.stego;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты НЗБ/LSB: вложение и извлечение.
 */
public class LsbStegoTest {

    @Test
    void embedThenExtractReturnsSameMessage() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        String message = "test message 123";
        byte[] payload = Payload.pack(message, StandardCharsets.UTF_8);

        BufferedImage stego = LsbStego.embed(img, payload);
        byte[] extractedPayload = LsbStego.extract(stego);
        String extracted = Payload.unpack(extractedPayload, StandardCharsets.UTF_8);

        assertEquals(message, extracted);
    }

    @Test
    void tooLargeMessageThrows() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB); // 300 bits ~ 37 bytes
        byte[] payload = new byte[200]; // too big

        assertThrows(IllegalArgumentException.class, () -> LsbStego.embed(img, payload));
    }

    @Test
    void corruptedCrcThrows() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        byte[] payload = Payload.pack("hello", StandardCharsets.UTF_8);

        // corrupt one byte
        payload[payload.length - 1] ^= 0x01;

        BufferedImage stego = LsbStego.embed(img, payload);
        byte[] extractedPayload = LsbStego.extract(stego);

        assertThrows(IllegalArgumentException.class, () -> Payload.unpack(extractedPayload, StandardCharsets.UTF_8));
    }
}
