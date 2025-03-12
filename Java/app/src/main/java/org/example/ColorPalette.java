package org.example;

public class ColorPalette {
    public static final java.awt.Color SNCF_RED = new java.awt.Color(223, 36, 47);
    public static final java.awt.Color SNCF_PURPLE = new java.awt.Color(129, 43, 109);
    public static final java.awt.Color SNCF_COOL_GRAY_1 = new java.awt.Color(225, 225, 225);
    public static final java.awt.Color SNCF_COOL_GRAY_3 = new java.awt.Color(215, 215, 215);
    public static final java.awt.Color SNCF_COOL_GRAY_5 = new java.awt.Color(185, 185, 185);
    public static final java.awt.Color SNCF_COOL_GRAY_7 = new java.awt.Color(160, 160, 160);
    public static final java.awt.Color SNCF_COOL_GRAY_9 = new java.awt.Color(116, 118, 120);
    public static final java.awt.Color SNCF_COOL_GRAY_11 = new java.awt.Color(77, 79, 83);
    public static final java.awt.Color SNCF_BLACK= new java.awt.Color(20, 20, 25);
    public static final java.awt.Color SNCF_YELLOW= new java.awt.Color(255, 182, 18);
    public static final java.awt.Color SNCF_ORANGE= new java.awt.Color(255, 128, 0);
    public static final java.awt.Color SNCF_DARKBLUE= new java.awt.Color(8, 8, 142);
    public static final java.awt.Color SNCF_LIGHTBLUE= new java.awt.Color(17, 195, 195);
    public static final java.awt.Color SNCF_LIGHTGREEN= new java.awt.Color(15, 203, 78);
    public static final java.awt.Color SNCF_PINK= new java.awt.Color(229, 17, 222);
    public static final java.awt.Color SNCF_BROWN= new java.awt.Color(80, 12, 26);

    public static java.awt.Color getColor(int i) {
        java.awt.Color[] colors = {
            SNCF_RED, SNCF_PURPLE,  SNCF_YELLOW, SNCF_BROWN, SNCF_DARKBLUE, SNCF_LIGHTBLUE,
            SNCF_ORANGE, SNCF_LIGHTGREEN, SNCF_PINK, SNCF_COOL_GRAY_1, SNCF_BLACK, 
        };
        if (i >= 0 && i < colors.length) {
            return colors[i];
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }
}
