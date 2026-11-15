import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Small helper that gives every screen the same modern look (colours, fonts, buttons, tables). */
public final class Ui {
    private Ui() {}

    public static final Color SIDEBAR = new Color(0x1E1B4B);
    public static final Color SIDEBAR_HOVER = new Color(0x312E81);
    public static final Color ACCENT = new Color(0x4F46E5);
    public static final Color ACCENT_HOVER = new Color(0x4338CA);
    public static final Color BG = new Color(0xF3F4F6);
    public static final Color TEXT = new Color(0x111827);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xD1D5DB);
    public static final Color ROW_ALT = new Color(0xF9FAFB);
    public static final Color GREEN = new Color(0x15803D);

    public static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD = BASE.deriveFont(Font.BOLD);
    public static final Font SMALL = BASE.deriveFont(12f);
    public static final Font SMALL_BOLD = BASE.deriveFont(Font.BOLD, 12f);
    public static final Font TITLE = BASE.deriveFont(Font.BOLD, 24f);

    // ---------- buttons ----------

    /** Rounded button with hover colour; painted by hand so it looks the same on every Look & Feel. */
    public static class FancyButton extends JButton {
        private final Color base;
        private final Color hover;
        private boolean over;

        FancyButton(String text, Color base, Color hover) {
            super(text);
            this.base = base;
            this.hover = hover;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(BOLD);
            setBorder(new EmptyBorder(8, 18, 8, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? BORDER : over ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton primary(String text) {
        return new FancyButton(text, ACCENT, ACCENT_HOVER);
    }

    public static JButton success(String text) {
        return new FancyButton(text, new Color(0x16A34A), new Color(0x15803D));
    }

    /** Sidebar navigation button. */
    public static class NavButton extends JButton {
        private boolean active;
        private boolean over;
        private static final Color IDLE = new Color(0xC7D2FE);

        public NavButton(String text) {
            super(text);
            setHorizontalAlignment(LEFT);
            setFont(BOLD);
            setForeground(IDLE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(11, 22, 11, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        public void setActive(boolean a) {
            active = a;
            setForeground(a ? Color.WHITE : IDLE);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active || over) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? ACCENT : SIDEBAR_HOVER);
                g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // ---------- inputs ----------

    public static JTextField field(String text, int columns) {
        JTextField f = new JTextField(text, columns);
        f.setFont(BASE);
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setCaretColor(ACCENT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 9, 6, 9)));
        return f;
    }

    public static JTextField field(int columns) {
        return field("", columns);
    }

    public static <T> JComboBox<T> style(JComboBox<T> c, int width) {
        c.setFont(BASE);
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setPreferredSize(new Dimension(width, 36));
        return c;
    }

    public static <T> JComboBox<T> combo(int width) {
        return style(new JComboBox<T>(), width);
    }

    /** Small grey caption above an input. */
    public static JPanel labeled(String caption, JComponent input) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(caption);
        l.setFont(SMALL_BOLD);
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(0, 2, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(input);
        return p;
    }

    public static JPanel row(JComponent... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        for (JComponent c : parts) p.add(c);
        return p;
    }

    // ---------- cards and tables ----------

    /** White rounded-looking panel with a title and a body. */
    public static JPanel card(String title, JComponent body) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0xE5E7EB), 1, true), new EmptyBorder(16, 18, 16, 18)));
        JLabel t = new JLabel(title);
        t.setFont(BASE.deriveFont(Font.BOLD, 16f));
        t.setForeground(TEXT);
        card.add(t, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    public static JScrollPane scroll(JTable table, int... widths) {
        styleTable(table);
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(new Color(0xE5E7EB), 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private static void styleTable(JTable t) {
        t.setFont(BASE);
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(0xE0E7FF));
        t.setSelectionForeground(TEXT);
        t.setFocusable(false);
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(LEFT);
                setForeground(TEXT);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, zebra);
        t.setDefaultRenderer(Number.class, zebra);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, v, false, false, r, c);
                l.setOpaque(true);
                l.setBackground(new Color(0xEEF2FF));
                l.setForeground(new Color(0x3730A3));
                l.setFont(BOLD);
                l.setHorizontalAlignment(LEFT);
                l.setBorder(new CompoundBorder(
                        javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC7D2FE)),
                        new EmptyBorder(0, 12, 0, 12)));
                return l;
            }
        });
    }
}
