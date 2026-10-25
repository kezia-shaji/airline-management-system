package com.airline.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared styling constants and UI utility helpers for the Airline Management System.
 * Defines a cohesive palette: Navy Blue, Royal Blue, Sky Blue, Subtle Teal, and Crisp White.
 */
public class UITheme {

    // Primary & Accent Brand Colors
    public static final Color NAVY_DARK       = new Color(15, 30, 65);     // #0F1E41 - Deep Navy Header/Sidebar
    public static final Color NAVY_PRIMARY    = new Color(24, 48, 89);     // #183059 - Primary Navy
    public static final Color ROYAL_BLUE      = new Color(29, 78, 216);    // #1D4ED8 - Action Blue / Buttons
    public static final Color ROYAL_HOVER     = new Color(37, 99, 235);    // #2563EB - Hover state
    public static final Color SKY_BLUE        = new Color(56, 189, 248);   // #38BDF8 - Sky Accent
    public static final Color SKY_LIGHT       = new Color(239, 246, 255);  // #EFF6FF - Soft Sky Tint
    public static final Color TEAL_ACCENT     = new Color(13, 148, 136);   // #0D9488 - Subtle Teal / Badges
    public static final Color TEAL_LIGHT      = new Color(240, 253, 250);  // #F0FDFA - Soft Teal Tint

    // Background & Neutral Colors
    public static final Color BG_MAIN         = new Color(245, 247, 251);  // #F5F7FB - Window canvas
    public static final Color BG_CARD         = Color.WHITE;               // Card / Surface
    public static final Color BORDER_LIGHT    = new Color(226, 232, 240);  // #E2E8F0 - Subtle Card Borders
    public static final Color BORDER_FOCUS    = new Color(59, 130, 246);   // #3B82F6 - Input Focus Border

    // Typography Colors
    public static final Color TEXT_PRIMARY    = new Color(15, 23, 42);     // #0F172A - Deep slate for readability
    public static final Color TEXT_SECONDARY  = new Color(71, 85, 105);    // #475569 - Subtitles & labels
    public static final Color TEXT_MUTED      = new Color(148, 163, 184);  // #94A3B8 - Hints & placeholders
    public static final Color TEXT_ON_NAVY    = Color.WHITE;

    // Status Colors
    public static final Color STATUS_SUCCESS_BG = new Color(236, 253, 245);
    public static final Color STATUS_SUCCESS_FG = new Color(5, 150, 105);
    public static final Color STATUS_WARN_BG    = new Color(254, 243, 199);
    public static final Color STATUS_WARN_FG    = new Color(217, 119, 6);
    public static final Color STATUS_DANGER_BG  = new Color(254, 242, 242);
    public static final Color STATUS_DANGER_FG  = new Color(225, 29, 72);
    public static final Color STATUS_INFO_BG    = new Color(238, 242, 255);
    public static final Color STATUS_INFO_FG    = new Color(79, 70, 229);

    // Modern Fonts (Sans-serif stack with fallback)
    public static final Font FONT_BRAND_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER      = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO        = new Font("Consolas", Font.PLAIN, 13);

    /**
     * Creates a rounded card panel with soft border and crisp white background.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER_LIGHT);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        return card;
    }

    /**
     * Creates a rounded card panel with custom padding.
     */
    public static JPanel createCardPanel(int padding) {
        JPanel card = createCardPanel();
        card.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return card;
    }

    /**
     * Primary action button with royal blue fill, rounded corners, and hover animation.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(NAVY_PRIMARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(ROYAL_HOVER);
                } else {
                    g2.setColor(ROYAL_BLUE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleButton(btn, Color.WHITE);
        return btn;
    }

    /**
     * Secondary / Outline button with subtle border and text.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(224, 231, 255));
                } else if (getModel().isRollover()) {
                    g2.setColor(SKY_LIGHT);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BORDER_LIGHT);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleButton(btn, NAVY_PRIMARY);
        return btn;
    }

    /**
     * Danger button (e.g. for cancellation or deletion).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(190, 18, 60));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(244, 63, 94));
                } else {
                    g2.setColor(STATUS_DANGER_FG);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleButton(btn, Color.WHITE);
        return btn;
    }

    private static void styleButton(JButton btn, Color textColor) {
        btn.setFont(FONT_BOLD);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
    }

    /**
     * Styles a standard JTextField with clean margins, padding, and subtle borders.
     */
    public static void styleTextField(JTextField tf) {
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    /**
     * Styles a JTable with modern row height, alternating row colors, header styling, and padding.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(224, 236, 254));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        // Default alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    lbl.setForeground(TEXT_PRIMARY);
                }
                return lbl;
            }
        });
    }

    /**
     * Creates a status badge label (e.g. SCHEDULED, CONFIRMED, CANCELLED).
     */
    public static JLabel createStatusBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBadgeBg(getText());
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SMALL);
        badge.setForeground(getBadgeFg(text));
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        return badge;
    }

    public static Color getBadgeBg(String text) {
        if (text == null) return STATUS_INFO_BG;
        String t = text.toUpperCase();
        if (t.contains("CONFIRMED") || t.contains("SCHEDULED") || t.contains("ACTIVE") || t.contains("AVAILABLE")) {
            return STATUS_SUCCESS_BG;
        } else if (t.contains("CANCELLED") || t.contains("BOOKED") || t.contains("DELAYED")) {
            return STATUS_DANGER_BG;
        } else if (t.contains("BUSINESS")) {
            return new Color(243, 232, 255);
        } else if (t.contains("ECONOMY")) {
            return SKY_LIGHT;
        }
        return STATUS_INFO_BG;
    }

    public static Color getBadgeFg(String text) {
        if (text == null) return STATUS_INFO_FG;
        String t = text.toUpperCase();
        if (t.contains("CONFIRMED") || t.contains("SCHEDULED") || t.contains("ACTIVE") || t.contains("AVAILABLE")) {
            return STATUS_SUCCESS_FG;
        } else if (t.contains("CANCELLED") || t.contains("BOOKED") || t.contains("DELAYED")) {
            return STATUS_DANGER_FG;
        } else if (t.contains("BUSINESS")) {
            return new Color(126, 34, 206);
        } else if (t.contains("ECONOMY")) {
            return ROYAL_BLUE;
        }
        return STATUS_INFO_FG;
    }

    /**
     * Creates a KPI summary stat card (e.g. "Total Flights", "48", "Active Flights in System").
     */
    public static JPanel createStatCard(String title, String value, String subtext, Color accent) {
        JPanel card = createCardPanel(16);
        card.setLayout(new BorderLayout(6, 6));

        // Top Row: Title + Color Accent Pill
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_SECONDARY);

        JPanel pill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pill.setPreferredSize(new Dimension(10, 10));
        pill.setOpaque(false);

        top.add(titleLbl, BorderLayout.WEST);
        top.add(pill, BorderLayout.EAST);

        // Middle: Large Value
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(NAVY_DARK);

        // Bottom: Subtitle
        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(TEXT_MUTED);

        card.add(top, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Creates a labeled form field container with label on top and input component below.
     */
    public static JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(FONT_BOLD);
        label.setForeground(TEXT_SECONDARY);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
}

