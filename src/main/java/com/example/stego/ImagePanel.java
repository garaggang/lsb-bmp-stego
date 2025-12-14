package com.example.stego;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class ImagePanel extends JPanel {
    private BufferedImage image;


    public ImagePanel(String title) {
        setPreferredSize(new Dimension(520, 520));
        setBorder(BorderFactory.createTitledBorder(title));
    }


    public void setImage(BufferedImage img) {
        this.image = img;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;

        int w = getWidth();
        int h = getHeight();

        int iw = image.getWidth();
        int ih = image.getHeight();

        double scale = Math.min((w - 10) / (double) iw, (h - 30) / (double) ih);
        int nw = (int) (iw * scale);
        int nh = (int) (ih * scale);

        int x = (w - nw) / 2;
        int y = (h - nh) / 2;

        g.drawImage(image, x, y, nw, nh, null);
    }
}
