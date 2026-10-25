package com.airline.ui;

import com.airline.database.DBConnection;
import com.airline.model.Flight;
import com.airline.model.User;
import com.airline.service.BookingService;
import com.airline.service.FlightService;
import com.airline.service.NotificationService;
import com.airline.service.TravelCompanionService;
import com.airline.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final User user;
    private final int passengerId;
    private final FlightService flights = new FlightService();
    private final BookingService bookings = new BookingService();
    private final NotificationService notifications = new NotificationService();
    private final TravelCompanionService companion = new TravelCompanionService();

    // Navigation and layout
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentCardLayout);
    private final List<JButton> navButtons = new ArrayList<>();

    // Keep track of active panels for refresh
    private Runnable refreshPassengerOverview;
    private Runnable refreshBookingsTable;
    private Runnable refreshNotificationsTable;
    private Runnable refreshAdminOverview;
    private Runnable refreshAdminFlights;
    private Runnable refreshAdminPassengers;

    public DashboardFrame(User user, int passengerId) {
        this.user = user;
        this.passengerId = passengerId;

        setTitle("Airline Management System - " + (user.isAdmin() ? "Admin Console" : "Passenger Portal") + " (" + user.getUsername() + ")");
        setSize(1200, 780);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // Top Navigation Header Bar
        root.add(createHeaderBar(), BorderLayout.NORTH);

        // Main Body: Left Sidebar + Center Content
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        JPanel sidebar = createSidebar();
        body.add(sidebar, BorderLayout.WEST);

        // Build Cards based on role
        if (user.isAdmin()) {
            buildAdminContent();
        } else {
            buildPassengerContent();
        }

        body.add(contentCards, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        add(root);
    }

    // ==========================================
    // TOP APPLICATION HEADER
    // ==========================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.NAVY_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle bottom accent border
                g2.setColor(new Color(56, 189, 248, 80));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(getWidth(), 64));
        header.setBorder(new EmptyBorder(10, 24, 10, 24));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("✈");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoIcon.setForeground(UITheme.SKY_BLUE);

        JLabel brandText = new JLabel("AIRLINE MANAGEMENT SYSTEM");
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandText.setForeground(Color.WHITE);

        brandPanel.add(logoIcon);
        brandPanel.add(brandText);

        // Right User Information & Actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 5));
        rightPanel.setOpaque(false);

        // User Avatar Circle
        String initial = user.getUsername().isEmpty() ? "U" : user.getUsername().substring(0, 1).toUpperCase();
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ROYAL_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);

        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(UITheme.FONT_BOLD);
        nameLabel.setForeground(Color.WHITE);

        JLabel rolePill = UITheme.createStatusBadge(user.getRole());

        JButton logoutBtn = UITheme.createSecondaryButton("Sign Out");
        logoutBtn.setPreferredSize(new Dimension(95, 32));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to sign out?",
                    "Sign Out Confirmation",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        rightPanel.add(avatar);
        rightPanel.add(nameLabel);
        rightPanel.add(rolePill);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(logoutBtn);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // MODERN SIDEBAR NAVIGATION
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(241, 245, 249));
        sidebar.setPreferredSize(new Dimension(230, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_LIGHT));

        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(16, 12, 16, 12));

        JLabel menuTitle = new JLabel("NAVIGATION");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuTitle.setForeground(UITheme.TEXT_MUTED);
        menuTitle.setBorder(new EmptyBorder(0, 8, 8, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navList.add(menuTitle);

        if (user.isAdmin()) {
            addNavButton(navList, "Overview", "📊", "admin_overview");
            addNavButton(navList, "Flight Management", "✈", "admin_flights");
            addNavButton(navList, "Passengers", "👥", "admin_passengers");
        } else {
            addNavButton(navList, "Overview", "🏠", "pass_overview");
            addNavButton(navList, "Search & Book", "🔍", "pass_search");
            addNavButton(navList, "My Bookings", "📋", "pass_bookings");
            addNavButton(navList, "Notifications", "🔔", "pass_notifications");
            addNavButton(navList, "Travel Companion", "🤖", "pass_companion");
            addNavButton(navList, "E-Ticket", "🎫", "pass_ticket");
        }

        sidebar.add(navList);
        sidebar.add(Box.createVerticalGlue());

        // Sidebar Footer Info Card
        JPanel infoCard = new JPanel(new BorderLayout(4, 4));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(12, 12, 16, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true),
                        new EmptyBorder(10, 10, 10, 10)
                )
        ));
        infoCard.setMaximumSize(new Dimension(215, 80));

        JLabel sysTitle = new JLabel("Airline Management System");
        sysTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sysTitle.setForeground(UITheme.NAVY_DARK);

        JLabel sysStatus = new JLabel("● Database Active");
        sysStatus.setFont(UITheme.FONT_SMALL);
        sysStatus.setForeground(UITheme.STATUS_SUCCESS_FG);

        infoCard.add(sysTitle, BorderLayout.NORTH);
        infoCard.add(sysStatus, BorderLayout.SOUTH);

        sidebar.add(infoCard);
        return sidebar;
    }

    private void addNavButton(JPanel container, String text, String icon, String cardKey) {
        JButton btn = new JButton(icon + "  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isSelected = Boolean.TRUE.equals(getClientProperty("selected"));
                if (isSelected) {
                    g2.setColor(UITheme.ROYAL_BLUE);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> selectNav(btn, cardKey));

        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));

        // Select first nav item by default
        if (navButtons.size() == 1) {
            btn.putClientProperty("selected", true);
            btn.setForeground(Color.WHITE);
        }
    }

    private void selectNav(JButton selectedBtn, String cardKey) {
        for (JButton b : navButtons) {
            boolean active = (b == selectedBtn);
            b.putClientProperty("selected", active);
            b.setForeground(active ? Color.WHITE : UITheme.TEXT_SECONDARY);
            b.repaint();
        }
        contentCardLayout.show(contentCards, cardKey);

        // Trigger contextual refreshes
        if ("pass_overview".equals(cardKey) && refreshPassengerOverview != null) refreshPassengerOverview.run();
        if ("pass_bookings".equals(cardKey) && refreshBookingsTable != null) refreshBookingsTable.run();
        if ("pass_notifications".equals(cardKey) && refreshNotificationsTable != null) refreshNotificationsTable.run();
        if ("admin_overview".equals(cardKey) && refreshAdminOverview != null) refreshAdminOverview.run();
        if ("admin_flights".equals(cardKey) && refreshAdminFlights != null) refreshAdminFlights.run();
        if ("admin_passengers".equals(cardKey) && refreshAdminPassengers != null) refreshAdminPassengers.run();
    }

    private void navigateToCard(String cardKey) {
        for (JButton b : navButtons) {
            String txt = b.getText();
            if (("pass_search".equals(cardKey) && txt.contains("Search")) ||
                ("pass_ticket".equals(cardKey) && txt.contains("Ticket")) ||
                ("pass_companion".equals(cardKey) && txt.contains("Companion")) ||
                ("pass_bookings".equals(cardKey) && txt.contains("Bookings"))) {
                selectNav(b, cardKey);
                return;
            }
        }
        contentCardLayout.show(contentCards, cardKey);
    }

    // ==========================================
    // PASSENGER CONTENT PANELS
    // ==========================================
    private void buildPassengerContent() {
        contentCards.add(buildPassengerOverviewPanel(), "pass_overview");
        contentCards.add(buildFlightSearchPanel(), "pass_search");
        contentCards.add(buildMyBookingsPanel(), "pass_bookings");
        contentCards.add(buildNotificationPanel(), "pass_notifications");
        contentCards.add(buildTravelCompanionPanel(), "pass_companion");
        contentCards.add(buildTicketPanel(), "pass_ticket");
    }

    // 1. Overview Panel
    private JPanel buildPassengerOverviewPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Welcome Header Banner
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.NAVY_PRIMARY,
                        getWidth(), getHeight(), UITheme.ROYAL_BLUE
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        banner.setLayout(new BorderLayout(16, 10));
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel welcomeTitle = new JLabel("Welcome back, " + user.getUsername() + "! ✈");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setForeground(Color.WHITE);

        JLabel welcomeSub = new JLabel("Manage your reservations, view upcoming flights, and explore real-time travel recommendations.");
        welcomeSub.setFont(UITheme.FONT_REGULAR);
        welcomeSub.setForeground(new Color(224, 242, 254));

        banner.add(welcomeTitle, BorderLayout.NORTH);
        banner.add(welcomeSub, BorderLayout.CENTER);

        // Stats Card Container
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        statsGrid.setOpaque(false);

        JPanel activeBookingsCard = UITheme.createStatCard("Confirmed Bookings", "0", "Active flight seats", UITheme.ROYAL_BLUE);
        JPanel unreadNotifCard = UITheme.createStatCard("Notifications", "0", "Unread alerts", UITheme.TEAL_ACCENT);
        JPanel nextDestCard = UITheme.createStatCard("Latest Destination", "None", "Recent confirmed city", UITheme.SKY_BLUE);
        JPanel totalFlightsCard = UITheme.createStatCard("Scheduled Routes", "0", "Fleet domestic flights", UITheme.NAVY_PRIMARY);

        statsGrid.add(activeBookingsCard);
        statsGrid.add(unreadNotifCard);
        statsGrid.add(nextDestCard);
        statsGrid.add(totalFlightsCard);

        // Quick Actions & Information Section
        JPanel bottomSection = UITheme.createCardPanel(20);
        bottomSection.setLayout(new BorderLayout(12, 12));

        JLabel quickActionsTitle = new JLabel("Quick Flight Actions");
        quickActionsTitle.setFont(UITheme.FONT_HEADER);
        quickActionsTitle.setForeground(UITheme.NAVY_DARK);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        buttonBar.setOpaque(false);

        JButton bookNowBtn = UITheme.createPrimaryButton("🔍 Search & Book Flight");
        JButton viewTicketsBtn = UITheme.createSecondaryButton("🎫 View E-Tickets");
        JButton aiGuideBtn = UITheme.createSecondaryButton("🤖 AI Travel Guide");
        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Data");

        bookNowBtn.addActionListener(e -> navigateToCard("pass_search"));
        viewTicketsBtn.addActionListener(e -> navigateToCard("pass_ticket"));
        aiGuideBtn.addActionListener(e -> navigateToCard("pass_companion"));

        buttonBar.add(bookNowBtn);
        buttonBar.add(viewTicketsBtn);
        buttonBar.add(aiGuideBtn);
        buttonBar.add(refreshBtn);

        // Demonstration helper guide
        JPanel helperCard = new JPanel(new BorderLayout(8, 6));
        helperCard.setOpaque(false);
        helperCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_LIGHT),
                new EmptyBorder(12, 0, 0, 0)
        ));

        JLabel demoHint = new JLabel("<html><body style='color:#64748B; font-size:11px;'>" +
                "<b>Flight Operations Tip:</b> Flights departing from Kochi (COK) to Bangalore, Delhi, Mumbai, and Chennai are currently scheduled. " +
                "Use the <i>Search & Book</i> tab to select seats in Business or Economy class." +
                "</body></html>");
        helperCard.add(demoHint, BorderLayout.CENTER);

        bottomSection.add(quickActionsTitle, BorderLayout.NORTH);
        bottomSection.add(buttonBar, BorderLayout.CENTER);
        bottomSection.add(helperCard, BorderLayout.SOUTH);

        // Asynchronous Refresh Logic for Overview off EDT
        refreshPassengerOverview = () -> {
            new Thread(() -> {
                try {
                    List<String[]> bList = bookings.myBookings(passengerId);
                    long confirmed = bList.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b[8])).count();

                    List<String[]> nList = notifications.get(user.getUserId());
                    long unread = nList.stream().filter(n -> "Unread".equalsIgnoreCase(n[4])).count();

                    String dest = companion.getLatestDestination(passengerId);

                    List<Flight> fList = flights.all();
                    long scheduled = fList.stream().filter(f -> "SCHEDULED".equalsIgnoreCase(f.getStatus())).count();

                    SwingUtilities.invokeLater(() -> {
                        updateStatCardValue(activeBookingsCard, String.valueOf(confirmed));
                        updateStatCardValue(unreadNotifCard, String.valueOf(unread));
                        updateStatCardValue(nextDestCard, dest != null ? dest : "None");
                        updateStatCardValue(totalFlightsCard, String.valueOf(scheduled));
                    });
                } catch (Exception ignored) {}
            }).start();
        };

        refreshBtn.addActionListener(e -> refreshPassengerOverview.run());
        refreshPassengerOverview.run();

        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(statsGrid, BorderLayout.NORTH);
        centerPanel.add(bottomSection, BorderLayout.CENTER);

        root.add(banner, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        return root;
    }

    private void updateStatCardValue(JPanel card, String newValue) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (lbl.getFont().getSize() == 26) {
                    lbl.setText(newValue);
                    return;
                }
            }
        }
    }

    // 2. Flight Search & Booking Panel
    private JPanel buildFlightSearchPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Search Criteria Card
        JPanel searchCard = UITheme.createCardPanel(16);
        searchCard.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Search Domestic Flights");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);
        searchCard.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        fields.setOpaque(false);

        JTextField src = new JTextField("Kochi", 11);
        JTextField dest = new JTextField("Bangalore", 11);
        JTextField date = new JTextField("2026-10-15", 10);
        UITheme.styleTextField(src);
        UITheme.styleTextField(dest);
        UITheme.styleTextField(date);

        JButton find = UITheme.createPrimaryButton("🔍 Search Flights");
        JButton showAll = UITheme.createSecondaryButton("Show All Scheduled");

        fields.add(UITheme.createFormField("Origin City", src));
        fields.add(UITheme.createFormField("Destination City", dest));
        fields.add(UITheme.createFormField("Departure Date (YYYY-MM-DD)", date));
        fields.add(Box.createHorizontalStrut(8));
        fields.add(find);
        fields.add(showAll);

        searchCard.add(fields, BorderLayout.CENTER);

        // Results Card with modern JTable
        JPanel resultsCard = UITheme.createCardPanel(16);
        resultsCard.setLayout(new BorderLayout(10, 10));

        DefaultTableModel flightModel = new DefaultTableModel(
                new String[]{"ID", "Flight No", "Airline", "Origin", "Destination", "Date", "Departure", "Arrival", "Available Seats", "Price (₹)", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable flightTable = new JTable(flightModel);
        UITheme.styleTable(flightTable);

        // Custom Cell Renderers for Badges & Currency
        flightTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    int seats = Integer.parseInt(v.toString());
                    if (seats > 5) {
                        l.setForeground(UITheme.STATUS_SUCCESS_FG);
                        l.setText("● " + seats + " seats left");
                    } else if (seats > 0) {
                        l.setForeground(UITheme.STATUS_WARN_FG);
                        l.setText("⚠ " + seats + " seats left");
                    } else {
                        l.setForeground(UITheme.STATUS_DANGER_FG);
                        l.setText("Sold Out");
                    }
                } catch (Exception ignored) {}
                return l;
            }
        });

        flightTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                l.setText("₹ " + v);
                return l;
            }
        });

        flightTable.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                String status = v != null ? v.toString() : "";
                JLabel badge = UITheme.createStatusBadge(status);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                p.setOpaque(false);
                p.add(badge);
                return p;
            }
        });

        // Hide ID column
        flightTable.getColumnModel().getColumn(0).setMinWidth(0);
        flightTable.getColumnModel().getColumn(0).setMaxWidth(0);
        flightTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scroll = new JScrollPane(flightTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        // Action Footer
        JPanel actionFooter = new JPanel(new BorderLayout(10, 10));
        actionFooter.setOpaque(false);
        actionFooter.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel hint = new JLabel("Select a flight from the list above, then click 'Select Flight & Choose Seats'.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_SECONDARY);

        JButton book = UITheme.createPrimaryButton("Select Flight & Choose Seats ➔");
        book.setPreferredSize(new Dimension(260, 40));

        actionFooter.add(hint, BorderLayout.WEST);
        actionFooter.add(book, BorderLayout.EAST);

        resultsCard.add(scroll, BorderLayout.CENTER);
        resultsCard.add(actionFooter, BorderLayout.SOUTH);

        // Event Listeners with async execution off EDT
        List<Flight> loadedFlights = new ArrayList<>();

        Runnable loadFlightsAction = () -> {
            new Thread(() -> {
                try {
                    String s = src.getText().trim();
                    String d = dest.getText().trim();
                    LocalDate dt = LocalDate.parse(date.getText().trim());
                    List<Flight> results = flights.search(s, d, dt);
                    SwingUtilities.invokeLater(() -> {
                        flightModel.setRowCount(0);
                        loadedFlights.clear();
                        for (Flight f : results) {
                            loadedFlights.add(f);
                            flightModel.addRow(new Object[]{
                                    f.getId(), f.getNumber(), f.getAirline(), f.getSource(), f.getDestination(),
                                    f.getDepartureDate(), f.getDepartureTime(), f.getArrivalTime(),
                                    f.getAvailableSeats(), f.getPrice(), f.getStatus()
                            });
                        }
                        if (results.isEmpty()) {
                            JOptionPane.showMessageDialog(this,
                                    "No matching flights found for the selected route and date.",
                                    "Flight Search",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        };

        find.addActionListener(e -> loadFlightsAction.run());

        showAll.addActionListener(e -> {
            new Thread(() -> {
                try {
                    List<Flight> results = flights.all();
                    SwingUtilities.invokeLater(() -> {
                        flightModel.setRowCount(0);
                        loadedFlights.clear();
                        for (Flight f : results) {
                            loadedFlights.add(f);
                            flightModel.addRow(new Object[]{
                                    f.getId(), f.getNumber(), f.getAirline(), f.getSource(), f.getDestination(),
                                    f.getDepartureDate(), f.getDepartureTime(), f.getArrivalTime(),
                                    f.getAvailableSeats(), f.getPrice(), f.getStatus()
                            });
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        });

        book.addActionListener(e -> {
            int selectedRow = flightTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a flight from the table first.",
                        "Flight Selection Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int flightId = (Integer) flightTable.getValueAt(selectedRow, 0);
            Flight selectedFlight = loadedFlights.stream().filter(f -> f.getId() == flightId).findFirst().orElse(null);
            if (selectedFlight != null) {
                selectSeat(selectedFlight);
                // Refresh table
                find.doClick();
            }
        });

        // Initial load
        showAll.doClick();

        root.add(searchCard, BorderLayout.NORTH);
        root.add(resultsCard, BorderLayout.CENTER);
        return root;
    }

    // Interactive Aircraft Seat Picker
    private void selectSeat(Flight f) {
        new Thread(() -> {
            try {
                List<String[]> seats = bookings.availableSeats(f.getId());
                SwingUtilities.invokeLater(() -> showSeatDialog(f, seats));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> error(ex));
            }
        }).start();
    }

    private void showSeatDialog(Flight f, List<String[]> seats) {
        if (seats.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No seats available for Flight " + f.getNumber() + ".",
                    "Flight Full",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Dialog container
        JPanel dialogPanel = new JPanel(new BorderLayout(14, 14));
        dialogPanel.setPreferredSize(new Dimension(560, 480));

        // Header Banner
        JPanel header = new JPanel(new BorderLayout(6, 4));
        header.setBackground(UITheme.NAVY_PRIMARY);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Select Available Seat — " + f.getNumber() + " (" + f.getAirline() + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(f.getSource() + " ➔ " + f.getDestination() + "  |  " + f.getDepartureDate() + " " + f.getDepartureTime());
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.SKY_LIGHT);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        legend.setOpaque(false);
        legend.add(createLegendItem("Business Class", new Color(126, 34, 206)));
        legend.add(createLegendItem("Economy Class", UITheme.ROYAL_BLUE));

        // Cabin Seating Grid
        JPanel cabin = new JPanel();
        cabin.setLayout(new BoxLayout(cabin, BoxLayout.Y_AXIS));
        cabin.setBackground(Color.WHITE);
        cabin.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel cockpitLabel = new JLabel("▲ COCKPIT / FORWARD ▲", SwingConstants.CENTER);
        cockpitLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cockpitLabel.setForeground(UITheme.TEXT_MUTED);
        cockpitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cabin.add(cockpitLabel);
        cabin.add(Box.createVerticalStrut(10));

        // Seats arranged in rows
        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> buttons = new ArrayList<>();
        JPanel grid = new JPanel(new GridLayout(0, 5, 8, 8));
        grid.setOpaque(false);

        for (String[] s : seats) {
            String seatId = s[0];
            String seatNo = s[1];
            String seatClass = s[2];

            boolean isBiz = "BUSINESS".equalsIgnoreCase(seatClass);
            JRadioButton b = new JRadioButton(seatNo + (isBiz ? " (Biz)" : ""));
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setForeground(isBiz ? new Color(126, 34, 206) : UITheme.ROYAL_BLUE);
            b.setBackground(isBiz ? new Color(243, 232, 255) : UITheme.SKY_LIGHT);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isBiz ? new Color(216, 180, 254) : new Color(186, 230, 253), 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            b.putClientProperty("seatId", Integer.parseInt(seatId));
            group.add(b);
            grid.add(b);
            buttons.add(b);
        }

        cabin.add(grid);

        dialogPanel.add(header, BorderLayout.NORTH);
        dialogPanel.add(new JScrollPane(cabin), BorderLayout.CENTER);
        dialogPanel.add(legend, BorderLayout.SOUTH);

        int ok = JOptionPane.showConfirmDialog(
                this,
                dialogPanel,
                "Aircraft Cabin Seat Selection",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (ok == JOptionPane.OK_OPTION) {
            JRadioButton selected = buttons.stream().filter(AbstractButton::isSelected).findFirst().orElse(null);
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select an available seat.", "Seat Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (Integer) selected.getClientProperty("seatId");

            new Thread(() -> {
                try {
                    int bookingId = bookings.book(passengerId, f.getId(), id);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Booking Confirmed Successfully!\n\nBooking Reference: #" + bookingId +
                                        "\nFlight: " + f.getNumber() + " (" + f.getSource() + " → " + f.getDestination() + ")" +
                                        "\nSeat Number: " + selected.getText() +
                                        "\n\nYou can access your boarding pass in the E-Ticket tab.",
                                "Flight Reservation Confirmed",
                                JOptionPane.INFORMATION_MESSAGE);

                        if (refreshPassengerOverview != null) refreshPassengerOverview.run();
                        if (refreshBookingsTable != null) refreshBookingsTable.run();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        }
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(12, 12));
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        p.add(box);
        p.add(l);
        return p;
    }

    // 3. My Bookings Panel
    private JPanel buildMyBookingsPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UITheme.createCardPanel(16);
        card.setLayout(new BorderLayout(12, 12));

        // Top Action Bar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("My Reservations & Flight History");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton refresh = UITheme.createSecondaryButton("🔄 Refresh");
        JButton viewTicketBtn = UITheme.createPrimaryButton("🎫 Show Selected E-Ticket");
        JButton cancel = UITheme.createDangerButton("✕ Cancel Selected Booking");

        actions.add(refresh);
        actions.add(viewTicketBtn);
        actions.add(cancel);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        // Table
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Booking ID", "Flight", "Origin", "Destination", "Date", "Departure", "Seat", "Fare (₹)", "Status", "Ticket No"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(m);
        UITheme.styleTable(table);

        // Status pill renderer
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                String st = v != null ? v.toString() : "";
                JLabel badge = UITheme.createStatusBadge(st);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                p.setOpaque(false);
                p.add(badge);
                return p;
            }
        });

        // Price formatting
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                l.setText("₹ " + v);
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        // Bottom manual ID input fallback
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setOpaque(false);
        JTextField manualId = new JTextField(8);
        UITheme.styleTextField(manualId);
        bottom.add(new JLabel("Specific Booking ID:"));
        bottom.add(manualId);

        JButton manualCancel = UITheme.createDangerButton("Cancel by ID");
        bottom.add(manualCancel);

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        // Load & Refresh logic off EDT
        refreshBookingsTable = () -> {
            new Thread(() -> {
                try {
                    List<String[]> data = bookings.myBookings(passengerId);
                    SwingUtilities.invokeLater(() -> {
                        m.setRowCount(0);
                        for (String[] x : data) {
                            m.addRow(x);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        };

        refresh.addActionListener(e -> refreshBookingsTable.run());
        refreshBookingsTable.run();

        // Cancel action
        cancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = Integer.parseInt(table.getValueAt(row, 0).toString());
            String status = table.getValueAt(row, 8).toString();
            if ("CANCELLED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This booking is already cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to cancel Booking #" + bookingId + "?\nThe seat will be released back to the inventory.",
                    "Cancel Booking Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                new Thread(() -> {
                    try {
                        bookings.cancel(passengerId, bookingId);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " cancelled and seat released.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            refreshBookingsTable.run();
                            if (refreshPassengerOverview != null) refreshPassengerOverview.run();
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> error(ex));
                    }
                }).start();
            }
        });

        manualCancel.addActionListener(e -> {
            String val = manualId.getText().trim();
            if (val.isEmpty()) return;
            try {
                int bId = Integer.parseInt(val);
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Cancel Booking #" + bId + "?",
                        "Confirm Cancellation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        try {
                            bookings.cancel(passengerId, bId);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(this, "Booking cancelled.");
                                refreshBookingsTable.run();
                                if (refreshPassengerOverview != null) refreshPassengerOverview.run();
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> error(ex));
                        }
                    }).start();
                }
            } catch (Exception ex) {
                error(ex);
            }
        });

        // View ticket from table selection
        viewTicketBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String bId = table.getValueAt(row, 0).toString();
            loadTicketInPanel(bId);
            navigateToCard("pass_ticket");
        });

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    // 4. Notification Center Panel
    private JPanel buildNotificationPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UITheme.createCardPanel(16);
        card.setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Notification Center");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton refresh = UITheme.createSecondaryButton("🔄 Refresh");
        JButton markAll = UITheme.createPrimaryButton("✓ Mark All as Read");

        actions.add(refresh);
        actions.add(markAll);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Type", "Title", "Message", "Status", "Date / Time"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(m);
        UITheme.styleTable(table);

        // Hide ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Type badge renderer
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                String type = v != null ? v.toString() : "GENERAL";
                JLabel badge = UITheme.createStatusBadge(type);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                p.setOpaque(false);
                p.add(badge);
                return p;
            }
        });

        // Status renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String st = v != null ? v.toString() : "";
                if ("Unread".equalsIgnoreCase(st)) {
                    l.setForeground(UITheme.ROYAL_BLUE);
                    l.setFont(UITheme.FONT_BOLD);
                    l.setText("● Unread");
                } else {
                    l.setForeground(UITheme.TEXT_MUTED);
                    l.setText("Read");
                }
                return l;
            }
        });

        table.getColumnModel().getColumn(3).setPreferredWidth(320);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        refreshNotificationsTable = () -> {
            new Thread(() -> {
                try {
                    List<String[]> data = notifications.get(user.getUserId());
                    SwingUtilities.invokeLater(() -> {
                        m.setRowCount(0);
                        for (String[] x : data) {
                            m.addRow(new Object[]{x[0], x[3], x[1], x[2], x[4], x[5]});
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        };

        refresh.addActionListener(e -> refreshNotificationsTable.run());
        markAll.addActionListener(e -> {
            new Thread(() -> {
                try {
                    notifications.markAllRead(user.getUserId());
                    SwingUtilities.invokeLater(() -> {
                        refreshNotificationsTable.run();
                        if (refreshPassengerOverview != null) refreshPassengerOverview.run();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        });

        refreshNotificationsTable.run();
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    // 5. Smart Travel Companion Panel (Groq AI)
    private JPanel buildTravelCompanionPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UITheme.createCardPanel(20);
        card.setLayout(new BorderLayout(14, 14));

        // Header
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);

        JLabel title = new JLabel("🤖 Smart Travel Companion — AI Guide");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);

        JLabel sub = new JLabel("Personalized destination insights, attractions, culinary highlights, and packing tips powered by Groq AI.");
        sub.setFont(UITheme.FONT_REGULAR);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        top.add(title, BorderLayout.NORTH);
        top.add(sub, BorderLayout.SOUTH);

        // Control Strip
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        controls.setOpaque(false);

        JTextField destinationInput = new JTextField(14);
        UITheme.styleTextField(destinationInput);

        JButton generate = UITheme.createPrimaryButton("✨ Generate AI Travel Guide");
        JButton copyBtn = UITheme.createSecondaryButton("📋 Copy Guide");
        JLabel statusLabel = new JLabel("Enter a destination or generate for your next flight.");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);

        controls.add(UITheme.createFormField("Target City / Destination", destinationInput));
        controls.add(generate);
        controls.add(copyBtn);
        controls.add(statusLabel);

        // Pre-fill destination from latest confirmed booking off EDT
        new Thread(() -> {
            try {
                String dest = companion.getLatestDestination(passengerId);
                if (dest != null && !dest.isBlank()) {
                    SwingUtilities.invokeLater(() -> {
                        destinationInput.setText(dest);
                        statusLabel.setText("Auto-detected latest confirmed destination: " + dest);
                    });
                }
            } catch (Exception ignored) {}
        }).start();

        // Content Area
        JTextArea responseArea = new JTextArea();
        responseArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        responseArea.setForeground(UITheme.TEXT_PRIMARY);
        responseArea.setBackground(new Color(250, 252, 255));
        responseArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        responseArea.setText("Click 'Generate AI Travel Guide' above to fetch tailored insights for your selected destination.\n\n" +
                "The travel companion provides:\n" +
                "• Top local attractions and monuments\n" +
                "• Must-try regional delicacies and dining spots\n" +
                "• Airport transportation and local transit guidance\n" +
                "• Weather considerations and packing checklist\n" +
                "• Safety tips and emergency contacts\n\n" +
                "Note: Requires GROQ_API_KEY environment variable configured on your system.");

        JScrollPane scroll = new JScrollPane(responseArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));

        // Loading bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        JPanel bottomControls = new JPanel(new BorderLayout(8, 8));
        bottomControls.setOpaque(false);
        bottomControls.add(controls, BorderLayout.NORTH);
        bottomControls.add(progressBar, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(bottomControls, BorderLayout.CENTER);
        card.add(scroll, BorderLayout.SOUTH);

        // Use BorderLayout for responsive expansion
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);
        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(bottomControls, BorderLayout.CENTER);

        JPanel fullCard = UITheme.createCardPanel(20);
        fullCard.setLayout(new BorderLayout(12, 12));
        fullCard.add(wrapper, BorderLayout.NORTH);
        fullCard.add(scroll, BorderLayout.CENTER);

        // Actions
        generate.addActionListener(e -> {
            String target = destinationInput.getText().trim();
            if (target.isEmpty()) {
                responseArea.setText("No destination specified.\nPlease enter a city name (e.g. Bangalore, Delhi, Mumbai) to generate a guide.");
                return;
            }

            final String finalDest = target;
            statusLabel.setText("Contacting AI Assistant for " + finalDest + "...");
            progressBar.setVisible(true);
            generate.setEnabled(false);
            responseArea.setText("Generating intelligent travel guide for " + finalDest + "...\nPlease wait a moment.");

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return companion.askGroq(finalDest);
                }

                @Override
                protected void done() {
                    progressBar.setVisible(false);
                    generate.setEnabled(true);
                    try {
                        String result = get();
                        responseArea.setText(result);
                        responseArea.setCaretPosition(0);
                        statusLabel.setText("Guide successfully generated for " + finalDest + ".");
                    } catch (Exception ex) {
                        String msg = ex.getMessage();
                        if (msg != null && msg.contains("GROQ_API_KEY is not set")) {
                            responseArea.setText("⚠️ GROQ_API_KEY environment variable is not configured.\n\n" +
                                    "To enable live AI generation:\n" +
                                    "1. Set GROQ_API_KEY in your system environment:\n" +
                                    "   export GROQ_API_KEY=\"your_groq_api_key_here\"\n" +
                                    "2. Restart the application.\n\n" +
                                    "Destination details for " + finalDest + " remain registered in the system.");
                            statusLabel.setText("GROQ_API_KEY not configured.");
                        } else {
                            responseArea.setText("Could not generate guide: " + msg);
                            statusLabel.setText("Error generating guide.");
                            error(ex);
                        }
                    }
                }
            };
            worker.execute();
        });

        copyBtn.addActionListener(e -> {
            String txt = responseArea.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txt), null);
            JOptionPane.showMessageDialog(this, "Travel guide copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        root.add(fullCard, BorderLayout.CENTER);
        return root;
    }

    // 6. E-Ticket Panel
    private final JTextField ticketBookingIdField = new JTextField(10);
    private final JTextArea ticketDisplayArea = new JTextArea();

    private JPanel buildTicketPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UITheme.createCardPanel(20);
        card.setLayout(new BorderLayout(14, 14));

        // Top Selection Bar
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        JLabel title = new JLabel("Electronic Boarding Pass & Ticket");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        UITheme.styleTextField(ticketBookingIdField);
        JButton show = UITheme.createPrimaryButton("Show Ticket");
        JButton copy = UITheme.createSecondaryButton("📋 Copy Ticket Text");

        controls.add(new JLabel("Booking ID:"));
        controls.add(ticketBookingIdField);
        controls.add(show);
        controls.add(copy);

        top.add(title, BorderLayout.WEST);
        top.add(controls, BorderLayout.EAST);

        // Center Monospace Display
        ticketDisplayArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        ticketDisplayArea.setEditable(false);
        ticketDisplayArea.setForeground(UITheme.TEXT_PRIMARY);
        ticketDisplayArea.setBackground(new Color(250, 252, 255));
        ticketDisplayArea.setBorder(new EmptyBorder(18, 18, 18, 18));
        ticketDisplayArea.setText("Enter a valid Booking ID above or select a booking from 'My Bookings' to view your electronic ticket.");

        JScrollPane scroll = new JScrollPane(ticketDisplayArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));

        show.addActionListener(e -> {
            String val = ticketBookingIdField.getText().trim();
            if (val.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Booking ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            loadTicketInPanel(val);
        });

        copy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ticketDisplayArea.getText()), null);
            JOptionPane.showMessageDialog(this, "E-Ticket text copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private void loadTicketInPanel(String bookingIdStr) {
        new Thread(() -> {
            try {
                int bId = Integer.parseInt(bookingIdStr);
                String ticketText = bookings.ticket(passengerId, bId);
                SwingUtilities.invokeLater(() -> {
                    ticketBookingIdField.setText(bookingIdStr);
                    ticketDisplayArea.setText(ticketText);
                    ticketDisplayArea.setCaretPosition(0);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> error(ex));
            }
        }).start();
    }

    // ==========================================
    // ADMIN CONTENT PANELS
    // ==========================================
    private void buildAdminContent() {
        contentCards.add(buildAdminOverviewPanel(), "admin_overview");
        contentCards.add(buildAdminFlightsPanel(), "admin_flights");
        contentCards.add(buildAdminPassengersPanel(), "admin_passengers");
    }

    // 1. Admin Overview & Stats
    private JPanel buildAdminOverviewPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Admin Header Banner
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.NAVY_DARK,
                        getWidth(), getHeight(), UITheme.NAVY_PRIMARY
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        banner.setLayout(new BorderLayout(16, 8));
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Administrator Control Center 🛡️");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Fleet operations overview, domestic flight scheduling, passenger records, and capacity monitoring.");
        sub.setFont(UITheme.FONT_REGULAR);
        sub.setForeground(UITheme.SKY_LIGHT);

        banner.add(title, BorderLayout.NORTH);
        banner.add(sub, BorderLayout.SOUTH);

        // Stats Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        statsGrid.setOpaque(false);

        JPanel totalFlightsCard = UITheme.createStatCard("Total Flights", "0", "Total registered routes", UITheme.ROYAL_BLUE);
        JPanel scheduledCard = UITheme.createStatCard("Scheduled Flights", "0", "Active on runway", UITheme.TEAL_ACCENT);
        JPanel passengersCard = UITheme.createStatCard("Total Passengers", "0", "Registered accounts", UITheme.SKY_BLUE);
        JPanel systemStatusCard = UITheme.createStatCard("System State", "ONLINE", "All services active", UITheme.STATUS_SUCCESS_FG);

        statsGrid.add(totalFlightsCard);
        statsGrid.add(scheduledCard);
        statsGrid.add(passengersCard);
        statsGrid.add(systemStatusCard);

        // Quick Management Card
        JPanel card = UITheme.createCardPanel(20);
        card.setLayout(new BorderLayout(14, 14));

        JLabel quickTitle = new JLabel("Administrative Actions");
        quickTitle.setFont(UITheme.FONT_HEADER);
        quickTitle.setForeground(UITheme.NAVY_DARK);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        buttonBar.setOpaque(false);

        JButton manageFlightsBtn = UITheme.createPrimaryButton("✈ Manage Flights & Add Route");
        JButton managePaxBtn = UITheme.createSecondaryButton("👥 View Passenger Directory");
        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Fleet Stats");

        manageFlightsBtn.addActionListener(e -> selectNav(navButtons.get(1), "admin_flights"));
        managePaxBtn.addActionListener(e -> selectNav(navButtons.get(2), "admin_passengers"));

        buttonBar.add(manageFlightsBtn);
        buttonBar.add(managePaxBtn);
        buttonBar.add(refreshBtn);

        card.add(quickTitle, BorderLayout.NORTH);
        card.add(buttonBar, BorderLayout.CENTER);

        // Refresh logic off EDT
        refreshAdminOverview = () -> {
            new Thread(() -> {
                try {
                    List<Flight> fs = flights.all();
                    long scheduled = fs.stream().filter(f -> "SCHEDULED".equalsIgnoreCase(f.getStatus())).count();
                    int paxCount = 0;
                    try (Connection c = DBConnection.getConnection();
                         PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM passengers");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            paxCount = rs.getInt(1);
                        }
                    }
                    final int finalPax = paxCount;
                    SwingUtilities.invokeLater(() -> {
                        updateStatCardValue(totalFlightsCard, String.valueOf(fs.size()));
                        updateStatCardValue(scheduledCard, String.valueOf(scheduled));
                        updateStatCardValue(passengersCard, String.valueOf(finalPax));
                    });
                } catch (Exception ignored) {}
            }).start();
        };

        refreshBtn.addActionListener(e -> refreshAdminOverview.run());
        refreshAdminOverview.run();

        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(statsGrid, BorderLayout.NORTH);
        centerPanel.add(card, BorderLayout.CENTER);

        root.add(banner, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        return root;
    }

    // 2. Admin Flight Management
    private JPanel buildAdminFlightsPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UITheme.createCardPanel(16);
        card.setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Flight Management & Fleet Operations");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refresh = UITheme.createSecondaryButton("🔄 Refresh");
        JButton add = UITheme.createPrimaryButton("➕ Add New Flight");

        actions.add(refresh);
        actions.add(add);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Flight No", "Airline", "Origin", "Destination", "Departure Date", "Time", "Seats Left", "Fare (₹)", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(m);
        UITheme.styleTable(table);

        // Price formatting
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                l.setText("₹ " + v);
                return l;
            }
        });

        // Status badge
        table.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                String st = v != null ? v.toString() : "";
                JLabel badge = UITheme.createStatusBadge(st);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                p.setOpaque(false);
                p.add(badge);
                return p;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        refreshAdminFlights = () -> {
            new Thread(() -> {
                try {
                    List<Flight> fs = flights.all();
                    SwingUtilities.invokeLater(() -> {
                        m.setRowCount(0);
                        for (Flight f : fs) {
                            m.addRow(new Object[]{
                                    f.getId(), f.getNumber(), f.getAirline(), f.getSource(), f.getDestination(),
                                    f.getDepartureDate(), f.getDepartureTime(), f.getAvailableSeats(), f.getPrice(), f.getStatus()
                            });
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        };

        refresh.addActionListener(e -> refreshAdminFlights.run());
        add.addActionListener(e -> addFlightDialog());

        refreshAdminFlights.run();
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private void addFlightDialog() {
        JTextField no = new JTextField();
        JTextField air = new JTextField("Air India");
        JTextField src = new JTextField("Kochi");
        JTextField dst = new JTextField("Bangalore");
        JTextField dd = new JTextField("2026-11-01");
        JTextField dt = new JTextField("09:00");
        JTextField ad = new JTextField("2026-11-01");
        JTextField at = new JTextField("10:00");
        JTextField seats = new JTextField("40");
        JTextField price = new JTextField("5000");

        JTextField[] fields = {no, air, src, dst, dd, dt, ad, at, seats, price};
        for (JTextField tf : fields) UITheme.styleTextField(tf);

        JPanel p = new JPanel(new GridLayout(0, 2, 12, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(500, 380));

        String[] labels = {
                "Flight Number *", "Airline Name *", "Origin City *", "Destination City *",
                "Departure Date (YYYY-MM-DD) *", "Departure Time (HH:MM) *",
                "Arrival Date (YYYY-MM-DD) *", "Arrival Time (HH:MM) *",
                "Total Seat Capacity *", "Ticket Price (₹) *"
        };

        for (int i = 0; i < labels.length; i++) {
            p.add(new JLabel(labels[i]));
            p.add(fields[i]);
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                p,
                "Schedule New Flight Route",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (ok == JOptionPane.OK_OPTION) {
            new Thread(() -> {
                try {
                    flights.add(
                            no.getText().trim(),
                            air.getText().trim(),
                            src.getText().trim(),
                            dst.getText().trim(),
                            LocalDate.parse(dd.getText().trim()),
                            LocalTime.parse(dt.getText().trim()),
                            LocalDate.parse(ad.getText().trim()),
                            LocalTime.parse(at.getText().trim()),
                            Integer.parseInt(seats.getText().trim()),
                            new BigDecimal(price.getText().trim())
                    );
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Flight route added successfully and seat inventory generated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        if (refreshAdminFlights != null) refreshAdminFlights.run();
                        if (refreshAdminOverview != null) refreshAdminOverview.run();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        }
    }

    // 3. Admin Passenger Directory
    private JPanel buildAdminPassengersPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel card = UITheme.createCardPanel(16);
        card.setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Registered Passenger Directory");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.NAVY_DARK);

        JButton refresh = UITheme.createSecondaryButton("🔄 Refresh Directory");
        top.add(title, BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Passenger ID", "Full Name", "Email Address", "Phone Number", "Passport No"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(m);
        UITheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        refreshAdminPassengers = () -> {
            new Thread(() -> {
                try {
                    List<Object[]> rows = new ArrayList<>();
                    String sql = "SELECT passenger_id, CONCAT(first_name, ' ', last_name), email, phone, passport_no FROM passengers ORDER BY passenger_id";
                    try (Connection c = DBConnection.getConnection();
                         PreparedStatement ps = c.prepareStatement(sql);
                         ResultSet r = ps.executeQuery()) {
                        while (r.next()) {
                            rows.add(new Object[]{r.getInt(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)});
                        }
                    }
                    SwingUtilities.invokeLater(() -> {
                        m.setRowCount(0);
                        for (Object[] row : rows) {
                            m.addRow(row);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                }
            }).start();
        };

        refresh.addActionListener(e -> refreshAdminPassengers.run());
        refreshAdminPassengers.run();

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
    }
}
