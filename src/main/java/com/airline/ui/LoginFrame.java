package com.airline.ui;

import com.airline.model.User;
import com.airline.service.LoginService;
import com.airline.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final LoginService service = new LoginService();

    public LoginFrame() {
        setTitle("Airline Management System - Portal Login");
        setSize(860, 520);
        setMinimumSize(new Dimension(800, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.setBackground(UITheme.BG_MAIN);

        // Left: Airline-Themed Welcome Panel
        mainContainer.add(createBrandingPanel());

        // Right: Modern Login Form
        mainContainer.add(createFormPanel());

        add(mainContainer);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Premium deep navy gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.NAVY_DARK,
                        getWidth(), getHeight(), UITheme.NAVY_PRIMARY
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle decorative wave / accent line
                g2.setColor(new Color(56, 189, 248, 40));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(-60, -60, 300, 300);
                g2.drawOval(getWidth() - 150, getHeight() - 150, 320, 320);
                g2.dispose();
            }
        };
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Top Brand Header
        JPanel brandTop = new JPanel(new BorderLayout(8, 8));
        brandTop.setOpaque(false);

        JLabel logoBadge = new JLabel("✈  AIRLINE");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoBadge.setForeground(Color.WHITE);

        JLabel tagline = new JLabel("MANAGEMENT SYSTEM");
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tagline.setForeground(UITheme.SKY_BLUE);

        brandTop.add(logoBadge, BorderLayout.NORTH);
        brandTop.add(tagline, BorderLayout.SOUTH);

        // Center: Value Propositions / Features
        JPanel featureList = new JPanel();
        featureList.setOpaque(false);
        featureList.setLayout(new BoxLayout(featureList, BoxLayout.Y_AXIS));

        JLabel welcomeTitle = new JLabel("<html><body style='width: 260px; color: #FFFFFF; font-size: 15px; font-weight: bold;'>" +
                "Welcome to the next generation of flight booking & operations.</body></html>");
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        featureList.add(welcomeTitle);
        featureList.add(Box.createVerticalStrut(25));
        featureList.add(createFeatureItem("Real-Time Flight Schedules", "Instant availability across all domestic routes"));
        featureList.add(Box.createVerticalStrut(15));
        featureList.add(createFeatureItem("Interactive Aircraft Seating", "Select your preferred seat class and cabin view"));
        featureList.add(Box.createVerticalStrut(15));
        featureList.add(createFeatureItem("Smart AI Travel Companion", "Destination insights powered by Groq intelligence"));

        // Bottom: Demonstration note
        JLabel footer = new JLabel("College Project Demonstration Edition • v1.0");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(new Color(255, 255, 255, 140));

        panel.add(brandTop, BorderLayout.NORTH);
        panel.add(featureList, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFeatureItem(String title, String subtitle) {
        JPanel item = new JPanel(new BorderLayout(8, 2));
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel("•  " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.WHITE);

        JLabel s = new JLabel("    " + subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s.setForeground(new Color(203, 213, 225));

        item.add(t, BorderLayout.NORTH);
        item.add(s, BorderLayout.SOUTH);
        return item;
    }

    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UITheme.BG_CARD);
        formContainer.setBorder(new EmptyBorder(30, 45, 30, 45));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 4, 0);

        // Header Title & Subtitle
        JLabel title = new JLabel("Sign In");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.NAVY_DARK);
        formContainer.add(title, g);

        g.gridy++;
        g.insets = new Insets(0, 0, 20, 0);
        JLabel subtitle = new JLabel("Enter your credentials to access your portal");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        formContainer.add(subtitle, g);

        // Username Field
        g.gridy++;
        g.insets = new Insets(0, 0, 6, 0);
        JLabel unLabel = new JLabel("Username");
        unLabel.setFont(UITheme.FONT_BOLD);
        unLabel.setForeground(UITheme.TEXT_PRIMARY);
        formContainer.add(unLabel, g);

        g.gridy++;
        g.insets = new Insets(0, 0, 14, 0);
        UITheme.styleTextField(username);
        username.setPreferredSize(new Dimension(username.getPreferredSize().width, 38));
        formContainer.add(username, g);

        // Password Field
        g.gridy++;
        g.insets = new Insets(0, 0, 6, 0);
        JLabel pwLabel = new JLabel("Password");
        pwLabel.setFont(UITheme.FONT_BOLD);
        pwLabel.setForeground(UITheme.TEXT_PRIMARY);
        formContainer.add(pwLabel, g);

        g.gridy++;
        g.insets = new Insets(0, 0, 20, 0);
        UITheme.styleTextField(password);
        password.setPreferredSize(new Dimension(password.getPreferredSize().width, 38));
        formContainer.add(password, g);

        // Sign In Button
        g.gridy++;
        g.insets = new Insets(0, 0, 10, 0);
        JButton login = UITheme.createPrimaryButton("Sign In to Portal");
        login.setPreferredSize(new Dimension(login.getPreferredSize().width, 40));
        formContainer.add(login, g);

        // Create Passenger Account Button
        g.gridy++;
        g.insets = new Insets(0, 0, 18, 0);
        JButton register = UITheme.createSecondaryButton("Create Passenger Account");
        register.setPreferredSize(new Dimension(register.getPreferredSize().width, 40));
        formContainer.add(register, g);

        // Demo credentials card
        g.gridy++;
        g.insets = new Insets(0, 0, 0, 0);
        JPanel demoBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6)) {
            @Override
            protected void paintComponent(Graphics g2d) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SKY_LIGHT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        demoBadge.setOpaque(false);
        JLabel hint = new JLabel("Demo Admin: admin / admin123   •   Passenger: kezia / kezia123 (Kezia Shaji)");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.ROYAL_BLUE);
        demoBadge.add(hint);
        formContainer.add(demoBadge, g);

        // Event listeners
        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> register());
        getRootPane().setDefaultButton(login);

        return formContainer;
    }

    private void doLogin() {
        String uText = username.getText().trim();
        String pText = new String(password.getPassword());

        if (uText.isEmpty() || pText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User u = service.login(uText, pText);
            if (u == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password. Please verify your credentials.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int pid = u.isAdmin() ? -1 : service.getPassengerId(u.getUserId());
            new DashboardFrame(u, pid).setVisible(true);
            dispose();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void register() {
        JTextField un = new JTextField();
        JPasswordField pw = new JPasswordField();
        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();

        UITheme.styleTextField(un);
        UITheme.styleTextField(pw);
        UITheme.styleTextField(first);
        UITheme.styleTextField(last);
        UITheme.styleTextField(email);
        UITheme.styleTextField(phone);

        JPanel p = new JPanel(new GridLayout(0, 2, 12, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(460, 260));

        p.add(new JLabel("Username *"));
        p.add(un);
        p.add(new JLabel("Password *"));
        p.add(pw);
        p.add(new JLabel("First Name *"));
        p.add(first);
        p.add(new JLabel("Last Name *"));
        p.add(last);
        p.add(new JLabel("Email Address *"));
        p.add(email);
        p.add(new JLabel("Phone Number *"));
        p.add(phone);

        int ok = JOptionPane.showConfirmDialog(
                this,
                p,
                "Register New Passenger Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (ok == JOptionPane.OK_OPTION) {
            String u = un.getText().trim();
            String pwd = new String(pw.getPassword());
            String f = first.getText().trim();
            String l = last.getText().trim();
            String em = email.getText().trim();
            String ph = phone.getText().trim();

            if (u.isEmpty() || pwd.isEmpty() || f.isEmpty() || l.isEmpty() || em.isEmpty() || ph.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "All fields marked with an asterisk (*) are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                service.register(u, pwd, f, l, em, ph);
                JOptionPane.showMessageDialog(this,
                        "Registration successful! You may now sign in using your credentials.",
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE);
                username.setText(u);
                password.setText("");
                password.requestFocus();
            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "System Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
