package com.example.stego;

import javax.swing.SwingUtilities;

/**
 * Точка входа GUI-приложения.
 */
public class App {
    /**
     * Запуск Swing GUI.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
