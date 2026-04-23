import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ============================================================================
// MAIN APPLICATION CLASS
// ============================================================================
public class Server {
    private static SearchWindow searchWindow;
    private static ResultsWindow resultsWindow;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            searchWindow = new SearchWindow();
            searchWindow.setVisible(true);
        });
    }

    public static void displayResults(String content, String keyword) {
        SwingUtilities.invokeLater(() -> {
            if (resultsWindow == null || !resultsWindow.isVisible()) {
                resultsWindow = new ResultsWindow();
                resultsWindow.setVisible(true);
            }
            resultsWindow.appendContent(content, keyword);
            resultsWindow.toFront();
        });
    }
}

// ============================================================================
// SEARCH WINDOW - User Input and Configuration
// ============================================================================
class SearchWindow extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 32);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private JTextField searchField;
    private JButton searchButton;
    private List<JCheckBox> websiteCheckboxes;
    private JToggleButton imageToggleButton;

    public SearchWindow() {
        initializeWindow();
        buildUI();
    }

    private void initializeWindow() {
        setTitle("BlogWeaver - Intelligent Content Aggregator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            setIconImage(createIconImage());
        } catch (Exception e) {
            // Icon not critical
        }
    }

    private Image createIconImage() {
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setColor(PRIMARY_COLOR);
        g2d.fillRoundRect(0, 0, 32, 32, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("B", 8, 24);
        g2d.dispose();
        return icon;
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(236, 240, 241));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(236, 240, 241));

        JLabel titleLabel = new JLabel("BlogWeaver");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Weaving Knowledge into Beautiful Narratives");
        subtitleLabel.setFont(new Font("Georgia", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(15, 15));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)));

        contentPanel.add(createSearchSection(), BorderLayout.NORTH);
        contentPanel.add(createWebsiteSection(), BorderLayout.CENTER);
        contentPanel.add(createOptionsSection(), BorderLayout.SOUTH);

        return contentPanel;
    }

    private JPanel createSearchSection() {
        JPanel searchSection = new JPanel();
        searchSection.setLayout(new BorderLayout(10, 10));
        searchSection.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("What would you like to explore today?");
        searchLabel.setFont(new Font("Georgia", Font.ITALIC, 16));
        searchLabel.setForeground(SECONDARY_COLOR);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);

        searchField = new JTextField("Artificial Intelligence");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setPreferredSize(new Dimension(0, 45));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        searchField.addActionListener(e -> performSearch());

        inputPanel.add(searchField, BorderLayout.CENTER);

        searchSection.add(searchLabel, BorderLayout.NORTH);
        searchSection.add(inputPanel, BorderLayout.CENTER);

        return searchSection;
    }

    private JPanel createWebsiteSection() {
        JPanel websiteSection = new JPanel();
        websiteSection.setLayout(new BorderLayout(10, 10));
        websiteSection.setBackground(Color.WHITE);

        JLabel websiteLabel = new JLabel("Select Your Sources:");
        websiteLabel.setFont(LABEL_FONT);
        websiteLabel.setForeground(SECONDARY_COLOR);

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridLayout(3, 2, 15, 10));
        checkboxPanel.setBackground(Color.WHITE);

        websiteCheckboxes = new ArrayList<>();
        String[] websites = {
                "📚 Wikipedia", "💻 HackerNews", "🔧 StackOverflow",
                "📦 GitHub", "📄 ArXiv", "💭 Reddit"
        };

        for (String website : websites) {
            JCheckBox checkbox = new JCheckBox(website, true);
            checkbox.setFont(NORMAL_FONT);
            checkbox.setBackground(Color.WHITE);
            checkbox.setFocusPainted(false);

            checkbox.addItemListener(e -> {
                if (checkbox.isSelected()) {
                    checkbox.setForeground(PRIMARY_COLOR);
                } else {
                    checkbox.setForeground(Color.BLACK);
                }
            });
            checkbox.setForeground(PRIMARY_COLOR);

            websiteCheckboxes.add(checkbox);
            checkboxPanel.add(checkbox);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);

        websiteSection.add(websiteLabel, BorderLayout.NORTH);
        websiteSection.add(scrollPane, BorderLayout.CENTER);

        return websiteSection;
    }

    private JPanel createOptionsSection() {
        JPanel optionsSection = new JPanel();
        optionsSection.setLayout(new BorderLayout(10, 10));
        optionsSection.setBackground(Color.WHITE);

        // Create image toggle button
        imageToggleButton = new JToggleButton("🖼️ Image Scraping: OFF");
        imageToggleButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        imageToggleButton.setBackground(new Color(231, 76, 60)); // Red for OFF
        imageToggleButton.setForeground(Color.BLACK);
        imageToggleButton.setFocusPainted(false);
        imageToggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageToggleButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        imageToggleButton.setOpaque(true);
        imageToggleButton.setContentAreaFilled(true);

        // Toggle button action
        imageToggleButton.addActionListener(e -> {
            if (imageToggleButton.isSelected()) {
                imageToggleButton.setText("🖼️ Image Scraping: ON");
                imageToggleButton.setBackground(new Color(46, 204, 113)); // Green for ON
                imageToggleButton.setForeground(Color.BLACK);
            } else {
                imageToggleButton.setText("🖼️ Image Scraping: OFF");
                imageToggleButton.setBackground(new Color(231, 76, 60)); // Red for OFF
                imageToggleButton.setForeground(Color.BLACK);
            }
        });

        searchButton = new JButton("Weave My Blog");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(200, 45));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchButton.addActionListener(e -> performSearch());

        // Hover effect
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchButton.setBackground(SECONDARY_COLOR);
                searchButton.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchButton.setBackground(PRIMARY_COLOR);
                searchButton.setForeground(Color.BLACK);
            }
        });

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        togglePanel.setBackground(Color.WHITE);
        togglePanel.add(imageToggleButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(searchButton);

        JPanel southPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        southPanel.setBackground(Color.WHITE);
        southPanel.add(togglePanel);
        southPanel.add(buttonPanel);

        optionsSection.add(southPanel, BorderLayout.CENTER);

        return optionsSection;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(236, 240, 241));

        JLabel footerLabel = new JLabel("Powered by multiple knowledge sources • Real-time aggregation");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(149, 165, 166));

        footerPanel.add(footerLabel);
        return footerPanel;
    }

    private void performSearch() {
        String topic = searchField.getText().trim();
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a topic to explore.",
                    "Topic Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> selectedSites = new ArrayList<>();
        String[] siteNames = { "wikipedia", "hackernews", "stackoverflow", "github", "arxiv", "reddit" };

        for (int i = 0; i < websiteCheckboxes.size(); i++) {
            if (websiteCheckboxes.get(i).isSelected()) {
                selectedSites.add(siteNames[i]);
            }
        }

        if (selectedSites.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one source.",
                    "Source Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if image scraping is enabled
        boolean imageScrapingEnabled = imageToggleButton.isSelected();
        if (imageScrapingEnabled) {
            JOptionPane.showMessageDialog(this,
                    "Image scraping feature is currently under development.\nThe blog will be generated without images.",
                    "Feature Not Available", JOptionPane.INFORMATION_MESSAGE);
        }

        searchButton.setEnabled(false);
        searchButton.setText("🔄 Weaving...");

        final String finalTopic = topic;

        new Thread(() -> {
            try {
                // Show initial loading message
                Server.displayResults("", finalTopic);

                // Create blog title
                String blogTitle = BlogFormatter.createBlogTitle(finalTopic);
                Server.displayResults(blogTitle, finalTopic);

                // Fetch and weave content from all sources
                Server.displayResults("\n", finalTopic);

                for (int i = 0; i < selectedSites.size(); i++) {
                    String site = selectedSites.get(i);
                    String content = fetchContent(site, finalTopic);

                    if (content != null && !content.trim().isEmpty()) {
                        // Format as blog section
                        String blogSection = BlogFormatter.formatSection(content, site, finalTopic);
                        Server.displayResults(blogSection, finalTopic);

                        // Add transition between sections
                        if (i < selectedSites.size() - 1) {
                            Server.displayResults(BlogFormatter.createTransition(), finalTopic);
                        }
                    }

                    Thread.sleep(300); // Visual flow
                }

                // Blog conclusion
                Server.displayResults(BlogFormatter.createConclusion(finalTopic), finalTopic);

            } catch (Exception e) {
                Server.displayResults("\n\n❌ An error occurred while weaving your blog. Please try again.\n",
                        finalTopic);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    searchButton.setEnabled(true);
                    searchButton.setText("Weave My Blog");
                });
            }
        }).start();
    }

    private String fetchContent(String site, String topic) {
        switch (site) {
            case "wikipedia":
                return APIFetcher.getWikipediaContent(topic);
            case "hackernews":
                return APIFetcher.getHackerNewsContent(topic);
            case "stackoverflow":
                return APIFetcher.getStackOverflowContent(topic);
            case "github":
                return APIFetcher.getGitHubContent(topic);
            case "arxiv":
                return APIFetcher.getArXivContent(topic);
            case "reddit":
                return APIFetcher.getRedditContent(topic);
            default:
                return null;
        }
    }
}

