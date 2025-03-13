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
    public static final java.awt.Color SNCF_TURQUOISE = new java.awt.Color(64, 224, 208);
    public static final java.awt.Color SNCF_LIME = new java.awt.Color(191, 255, 0);
    public static final java.awt.Color SNCF_GOLD = new java.awt.Color(255, 215, 0);
    public static final java.awt.Color SNCF_CYAN = new java.awt.Color(0, 200, 255);
    public static final java.awt.Color SNCF_VIOLET = new java.awt.Color(138, 43, 226);
    public static final java.awt.Color SNCF_MAGENTA = new java.awt.Color(255, 0, 144);
    public static final java.awt.Color SNCF_FOREST_GREEN = new java.awt.Color(34, 139, 34);
    public static final java.awt.Color SNCF_CRIMSON = new java.awt.Color(220, 20, 60);
    public static final java.awt.Color SNCF_TEAL = new java.awt.Color(0, 128, 128);
    public static final java.awt.Color SNCF_SALMON = new java.awt.Color(250, 128, 114);
    public static final java.awt.Color SNCF_DEEP_ORANGE = new java.awt.Color(255, 87, 34);
    public static final java.awt.Color SNCF_INDIGO = new java.awt.Color(75, 0, 130);
    public static final java.awt.Color SNCF_OLIVE = new java.awt.Color(128, 128, 0);
    public static final java.awt.Color SNCF_CORAL = new java.awt.Color(255, 127, 80);
    public static final java.awt.Color SNCF_SKY_BLUE = new java.awt.Color(135, 206, 250);
    public static final java.awt.Color SNCF_CHOCOLATE = new java.awt.Color(210, 105, 30);
    public static final java.awt.Color SNCF_ROYAL_BLUE = new java.awt.Color(65, 105, 225);
    public static final java.awt.Color SNCF_DARK_RED = new java.awt.Color(139, 0, 0);
    public static final java.awt.Color SNCF_SEA_GREEN = new java.awt.Color(46, 139, 87);
    public static final java.awt.Color SNCF_PLUM = new java.awt.Color(221, 160, 221);



    public static java.awt.Color getColor(int i) {
        java.awt.Color[] colors = {
            SNCF_PURPLE,  SNCF_YELLOW, SNCF_DARKBLUE, SNCF_LIGHTBLUE,
            SNCF_ORANGE, SNCF_LIGHTGREEN, SNCF_PINK, SNCF_BLACK, 
            SNCF_RED, SNCF_LIME, SNCF_GOLD, SNCF_CYAN, SNCF_VIOLET, SNCF_MAGENTA, 
            SNCF_FOREST_GREEN, SNCF_CRIMSON, SNCF_TEAL, SNCF_SALMON, SNCF_TURQUOISE,
            SNCF_DEEP_ORANGE, SNCF_INDIGO, SNCF_OLIVE, SNCF_CORAL, SNCF_SKY_BLUE,
            SNCF_CHOCOLATE, SNCF_ROYAL_BLUE, SNCF_DARK_RED, SNCF_SEA_GREEN, SNCF_PLUM,

        };
        if (i >= 0 && i < colors.length) {
            return colors[i];
        } else {
            return colors[i%colors.length];
        }
    }
}
