package com.example.stego;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Главное окно: выбор BMP, ввод текста и вложение по НЗБ (LSB).
 */
public class MainFrame extends JFrame {
    private static final Logger log = LogManager.getLogger(MainFrame.class);

    private final JTextField inputPath = new JTextField();
    private final JTextField outputPath = new JTextField();
    private final JTextArea messageArea = new JTextArea(6, 40);

    private final JLabel capacityLabel = new JLabel("Ёмкость: —");
    private final JLabel statusLabel = new JLabel("Готово");

    private final ImagePanel originalPanel = new ImagePanel("Оригинал");
    private final ImagePanel lsbPanel = new ImagePanel("Результат (только младшие биты)");

    /**
     * Конструктор GUI.
     */
    public MainFrame() {
        super("НЗБ (LSB) — вложение текста в BMP");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1160, 720);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildTopControls(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildBottom(), BorderLayout.SOUTH);
    }

    private JComponent buildTopControls() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Input row
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        p.add(new JLabel("Оригинал (BMP):"), c);

        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        p.add(inputPath, c);

        c.gridx = 2; c.gridy = 0; c.weightx = 0;
        p.add(new JButton(new AbstractAction("Обзор...") {
            @Override public void actionPerformed(ActionEvent e) {
                chooseFile(inputPath, true);
                updateCapacityPreview();
            }
        }), c);

        // Output row
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        p.add(new JLabel("С вложением (BMP):"), c);

        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        p.add(outputPath, c);

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        p.add(new JButton(new AbstractAction("Куда сохранить...") {
            @Override public void actionPerformed(ActionEvent e) {
                chooseFile(outputPath, false);
            }
        }), c);

        // Info row
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        p.add(new JLabel("Инфо:"), c);

        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        JPanel info = new JPanel(new BorderLayout());
        info.add(capacityLabel, BorderLayout.WEST);
        info.add(statusLabel, BorderLayout.EAST);
        p.add(info, c);

        return p;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.add(originalPanel);
        center.add(lsbPanel);
        return center;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JPanel msgPanel = new JPanel(new BorderLayout(6, 6));
        msgPanel.add(new JLabel("Текст для вложения:"), BorderLayout.NORTH);
        msgPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton embedBtn = new JButton("Вложить");
        embedBtn.addActionListener(e -> doEmbed());

        JButton extractBtn = new JButton("Извлечь из BMP");
        extractBtn.addActionListener(e -> doExtract());

        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> {
            messageArea.setText("");
            statusLabel.setText("Очищено");
        });

        buttons.add(extractBtn);
        buttons.add(embedBtn);
        buttons.add(clearBtn);

        bottom.add(msgPanel, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        return bottom;
    }

    private void chooseFile(JTextField field, boolean openDialog) {
        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = openDialog ? ch.showOpenDialog(this) : ch.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            field.setText(ch.getSelectedFile().getAbsolutePath());
        }
    }

    private void updateCapacityPreview() {
        try {
            File in = new File(inputPath.getText().trim());
            if (!in.exists()) return;
            BufferedImage src = ImageIO.read(in);
            if (src == null) return;

            long capBytes = LsbStego.capacityBytes(src.getWidth(), src.getHeight());
            capacityLabel.setText("Ёмкость: ~" + capBytes + " байт (минус заголовок)");
        } catch (Exception ignored) {
            // не критично
        }
    }

    private void doEmbed() {
        try {
            File in = new File(inputPath.getText().trim());
            File out = new File(outputPath.getText().trim());

            if (!in.exists()) throw new IllegalArgumentException("Файл оригинала не найден.");
            if (out.isDirectory()) throw new IllegalArgumentException("Путь вывода указывает на папку.");
            if (outputPath.getText().trim().isEmpty()) throw new IllegalArgumentException("Укажите файл для сохранения результата.");

            BufferedImage src = ImageIO.read(in);
            if (src == null) throw new IllegalArgumentException("Не удалось прочитать изображение. Проверьте BMP.");

            originalPanel.setImage(src);

            String msg = messageArea.getText();
            if (msg == null) msg = "";

            byte[] payload = Payload.pack(msg, StandardCharsets.UTF_8);

            BufferedImage stego = LsbStego.embed(src, payload);

            boolean ok = ImageIO.write(stego, "bmp", out);
            if (!ok) throw new IllegalStateException("ImageIO не смог записать BMP (нет writer'а).");

            BufferedImage lsbView = LsbStego.buildLsbVisualization(stego);
            lsbPanel.setImage(lsbView);

            statusLabel.setText("Вложено: " + payload.length + " байт");
            log.info("Успешное вложение: in={}, out={}, bytes={}", in, out, payload.length);

            JOptionPane.showMessageDialog(this,
                    "Готово!\nСохранено: " + out.getAbsolutePath(),
                    "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            statusLabel.setText("Ошибка");
            log.error("Ошибка вложения", ex);
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExtract() {
        try {
            File in = new File(inputPath.getText().trim());
            if (!in.exists()) throw new IllegalArgumentException("Файл не найден.");

            BufferedImage img = ImageIO.read(in);
            if (img == null) throw new IllegalArgumentException("Не удалось прочитать изображение. Проверьте BMP.");

            byte[] payload = LsbStego.extract(img);
            String msg = Payload.unpack(payload, StandardCharsets.UTF_8);

            messageArea.setText(msg);
            statusLabel.setText("Извлечено: " + payload.length + " байт");
            log.info("Успешное извлечение: in={}, bytes={}", in, payload.length);

            JOptionPane.showMessageDialog(this,
                    "Сообщение извлечено в поле текста.",
                    "Готово", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            statusLabel.setText("Ошибка");
            log.error("Ошибка извлечения", ex);
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