// ============================================================================
// RESULTS WINDOW - Blog Display
// ============================================================================
class ResultsWindow extends JFrame {
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 24);
    private static final Font HEADING_FONT = new Font("Georgia", Font.BOLD, 18);
    private static final Font BODY_FONT = new Font("Georgia", Font.PLAIN, 14);
    private static final Font CAPTION_FONT = new Font("Georgia", Font.ITALIC, 12);

    private JTextPane blogPane;
    private StyledDocument doc;
    private JScrollPane scrollPane;

    // Color scheme for depth
    private static final Color BLOG_BG = new Color(253, 254, 254);
    private static final Color TITLE_COLOR = new Color(44, 62, 80);
    private static final Color HEADING_COLOR = new Color(41, 128, 185);
    private static final Color BODY_COLOR = new Color(52, 73, 94);
    private static final Color KEYWORD_COLOR = new Color(192, 57, 43); // Red
    private static final Color TRANSITION_COLOR = new Color(142, 68, 173); // Purple for transitions
    private static final Color EMPHASIS_COLOR = new Color(211, 84, 0); // Brown-red for emphasis
    private static final Color QUOTE_COLOR = new Color(127, 140, 141);
    private static final Color SOURCE_COLOR = new Color(155, 89, 182);

    public ResultsWindow() {
        initializeWindow();
        buildUI();
    }

    private void initializeWindow() {
        setTitle("📖 BlogWeaver - Your Knowledge Narrative");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - 1000) / 2, (screenSize.height - 800) / 2);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BLOG_BG);

        // Header
        JPanel headerPanel = createHeader();

        // Blog text pane (full width)
        blogPane = new JTextPane();
        blogPane.setEditable(false);
        blogPane.setBackground(BLOG_BG);

        doc = blogPane.getStyledDocument();

        scrollPane = new JScrollPane(blogPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        scrollPane.setBackground(BLOG_BG);
        scrollPane.getViewport().setBackground(BLOG_BG);

        // Control panel
        JPanel controlPanel = createControlPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Initial welcome message
        appendBlogText("\n👋 Welcome to BlogWeaver!\n\n", HEADING_FONT, HEADING_COLOR, true);
        appendBlogText("Enter a topic and select your sources to weave a beautiful knowledge narrative.\n",
                BODY_FONT, BODY_COLOR, false);
        appendBlogText("Content will be intelligently aggregated in real-time with smart highlighting.\n\n",
                BODY_FONT, QUOTE_COLOR, true);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("📖 BlogWeaver - Knowledge Narrative");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Where information becomes inspiration");
        subtitleLabel.setFont(new Font("Georgia", Font.ITALIC, 12));
        subtitleLabel.setForeground(new Color(189, 195, 199));

        JPanel labelPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        labelPanel.setBackground(new Color(44, 62, 80));
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        headerPanel.add(labelPanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlPanel.setBackground(new Color(236, 240, 241));
        controlPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JButton exportButton = createControlButton("📥 Export Blog");
        exportButton.addActionListener(e -> exportBlog());

        JButton clearButton = createControlButton("🗑️ Clear");
        clearButton.addActionListener(e -> clearBlog());

        JButton printButton = createControlButton("🖨️ Print");
        printButton.addActionListener(e -> printBlog());

        controlPanel.add(printButton);
        controlPanel.add(exportButton);
        controlPanel.add(clearButton);

        return controlPanel;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(new Color(52, 73, 94));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(44, 62, 80), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(44, 62, 80));
                button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 73, 94));
                button.setForeground(Color.BLACK);
            }
        });

        return button;
    }

    public void appendContent(String content, String keyword) {
        if (content == null || content.isEmpty())
            return;

        SwingUtilities.invokeLater(() -> {
            try {
                // Parse the content and apply appropriate styling
                String[] lines = content.split("\n");

                for (String line : lines) {
                    if (line.startsWith("# ")) {
                        // Blog title
                        appendBlogText(line.substring(2) + "\n", TITLE_FONT, TITLE_COLOR, true);
                    } else if (line.startsWith("## ")) {
                        // Section heading
                        appendBlogText("\n" + line.substring(3) + "\n", HEADING_FONT, HEADING_COLOR, true);
                    } else if (line.startsWith("> ")) {
                        // Quote
                        appendBlogText(line.substring(2) + "\n",
                                new Font("Georgia", Font.ITALIC, 14), QUOTE_COLOR, false);
                    } else if (line.startsWith("---")) {
                        // Divider
                        appendBlogText("✦ ✦ ✦\n", BODY_FONT, TRANSITION_COLOR, true);
                    } else if (line.startsWith("📌")) {
                        // Source indicator
                        appendBlogText(line + "\n", new Font("Georgia", Font.BOLD, 12), SOURCE_COLOR, true);
                    } else {
                        // Regular text with keyword and emphasis highlighting
                        appendHighlightedText(line + "\n", keyword);
                    }
                }

                blogPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void appendBlogText(String text, Font font, Color color, boolean bold) {
        try {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, font.getFamily());
            StyleConstants.setFontSize(attrs, font.getSize());
            StyleConstants.setForeground(attrs, color);
            StyleConstants.setBold(attrs, bold);

            if (font.isItalic()) {
                StyleConstants.setItalic(attrs, true);
            }

            doc.insertString(doc.getLength(), text, attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendHighlightedText(String text, String keyword) {
        // Words to emphasize (transition words, emphasis words)
        Set<String> emphasisWords = new HashSet<>(Arrays.asList(
                "however", "but", "actually", "therefore", "moreover", "furthermore",
                "nevertheless", "consequently", "meanwhile", "nonetheless", "otherwise",
                "indeed", "specifically", "significantly", "interestingly", "importantly",
                "surprisingly", "essentially", "fundamentally", "particularly"));

        // Stop words not to highlight
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "is", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "from", "as", "into", "through", "during", "before",
                "after", "above", "below", "between", "out", "off", "over", "under", "again",
                "further", "then", "once", "here", "there", "when", "where", "why", "how",
                "all", "both", "each", "few", "more", "most", "other", "some", "such", "no",
                "nor", "not", "only", "own", "same", "so", "than", "too", "very", "will",
                "just", "should", "now", "am", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "having", "do", "does", "did", "doing", "would",
                "could", "shall", "may", "might", "must", "can"));

        String[] keywords = keyword.toLowerCase().split("\\s+");

        // Split text preserving delimiters
        Pattern pattern = Pattern.compile("([\\w']+|[^\\w'])");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            String cleanWord = word.replaceAll("[^a-zA-Z']", "").toLowerCase();

            if (cleanWord.isEmpty()) {
                // Whitespace or punctuation
                try {
                    doc.insertString(doc.getLength(), word, null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                continue;
            }

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, BODY_FONT.getFamily());
            StyleConstants.setFontSize(attrs, BODY_FONT.getSize());

            if (emphasisWords.contains(cleanWord)) {
                // Emphasis words in brown-red
                StyleConstants.setForeground(attrs, EMPHASIS_COLOR);
                StyleConstants.setBold(attrs, true);
                StyleConstants.setItalic(attrs, true);
            } else if (!stopWords.contains(cleanWord)) {
                // Check if it's a keyword
                boolean isKeyword = false;
                for (String kw : keywords) {
                    if (cleanWord.equals(kw) ||
                            (cleanWord.length() > 3 && kw.contains(cleanWord)) ||
                            (kw.length() > 3 && cleanWord.contains(kw))) {
                        isKeyword = true;
                        break;
                    }
                }

                if (isKeyword) {
                    StyleConstants.setForeground(attrs, KEYWORD_COLOR);
                    StyleConstants.setBold(attrs, true);
                    StyleConstants.setBackground(attrs, new Color(255, 235, 238));
                } else {
                    StyleConstants.setForeground(attrs, BODY_COLOR);
                }
            } else {
                StyleConstants.setForeground(attrs, BODY_COLOR);
            }

            try {
                doc.insertString(doc.getLength(), word, attrs);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportBlog() {
        try {
            String content = doc.getText(0, doc.getLength());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("blogweaver_article.html"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                // Create HTML export
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html>\n<html>\n<head>\n");
                html.append("<meta charset='UTF-8'>\n");
                html.append("<title>BlogWeaver Article</title>\n");
                html.append("<style>\n");
                html.append("body { font-family: Georgia, serif; max-width: 800px; margin: 0 auto; padding: 20px; ");
                html.append("background: #fdfefe; color: #2c3e50; line-height: 1.6; }\n");
                html.append("h1 { color: #2c3e50; border-bottom: 2px solid #2980b9; padding-bottom: 10px; }\n");
                html.append("h2 { color: #2980b9; margin-top: 30px; }\n");
                html.append(".keyword { color: #c0392b; font-weight: bold; background: #fce4ec; padding: 2px 4px; }\n");
                html.append(".emphasis { color: #d35400; font-weight: bold; font-style: italic; }\n");
                html.append("blockquote { border-left: 3px solid #7f8c8d; margin: 20px 0; padding: 10px 20px; ");
                html.append("color: #7f8c8d; font-style: italic; }\n");
                html.append("</style>\n</head>\n<body>\n");
                html.append(content.replace("\n", "<br>\n"));
                html.append("\n</body>\n</html>");

                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write(html.toString());
                writer.close();

                JOptionPane.showMessageDialog(this,
                        "Blog exported successfully as HTML!",
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearBlog() {
        try {
            doc.remove(0, doc.getLength());

            appendBlogText("\nBlog cleared. Ready for a new narrative!\n",
                    HEADING_FONT, HEADING_COLOR, true);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void printBlog() {
        try {
            blogPane.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Print error: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ============================================================================
// BLOG FORMATTER - Creates beautiful blog-style content
// ============================================================================
class BlogFormatter {

    public static String createBlogTitle(String topic) {
        String capitalizedTopic = capitalizeWords(topic);
        return "# " + capitalizedTopic + ": A Comprehensive Exploration\n\n" +
                "> A knowledge narrative woven from multiple authoritative sources\n";
    }

    public static String formatSection(String rawContent, String source, String topic) {
        StringBuilder section = new StringBuilder();

        // Section header based on source
        String sourceName = getSourceName(source);
        String sectionTitle = generateSectionTitle(source, topic);

        section.append("## ").append(sectionTitle).append("\n\n");
        section.append("📌 Source: ").append(sourceName).append("\n\n");

        // Clean and format content
        String[] paragraphs = rawContent.split("\\n\\n");
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty())
                continue;

            String cleanedParagraph = cleanAndEnhanceParagraph(paragraph.trim());
            section.append(cleanedParagraph).append("\n\n");
        }

        return section.toString();
    }

    public static String createTransition() {
        String[] transitions = {
                "\n---\n> *Building upon this foundation, let us explore further perspectives...*\n\n",
                "\n---\n> *The narrative deepens as we consider additional insights...*\n\n",
                "\n---\n> *Expanding our understanding, we turn to another valuable source...*\n\n",
                "\n---\n> *Interestingly, a different angle emerges from our next source...*\n\n"
        };

        return transitions[new Random().nextInt(transitions.length)];
    }

    public static String createConclusion(String topic) {
        return "\n---\n\n## Synthesis: Weaving It All Together\n\n" +
                "As we reflect upon the diverse perspectives gathered about " +
                capitalizeWords(topic) + ", a richer, more nuanced understanding emerges. " +
                "The convergence of insights from multiple authoritative sources paints a " +
                "comprehensive picture that no single source could provide alone.\n\n" +
                "> *Knowledge, like a tapestry, gains its beauty and strength from the " +
                "interweaving of many threads.*\n\n" +
                "*This blog was intelligently aggregated by BlogWeaver, " +
                "transforming information into narrative.*\n";
    }

    private static String getSourceName(String source) {
        switch (source) {
            case "wikipedia":
                return "Wikipedia - The Free Encyclopedia";
            case "hackernews":
                return "Hacker News - Tech Community";
            case "stackoverflow":
                return "Stack Overflow - Developer Knowledge";
            case "github":
                return "GitHub - Open Source Community";
            case "arxiv":
                return "ArXiv - Academic Research";
            case "reddit":
                return "Reddit - Community Discussions";
            default:
                return source;
        }
    }

    private static String generateSectionTitle(String source, String topic) {
        switch (source) {
            case "wikipedia":
                return "The Encyclopedia Perspective on " + capitalizeWords(topic);
            case "hackernews":
                return "Tech Community Insights: " + capitalizeWords(topic);
            case "stackoverflow":
                return "Practical Applications: " + capitalizeWords(topic);
            case "github":
                return "Innovation & Development: " + capitalizeWords(topic);
            case "arxiv":
                return "Academic Research: " + capitalizeWords(topic);
            case "reddit":
                return "Community Voices: " + capitalizeWords(topic);
            default:
                return "Exploring " + capitalizeWords(topic);
        }
    }

    private static String cleanAndEnhanceParagraph(String paragraph) {
        // Clean up
        paragraph = paragraph.replaceAll("\\s+", " ")
                .replaceAll("\"", "'")
                .trim();

        // Enhance with transition words where appropriate
        if (paragraph.length() > 100) {
            // Add variety to paragraph beginnings
            String[] starters = {
                    "Interestingly, ", "Notably, ", "Indeed, ", "Essentially, ",
                    "Furthermore, ", "Moreover, ", "Significantly, "
            };

            // Randomly add starter to some paragraphs
            if (Math.random() > 0.7 && !paragraph.startsWith("http")) {
                paragraph = starters[new Random().nextInt(starters.length)] +
                        paragraph.substring(0, 1).toLowerCase() +
                        paragraph.substring(1);
            }
        }

        return paragraph;
    }

    private static String capitalizeWords(String text) {
        String[] words = text.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                if (capitalized.length() > 0) {
                    capitalized.append(" ");
                }
                // Don't capitalize common words unless they're first
                if (capitalized.length() == 0 ||
                        !Arrays.asList("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of",
                                "with")
                                .contains(word.toLowerCase())) {
                    capitalized.append(word.substring(0, 1).toUpperCase());
                    if (word.length() > 1) {
                        capitalized.append(word.substring(1).toLowerCase());
                    }
                } else {
                    capitalized.append(word.toLowerCase());
                }
            }
        }

        return capitalized.toString();
    }
}

// ============================================================================
// API FETCHER - Enhanced content fetching
// ============================================================================
class APIFetcher {
    private static final String WIKIPEDIA_API = "https://en.wikipedia.org/api/rest_v1/page/summary/";
    private static final String HACKERNEWS_API = "https://hn.algolia.com/api/v1/search?query=";
    private static final String STACKOVERFLOW_API = "https://api.stackexchange.com/2.3/search/advanced?order=desc&sort=activity&site=stackoverflow&title=";
    private static final String GITHUB_API = "https://api.github.com/search/repositories?q=";
    private static final String ARXIV_API = "https://export.arxiv.org/api/query?search_query=all:";
    private static final String REDDIT_API = "https://www.reddit.com/search.json?q=";

    private static final int TIMEOUT_MS = 10000;

    public static String getWikipediaContent(String topic) {
        try {
            String url = WIKIPEDIA_API + encodeSpace(topic);
            String json = fetchUrl(url);

            String title = extractJson(json, "\"title\":\"", "\",");
            String extract = extractJson(json, "\"extract\":\"", "\",");

            if (title.equals("Not found") || extract.equals("Not found")) {
                return "Wikipedia provides comprehensive knowledge about " + topic +
                        ", though specific details may vary based on current research and understanding.\n\n" +
                        "The topic encompasses various aspects that continue to evolve with new discoveries and insights.";
            }

            // Clean and format the extract into paragraphs
            String cleanedExtract = cleanText(extract);

            // Split into sentences and group into paragraphs
            String[] sentences = cleanedExtract.split("\\. ");
            StringBuilder formatted = new StringBuilder();
            StringBuilder currentParagraph = new StringBuilder();

            for (int i = 0; i < sentences.length; i++) {
                if (!sentences[i].trim().isEmpty()) {
                    currentParagraph.append(sentences[i].trim()).append(". ");

                    // Create paragraphs of 2-3 sentences
                    if ((i + 1) % 3 == 0 || i == sentences.length - 1) {
                        formatted.append(currentParagraph.toString().trim()).append("\n\n");
                        currentParagraph = new StringBuilder();
                    }
                }
            }

            return formatted.toString().trim();

        } catch (Exception e) {
            return "The exploration of " + topic + " represents an important area of knowledge " +
                    "that deserves careful consideration and understanding.\n\n" +
                    "Various perspectives and approaches contribute to our evolving comprehension of this subject.";
        }
    }

    public static String getHackerNewsContent(String topic) {
        try {
            String url = HACKERNEWS_API + encodeSpace(topic) + "&tags=story&hitsPerPage=3";
            String json = fetchUrl(url);

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String title = extractJson(json, "\"title\":\"", "\",", i);
                String points = extractJson(json, "\"points\":", ",", i);
                String author = extractJson(json, "\"author\":\"", "\",", i);

                if (!title.equals("Not found")) {
                    content.append(cleanText(title));
                    if (!points.equals("Not found")) {
                        content.append(" [Community interest: ").append(points).append(" points]");
                    }
                    if (!author.equals("Not found")) {
                        content.append(" - Shared by ").append(author);
                    }
                    content.append("\n\n");
                }
            }

            if (content.length() == 0) {
                return "The tech community actively discusses various aspects of " + topic +
                        ", sharing insights and experiences that contribute to collective understanding.\n\n" +
                        "These discussions often highlight practical applications and emerging trends.";
            }

            return "The tech community has been actively discussing " + topic + ":\n\n" + content.toString().trim();

        } catch (Exception e) {
            return "Tech community discussions around " + topic + " reveal interesting perspectives " +
                    "and practical insights worth exploring further.";
        }
    }

    public static String getStackOverflowContent(String topic) {
        try {
            String url = STACKOVERFLOW_API + encodeSpace(topic) + "&pagesize=3";
            String json = fetchUrl(url);

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String title = extractJson(json, "\"title\":\"", "\",", i);
                String score = extractJson(json, "\"score\":", ",", i);

                if (!title.equals("Not found")) {
                    content.append("💡 ").append(cleanText(title));
                    if (!score.equals("Not found")) {
                        content.append(" [Relevance: ").append(score).append(" votes]");
                    }
                    content.append("\n\n");
                }
            }

            if (content.length() == 0) {
                return "Developers frequently encounter and solve challenges related to " + topic +
                        ", building a rich knowledge base of practical solutions and best practices.\n\n" +
                        "These collective experiences form valuable resources for the development community.";
            }

            return "Developers are actively solving problems related to " + topic + ":\n\n" + content.toString().trim();

        } catch (Exception e) {
            return "The developer community has accumulated significant practical knowledge about " +
                    topic + " through real-world problem solving and collaboration.";
        }
    }

    public static String getGitHubContent(String topic) {
        try {
            String url = GITHUB_API + encodeSpace(topic) + "&sort=stars&order=desc&per_page=3";
            String json = fetchUrl(url);

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String name = extractJson(json, "\"full_name\":\"", "\",", i);
                String description = extractJson(json, "\"description\":\"", "\",", i);
                String stars = extractJson(json, "\"stargazers_count\":", ",", i);

                if (!name.equals("Not found")) {
                    content.append("🔧 ").append(cleanText(name));
                    if (!stars.equals("Not found")) {
                        content.append(" [⭐ ").append(stars).append(" stars]");
                    }
                    if (!description.equals("Not found") && !description.isEmpty()) {
                        content.append("\n").append(cleanText(description));
                    }
                    content.append("\n\n");
                }
            }

            if (content.length() == 0) {
                return "The open-source community actively develops and maintains projects related to " +
                        topic + ", demonstrating the power of collaborative innovation.\n\n" +
                        "These projects represent cutting-edge implementations and practical tools.";
            }

            return "Open-source innovation in " + topic + " is thriving:\n\n" + content.toString().trim();

        } catch (Exception e) {
            return "Open-source development around " + topic + " showcases the collaborative spirit " +
                    "of the developer community and their commitment to shared progress.";
        }
    }

    public static String getArXivContent(String topic) {
        try {
            String url = ARXIV_API + encodeSpace(topic) + "&max_results=3";
            String xml = fetchUrl(url);

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String title = extractXml(xml, "<title>", "</title>", i);
                String summary = extractXml(xml, "<summary>", "</summary>", i);

                if (!title.equals("Not found") && !title.isEmpty()) {
                    content.append("📚 ").append(cleanText(title));
                    if (!summary.equals("Not found") && !summary.isEmpty()) {
                        String cleanSummary = cleanText(summary);
                        if (cleanSummary.length() > 300) {
                            cleanSummary = cleanSummary.substring(0, 300) + "...";
                        }
                        content.append("\n").append(cleanSummary);
                    }
                    content.append("\n\n");
                }
            }

            if (content.length() == 0) {
                return "Academic research continues to advance our understanding of " + topic +
                        ", with scholars exploring various aspects through rigorous methodology.\n\n" +
                        "The academic community contributes valuable theoretical frameworks and empirical findings.";
            }

            return "Academic research is advancing knowledge in " + topic + ":\n\n" + content.toString().trim();

        } catch (Exception e) {
            return "The academic community actively researches " + topic +
                    ", contributing valuable insights through peer-reviewed studies and theoretical advances.";
        }
    }

    public static String getRedditContent(String topic) {
        try {
            String url = REDDIT_API + encodeSpace(topic) + "&sort=relevance&limit=3";
            String json = fetchUrl(url);

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String title = extractJson(json, "\"title\":\"", "\",", i);
                String subreddit = extractJson(json, "\"subreddit\":\"", "\",", i);
                String score = extractJson(json, "\"score\":", ",", i);

                if (!title.equals("Not found") && !title.isEmpty()) {
                    if (!subreddit.equals("Not found")) {
                        content.append("💬 r/").append(subreddit).append(": ");
                    }
                    content.append(cleanText(title));
                    if (!score.equals("Not found")) {
                        content.append(" [Community engagement: ").append(score).append("]");
                    }
                    content.append("\n\n");
                }
            }

            if (content.length() == 0) {
                return "Community discussions about " + topic + " reflect diverse perspectives " +
                        "and real-world experiences that enrich our understanding.\n\n" +
                        "These conversations often reveal practical insights and personal experiences.";
            }

            return "Community perspectives on " + topic + " are diverse and engaging:\n\n" + content.toString().trim();

        } catch (Exception e) {
            return "Community discussions provide valuable grassroots perspectives on " + topic +
                    ", complementing more formal sources of knowledge.";
        }
    }

    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "BlogWeaver/1.0");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static String extractJson(String text, String start, String end) {
        return extractJson(text, start, end, 0);
    }

    private static String extractJson(String text, String start, String end, int occurrence) {
        try {
            int currentIndex = 0;
            int foundCount = 0;

            while (foundCount <= occurrence) {
                int i = text.indexOf(start, currentIndex);
                if (i == -1)
                    return "Not found";

                if (foundCount == occurrence) {
                    i += start.length();
                    int j = text.indexOf(end, i);
                    if (j == -1)
                        return "Not found";
                    return text.substring(i, j);
                }

                currentIndex = i + start.length();
                foundCount++;
            }

            return "Not found";
        } catch (Exception e) {
            return "Error";
        }
    }

    private static String extractXml(String text, String start, String end, int occurrence) {
        try {
            int currentIndex = 0;
            int foundCount = 0;

            while (foundCount <= occurrence) {
                int i = text.indexOf(start, currentIndex);
                if (i == -1)
                    return "Not found";

                if (foundCount == occurrence) {
                    i += start.length();
                    int j = text.indexOf(end, i);
                    if (j == -1)
                        return "Not found";
                    return text.substring(i, j);
                }

                currentIndex = i + start.length();
                foundCount++;
            }

            return "Not found";
        } catch (Exception e) {
            return "Error";
        }
    }

    private static String cleanText(String s) {
        if (s == null)
            return "";
        return s.replace("\\\"", "'")
                .replace("\\n", " ")
                .replace("\\/", "/")
                .replace("\\t", " ")
                .replace("\\r", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String encodeSpace(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s.replace(" ", "%20");
        }
    }
}