package com.example.BuiThiDiemTrinh_TH5.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceDetailResponse;
import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.gui.binding.AddFoodFormData;
import com.example.BuiThiDiemTrinh_TH5.gui.binding.CheckoutFormData;
import com.example.BuiThiDiemTrinh_TH5.gui.binding.InvoiceGuiBinding;
import com.example.BuiThiDiemTrinh_TH5.repository.CafeTableRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;

/**
 * Giao diện Quản lý gọi món & Thanh toán ChinTyn Cafe (Tuần 4 - GUI Integration).
 *
 * Phân công trách nhiệm:
 * • Trinh (Chủ trì thiết kế giao diện):
 *   - Thiết kế giao diện đồ họa hoàn chỉnh dựa trên Java Swing.
 *   - Bố cục trực quan: Sơ đồ bàn dạng lưới (Table Grid), danh sách thực đơn với tìm kiếm tức thì,
 *     bảng chi tiết hóa đơn, bộ tính tiền và in hóa đơn POS đẹp mắt.
 *   - Hỗ trợ thao tác chuột và bàn phím mượt mà: Double-click gọi món, nút tăng/giảm nhanh, phím tắt F5/F9.
 * • Tuyền (Ráp nối Backend):
 *   - Cung cấp cầu nối Data Binding qua {@link InvoiceGuiBinding}.
 *   - Khi bấm "Thêm món vào bàn" hoặc "Thanh toán", hệ thống gọi chính xác nghiệp vụ tính tiền,
 *     trừ kho và lưu trữ hóa đơn xuống Database của tuần 3.
 */
@Component
@ConditionalOnProperty(name = "app.gui.enabled", havingValue = "true")
public class CafeDesktopWindow {

    private static final Logger LOGGER = Logger.getLogger(CafeDesktopWindow.class.getName());

    // Bảng màu nhận diện thương hiệu ChinTyn Cafe
    private static final Color COFFEE = new Color(91, 58, 41);
    private static final Color ESPRESSO = new Color(54, 37, 30);
    private static final Color CARAMEL = new Color(190, 132, 76);
    private static final Color CREAM = new Color(250, 247, 241);
    private static final Color SOFT_CREAM = new Color(246, 241, 233);
    private static final Color BORDER = new Color(231, 224, 215);
    private static final Color SELECTED = new Color(250, 241, 228);
    private static final Color MUTED = new Color(105, 105, 105);
    private static final Color GREEN_BG = new Color(232, 248, 240);
    private static final Color GREEN_TEXT = new Color(38, 128, 77);
    private static final Color ORANGE_BG = new Color(255, 243, 224);
    private static final Color ORANGE_TEXT = new Color(211, 84, 0);

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter CLOCK_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("vi-VN"));

    private final CafeTableRepository tableRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final InvoiceGuiBinding invoiceGuiBinding;

    private final DefaultListModel<Product> productListModel = new DefaultListModel<>();
    private final DefaultTableModel invoiceTableModel = new DefaultTableModel(
            new Object[]{"STT", "Tên món", "SL", "Đơn giá", "Thành tiền"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JFrame frame;
    private JButton refreshButton;
    private JLabel clockLabel;
    private JLabel tableStatsLabel;

    // Sơ đồ bàn & Thực đơn (Cột trái)
    private JPanel tableGridPanel;
    private JComboBox<String> tableFilterSelector;
    private JTextField productSearch;
    private JSpinner quantitySpinner;
    private JList<Product> productList;

    // Hóa đơn & Thanh toán (Cột phải)
    private JComboBox<CafeTable> tableSelector;
    private JComboBox<Employee> employeeSelector;
    private JLabel invoiceStatus;
    private JTable invoiceTable;

    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private JTextField discountCode;
    private JComboBox<String> paymentSelector;
    private JPanel cashPaymentPanel;
    private JTextField cashGivenField;
    private JLabel changeDueLabel;
    private JButton checkoutButton;
    private JButton printReceiptButton;
    private JLabel footerStatus;

    // Bộ nhớ đệm dữ liệu tham chiếu
    private List<CafeTable> tables = List.of();
    private List<Employee> employees = List.of();
    private List<Product> products = List.of();
    private InvoiceResponse currentInvoice;
    private boolean updatingTableSelector;
    private boolean refreshInProgress;
    private Timer clockTimer;

    public CafeDesktopWindow(
            CafeTableRepository tableRepository,
            ProductRepository productRepository,
            EmployeeRepository employeeRepository,
            InvoiceGuiBinding invoiceGuiBinding
    ) {
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
        this.employeeRepository = employeeRepository;
        this.invoiceGuiBinding = invoiceGuiBinding;
    }

    @EventListener
    public void openAfterStartup(ApplicationReadyEvent event) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Giao diện Swing cần môi trường có màn hình. Tắt app.gui.enabled hoặc chạy trên máy có giao diện."
            );
        }
        SwingUtilities.invokeLater(this::createAndShowWindow);
    }

    private void createAndShowWindow() {
        frame = new JFrame("ChinTyn Cafe | Hệ thống Quản lý Gọi món & Thanh toán");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1150, 720));
        frame.setSize(1366, 840);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(CREAM);
        frame.setLayout(new BorderLayout(0, 0));

        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(createWorkspace(), BorderLayout.CENTER);
        frame.add(createFooter(), BorderLayout.SOUTH);

        setupKeyboardShortcuts();
        startClock();

        try {
            refreshReferenceData(null);
        } catch (RuntimeException exception) {
            showError(exception);
        }

        frame.setVisible(true);
    }

    private void startClock() {
        clockTimer = new Timer(1000, event -> {
            if (clockLabel != null) {
                clockLabel.setText(LocalDateTime.now().format(CLOCK_FORMATTER));
            }
        });
        clockTimer.start();
    }

    private void setupKeyboardShortcuts() {
        frame.getRootPane().registerKeyboardAction(
                event -> refreshReferenceDataInBackground(selectedTableId()),
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        frame.getRootPane().registerKeyboardAction(
                event -> runAction(this::checkout),
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel createHeader() {
        JPanel header = new GradientHeader();
        header.setLayout(new BorderLayout(16, 0));
        header.setBorder(BorderFactory.createEmptyBorder(16, 26, 16, 26));

        // Logo và Tên thương hiệu
        JLabel brandMark = new JLabel("C");
        brandMark.setHorizontalAlignment(JLabel.CENTER);
        brandMark.setOpaque(true);
        brandMark.setBackground(CARAMEL);
        brandMark.setForeground(Color.WHITE);
        brandMark.setFont(new Font(Font.SERIF, Font.BOLD, 26));
        brandMark.setPreferredSize(new Dimension(50, 50));
        brandMark.setBorder(BorderFactory.createLineBorder(new Color(235, 196, 150), 2, true));

        JLabel title = new JLabel("CHINTYN CAFE");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        JLabel subtitle = new JLabel("QUẢN LÝ GỌI MÓN · THANH TOÁN TẠI BÀN · TUẦN 4");
        subtitle.setForeground(new Color(230, 215, 200));
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        JPanel titles = new JPanel(new GridBagLayout());
        titles.setOpaque(false);
        GridBagConstraints brandConstraints = constraints(0, 0);
        brandConstraints.gridheight = 2;
        brandConstraints.insets = new Insets(0, 0, 0, 14);
        titles.add(brandMark, brandConstraints);
        titles.add(title, constraints(1, 0));
        GridBagConstraints subtitleConstraints = constraints(0, 1);
        subtitleConstraints.gridx = 1;
        subtitleConstraints.insets = new Insets(4, 2, 0, 0);
        titles.add(subtitle, subtitleConstraints);

        // Khu vực phải: Nút Làm mới dữ liệu độc lập, thoáng đãng, không bị đè chữ
        refreshButton = new JButton("🔄 Làm mới dữ liệu (F5)");
        secondaryButton(refreshButton);
        refreshButton.addActionListener(event -> refreshReferenceDataInBackground(selectedTableId()));

        header.add(titles, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(CREAM);
        workspace.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

        // Cột trái: Sơ đồ bàn & Menu thực đơn (48% width)
        GridBagConstraints leftConstraints = constraints(0, 0);
        leftConstraints.weightx = 0.48;
        leftConstraints.weighty = 1.0;
        leftConstraints.fill = GridBagConstraints.BOTH;
        leftConstraints.insets = new Insets(0, 0, 0, 8);
        workspace.add(createLeftPanel(), leftConstraints);

        // Cột phải: Chi tiết Hóa đơn & Thanh toán (52% width)
        GridBagConstraints rightConstraints = constraints(1, 0);
        rightConstraints.weightx = 0.52;
        rightConstraints.weighty = 1.0;
        rightConstraints.fill = GridBagConstraints.BOTH;
        rightConstraints.insets = new Insets(0, 8, 0, 0);
        workspace.add(createOrderPanel(), rightConstraints);

        return workspace;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        // Khối 1: Sơ đồ bàn trực quan (Table Map Grid)
        GridBagConstraints tableMapConstraints = constraints(0, 0);
        tableMapConstraints.weightx = 1.0;
        tableMapConstraints.weighty = 0.38;
        tableMapConstraints.fill = GridBagConstraints.BOTH;
        tableMapConstraints.insets = new Insets(0, 0, 8, 0);
        panel.add(createTableMapPanel(), tableMapConstraints);

        // Khối 2: Thực đơn & Gọi món
        GridBagConstraints menuConstraints = constraints(0, 1);
        menuConstraints.weightx = 1.0;
        menuConstraints.weighty = 0.62;
        menuConstraints.fill = GridBagConstraints.BOTH;
        panel.add(createMenuPanel(), menuConstraints);

        return panel;
    }

    private JPanel createTableMapPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 8));

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        topBar.add(sectionTitle("Sơ đồ bàn quán cà phê"), BorderLayout.WEST);

        tableFilterSelector = new JComboBox<>(new String[]{"Tất cả bàn", "🟢 Bàn trống", "🟠 Đang dùng"});
        styleInput(tableFilterSelector);
        tableFilterSelector.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tableFilterSelector.addActionListener(event -> renderTableGrid());
        topBar.add(tableFilterSelector, BorderLayout.EAST);

        // Danh sách thẻ bàn trực quan
        tableGridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tableGridPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = styledScrollPane(tableGridPanel);
        scrollPane.setPreferredSize(new Dimension(100, 130));

        card.add(topBar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMenuPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 10));

        // Thanh tìm kiếm món ăn
        productSearch = new JTextField();
        productSearch.setToolTipText("Nhập tên món để tìm kiếm nhanh");
        styleInput(productSearch);
        productSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                filterProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                filterProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                filterProducts();
            }
        });

        JPanel searchBar = new JPanel(new BorderLayout(8, 6));
        searchBar.setOpaque(false);
        searchBar.add(sectionTitle("Thực đơn đồ uống & Món ăn"), BorderLayout.NORTH);
        searchBar.add(productSearch, BorderLayout.CENTER);

        // Danh sách món ăn
        productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setCellRenderer(new ProductCellRenderer());
        productList.setBackground(Color.WHITE);
        productList.setFixedCellHeight(64);
        productList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // Nhấp đúp chuột vào món để thêm nhanh 1 phần
        productList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)) {
                    runAction(CafeDesktopWindow.this::addSelectedProduct);
                }
            }
        });

        // Bấm phím Enter trên danh sách món cũng thêm món
        productList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    runAction(CafeDesktopWindow.this::addSelectedProduct);
                }
            }
        });

        // Khung điều khiển số lượng & Nút Thêm món
        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setOpaque(false);
        addPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JLabel qtyLabel = new JLabel("Số lượng:");
        qtyLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        qtyLabel.setForeground(ESPRESSO);

        JButton minusButton = new JButton("➖");
        buttonToolbarStyle(minusButton);
        minusButton.setPreferredSize(new Dimension(36, 36));
        minusButton.addActionListener(event -> {
            int current = (Integer) quantitySpinner.getValue();
            if (current > 1) {
                quantitySpinner.setValue(current - 1);
            }
        });

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        styleInput(quantitySpinner);
        quantitySpinner.setPreferredSize(new Dimension(58, 36));

        JButton plusButton = new JButton("➕");
        buttonToolbarStyle(plusButton);
        plusButton.setPreferredSize(new Dimension(36, 36));
        plusButton.addActionListener(event -> {
            int current = (Integer) quantitySpinner.getValue();
            quantitySpinner.setValue(current + 1);
        });

        JButton addButton = primaryButton("➕ Thêm món vào bàn");
        addButton.addActionListener(event -> runAction(this::addSelectedProduct));

        GridBagConstraints labelConstraints = constraints(0, 0);
        labelConstraints.anchor = GridBagConstraints.WEST;
        addPanel.add(qtyLabel, labelConstraints);

        GridBagConstraints minusConstraints = constraints(1, 0);
        minusConstraints.insets = new Insets(0, 8, 0, 2);
        addPanel.add(minusButton, minusConstraints);

        GridBagConstraints spinnerConstraints = constraints(2, 0);
        spinnerConstraints.insets = new Insets(0, 0, 0, 2);
        addPanel.add(quantitySpinner, spinnerConstraints);

        GridBagConstraints plusConstraints = constraints(3, 0);
        plusConstraints.insets = new Insets(0, 0, 0, 10);
        addPanel.add(plusButton, plusConstraints);

        GridBagConstraints addBtnConstraints = constraints(4, 0);
        addBtnConstraints.weightx = 1.0;
        addBtnConstraints.fill = GridBagConstraints.HORIZONTAL;
        addPanel.add(addButton, addBtnConstraints);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(styledScrollPane(productList), BorderLayout.CENTER);
        panel.add(addPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createOrderPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 10));

        // Khung thông tin bàn & nhân viên phục vụ
        JPanel orderHeader = new JPanel(new GridBagLayout());
        orderHeader.setOpaque(false);

        GridBagConstraints tableLabelConstraints = constraints(0, 0);
        tableLabelConstraints.anchor = GridBagConstraints.WEST;
        orderHeader.add(new JLabel("Bàn:"), tableLabelConstraints);

        tableSelector = new JComboBox<>();
        tableSelector.setRenderer(new TableCellRenderer());
        styleInput(tableSelector);
        tableSelector.addActionListener(event -> {
            if (!updatingTableSelector) {
                loadSelectedTableInvoice();
                updateTableGridHighlight();
            }
        });
        GridBagConstraints tableSelectorConstraints = constraints(1, 0);
        tableSelectorConstraints.weightx = 1.0;
        tableSelectorConstraints.fill = GridBagConstraints.HORIZONTAL;
        tableSelectorConstraints.insets = new Insets(0, 8, 0, 0);
        orderHeader.add(tableSelector, tableSelectorConstraints);

        GridBagConstraints employeeLabelConstraints = constraints(0, 1);
        employeeLabelConstraints.anchor = GridBagConstraints.WEST;
        employeeLabelConstraints.insets = new Insets(8, 0, 0, 0);
        orderHeader.add(new JLabel("Nhân viên:"), employeeLabelConstraints);

        employeeSelector = new JComboBox<>();
        employeeSelector.setRenderer(new EmployeeCellRenderer());
        styleInput(employeeSelector);
        GridBagConstraints employeeSelectorConstraints = constraints(1, 1);
        employeeSelectorConstraints.fill = GridBagConstraints.HORIZONTAL;
        employeeSelectorConstraints.insets = new Insets(8, 8, 0, 0);
        orderHeader.add(employeeSelector, employeeSelectorConstraints);

        // Banner trạng thái hóa đơn
        invoiceStatus = new JLabel("Chọn bàn để xem hóa đơn");
        invoiceStatus.setForeground(COFFEE);
        invoiceStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        invoiceStatus.setOpaque(true);
        invoiceStatus.setBackground(SOFT_CREAM);
        invoiceStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(sectionTitle("Hóa đơn bàn hiện tại"), BorderLayout.NORTH);
        titlePanel.add(orderHeader, BorderLayout.CENTER);
        titlePanel.add(invoiceStatus, BorderLayout.SOUTH);

        // Bảng chi tiết món đã gọi
        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(36);
        invoiceTable.setFillsViewportHeight(true);
        invoiceTable.setShowGrid(false);
        invoiceTable.setIntercellSpacing(new Dimension(0, 0));
        invoiceTable.setSelectionBackground(SELECTED);
        invoiceTable.setSelectionForeground(ESPRESSO);
        invoiceTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        invoiceTable.setForeground(ESPRESSO);
        invoiceTable.getTableHeader().setReorderingAllowed(false);
        invoiceTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        invoiceTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        invoiceTable.getTableHeader().setBackground(SOFT_CREAM);
        invoiceTable.getTableHeader().setForeground(MUTED);
        invoiceTable.setDefaultRenderer(Object.class, new InvoiceCellRenderer());

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(styledScrollPane(invoiceTable), BorderLayout.CENTER);
        panel.add(createTotalsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 218, 208)));

        subtotalLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        subtotalLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        discountLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        discountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        discountLabel.setForeground(GREEN_TEXT);

        totalLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        totalLabel.setForeground(CARAMEL);

        addSummaryRow(panel, 0, "Tạm tính:", subtotalLabel);
        addSummaryRow(panel, 1, "Giảm giá:", discountLabel);
        addSummaryRow(panel, 2, "Khách cần trả:", totalLabel);

        // Khung mã giảm giá & Phương thức thanh toán
        discountCode = new JTextField();
        discountCode.setToolTipText("Mã giảm giá (ví dụ: GIAM10, GIAM20, VIP, CAFE50K)");
        styleInput(discountCode);

        paymentSelector = new JComboBox<>(new String[]{
                "Tiền mặt", "Chuyển khoản", "Thẻ", "Ví điện tử"
        });
        styleInput(paymentSelector);
        paymentSelector.addActionListener(event -> toggleCashPaymentPanel());

        JPanel checkoutFields = new JPanel(new GridLayout(1, 2, 8, 0));
        checkoutFields.setOpaque(false);
        checkoutFields.add(discountCode);
        checkoutFields.add(paymentSelector);

        // Khung nhập tiền khách đưa & tính tiền thối lại khi thanh toán tiền mặt
        cashPaymentPanel = createCashPaymentPanel();

        // Hàng nút Thanh toán và Xem/In phiếu hóa đơn
        checkoutButton = primaryButton("💳 THANH TOÁN (F9)");
        checkoutButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        checkoutButton.addActionListener(event -> runAction(this::checkout));

        printReceiptButton = new JButton("🖨️ In hóa đơn");
        buttonToolbarStyle(printReceiptButton);
        printReceiptButton.addActionListener(event -> runAction(this::directPrintReceipt));

        JPanel actionButtonPanel = new JPanel(new BorderLayout(8, 0));
        actionButtonPanel.setOpaque(false);
        actionButtonPanel.add(checkoutButton, BorderLayout.CENTER);
        actionButtonPanel.add(printReceiptButton, BorderLayout.EAST);

        GridBagConstraints fieldsConstraints = constraints(0, 3);
        fieldsConstraints.gridwidth = 2;
        fieldsConstraints.weightx = 1.0;
        fieldsConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldsConstraints.insets = new Insets(10, 0, 6, 0);
        panel.add(checkoutFields, fieldsConstraints);

        GridBagConstraints cashConstraints = constraints(0, 4);
        cashConstraints.gridwidth = 2;
        cashConstraints.weightx = 1.0;
        cashConstraints.fill = GridBagConstraints.HORIZONTAL;
        cashConstraints.insets = new Insets(0, 0, 6, 0);
        panel.add(cashPaymentPanel, cashConstraints);

        GridBagConstraints buttonConstraints = constraints(0, 5);
        buttonConstraints.gridwidth = 2;
        buttonConstraints.weightx = 1.0;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(4, 0, 2, 0);
        panel.add(actionButtonPanel, buttonConstraints);

        return panel;
    }

    private JPanel createCashPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(SOFT_CREAM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JLabel cashLabel = new JLabel("Tiền khách đưa:");
        cashLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        cashLabel.setForeground(ESPRESSO);

        cashGivenField = new JTextField();
        styleInput(cashGivenField);
        cashGivenField.setPreferredSize(new Dimension(110, 32));
        cashGivenField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calculateCashChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calculateCashChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calculateCashChange();
            }
        });

        // Nút chọn nhanh "Vừa đủ"
        JButton exactButton = new JButton("Vừa đủ");
        buttonToolbarStyle(exactButton);
        exactButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        exactButton.addActionListener(event -> {
            if (currentInvoice != null && currentInvoice.totalAmount() != null) {
                cashGivenField.setText(currentInvoice.totalAmount().toPlainString());
            }
        });

        changeDueLabel = new JLabel("Tiền thối lại: 0 ₫");
        changeDueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        changeDueLabel.setForeground(GREEN_TEXT);

        GridBagConstraints labelConstraints = constraints(0, 0);
        labelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(cashLabel, labelConstraints);

        GridBagConstraints inputConstraints = constraints(1, 0);
        inputConstraints.insets = new Insets(0, 6, 0, 4);
        panel.add(cashGivenField, inputConstraints);

        GridBagConstraints exactConstraints = constraints(2, 0);
        exactConstraints.insets = new Insets(0, 0, 0, 8);
        panel.add(exactButton, exactConstraints);

        GridBagConstraints changeConstraints = constraints(3, 0);
        changeConstraints.weightx = 1.0;
        changeConstraints.anchor = GridBagConstraints.EAST;
        panel.add(changeDueLabel, changeConstraints);

        return panel;
    }

    private void toggleCashPaymentPanel() {
        boolean isCash = paymentSelector.getSelectedIndex() == 0;
        cashPaymentPanel.setVisible(isCash);
        calculateCashChange();
    }

    private void calculateCashChange() {
        if (currentInvoice == null || currentInvoice.totalAmount() == null) {
            changeDueLabel.setText("Tiền thối lại: 0 ₫");
            changeDueLabel.setForeground(GREEN_TEXT);
            return;
        }

        String input = cashGivenField.getText().trim().replace(".", "").replace(",", "");
        if (input.isEmpty()) {
            changeDueLabel.setText("Tiền thối lại: 0 ₫");
            changeDueLabel.setForeground(MUTED);
            return;
        }

        try {
            BigDecimal cash = new BigDecimal(input);
            BigDecimal change = cash.subtract(currentInvoice.totalAmount());
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeDueLabel.setText("Tiền thối lại: " + formatCurrency(change));
                changeDueLabel.setForeground(GREEN_TEXT);
            } else {
                changeDueLabel.setText("Khách đưa thiếu: " + formatCurrency(change.abs()));
                changeDueLabel.setForeground(new Color(192, 57, 43));
            }
        } catch (NumberFormatException exception) {
            changeDueLabel.setText("Số tiền không hợp lệ");
            changeDueLabel.setForeground(new Color(192, 57, 43));
        }
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 26, 12, 26));

        footerStatus = new JLabel("Hệ thống sẵn sàng phục vụ");
        footerStatus.setForeground(MUTED);
        footerStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        tableStatsLabel = new JLabel("Đang tải dữ liệu bàn...");
        tableStatsLabel.setForeground(COFFEE);
        tableStatsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tableStatsLabel.setHorizontalAlignment(JLabel.CENTER);

        clockLabel = new JLabel(LocalDateTime.now().format(CLOCK_FORMATTER));
        clockLabel.setForeground(MUTED);
        clockLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        clockLabel.setHorizontalAlignment(JLabel.RIGHT);

        panel.add(footerStatus, BorderLayout.WEST);
        panel.add(tableStatsLabel, BorderLayout.CENTER);
        panel.add(clockLabel, BorderLayout.EAST);
        return panel;
    }

    private void refreshReferenceData(Long tableIdToSelect) {
        ReferenceData data = fetchReferenceData();
        applyReferenceData(data, tableIdToSelect);
        footerStatus.setText("Đã nạp " + data.tables().size() + " bàn, " + data.products().size()
                + " món và " + data.employees().size() + " nhân viên");
        loadSelectedTableInvoice();
    }

    private ReferenceData fetchReferenceData() {
        List<CafeTable> tableList = tableRepository.findAll();
        List<Employee> employeeList = employeeRepository.findAll();
        List<Product> availableProducts = productRepository.findAll().stream()
                .filter(product -> product.isAvailable() && product.getQuantity() != null && product.getQuantity() > 0)
                .toList();
        return new ReferenceData(tableList, employeeList, availableProducts);
    }

    private void applyReferenceData(ReferenceData data, Long tableIdToSelect) {
        Employee previouslySelectedEmployee = (Employee) employeeSelector.getSelectedItem();
        Long employeeIdToSelect = previouslySelectedEmployee == null ? null : previouslySelectedEmployee.getId();

        tables = data.tables();
        employees = data.employees();
        products = data.products();

        updatingTableSelector = true;
        try {
            tableSelector.removeAllItems();
            for (CafeTable table : tables) {
                tableSelector.addItem(table);
            }
            if (tableIdToSelect != null) {
                for (int index = 0; index < tableSelector.getItemCount(); index++) {
                    if (tableSelector.getItemAt(index).getId().equals(tableIdToSelect)) {
                        tableSelector.setSelectedIndex(index);
                        break;
                    }
                }
            }

            employeeSelector.removeAllItems();
            for (Employee employee : employees) {
                employeeSelector.addItem(employee);
            }
            if (employeeIdToSelect != null) {
                for (int index = 0; index < employeeSelector.getItemCount(); index++) {
                    if (employeeSelector.getItemAt(index).getId().equals(employeeIdToSelect)) {
                        employeeSelector.setSelectedIndex(index);
                        break;
                    }
                }
            }
        } finally {
            updatingTableSelector = false;
        }

        renderTableGrid();
        filterProducts();
        updateTableStatistics();
    }

    private void updateTableStatistics() {
        long total = tables.size();
        long occupied = tables.stream().filter(t -> t.getStatus() == TableStatus.OCCUPIED).count();
        long available = total - occupied;
        tableStatsLabel.setText("Tổng: " + total + " bàn  |  🟢 Trống: " + available + "  |  🟠 Đang dùng: " + occupied);
    }

    private void renderTableGrid() {
        tableGridPanel.removeAll();
        Long currentSelectedId = selectedTableId();
        int filterMode = tableFilterSelector.getSelectedIndex(); // 0: Tất cả, 1: Trống, 2: Đang dùng

        for (CafeTable table : tables) {
            if (filterMode == 1 && table.getStatus() != TableStatus.AVAILABLE) {
                continue;
            }
            if (filterMode == 2 && table.getStatus() != TableStatus.OCCUPIED) {
                continue;
            }

            JButton tableButton = createTableButton(table, table.getId().equals(currentSelectedId));
            tableGridPanel.add(tableButton);
        }
        tableGridPanel.revalidate();
        tableGridPanel.repaint();
    }

    private JButton createTableButton(CafeTable table, boolean isSelected) {
        boolean isOccupied = table.getStatus() == TableStatus.OCCUPIED;
        String statusText = isOccupied ? "🟠 Đang dùng" : "🟢 Trống";
        String htmlText = "<html><center><b>Bàn " + table.getTableNumber() + "</b><br>"
                + "<font size='2' color='" + (isOccupied ? "#D35400" : "#27AE60") + "'>" + statusText + "</font><br>"
                + "<font size='2' color='#7F8C8D'>" + table.getCapacity() + " chỗ</font></center></html>";

        JButton button = new JButton(htmlText);
        button.setPreferredSize(new Dimension(108, 62));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(isOccupied ? ORANGE_BG : (isSelected ? SELECTED : GREEN_BG));

        if (isSelected) {
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARAMEL, 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        } else {
            button.setBorder(BorderFactory.createLineBorder(isOccupied ? new Color(245, 205, 160) : new Color(185, 225, 200)));
        }

        button.addActionListener(event -> {
            for (int i = 0; i < tableSelector.getItemCount(); i++) {
                if (tableSelector.getItemAt(i).getId().equals(table.getId())) {
                    tableSelector.setSelectedIndex(i);
                    break;
                }
            }
        });
        return button;
    }

    private void updateTableGridHighlight() {
        renderTableGrid();
    }

    private void refreshReferenceDataInBackground(Long tableIdToSelect) {
        if (refreshInProgress) {
            return;
        }
        refreshInProgress = true;
        refreshButton.setEnabled(false);
        refreshButton.setText("Đang tải...");
        footerStatus.setText("Đang tải danh sách bàn, món và nhân viên từ cơ sở dữ liệu...");

        new SwingWorker<ReferenceData, Void>() {
            @Override
            protected ReferenceData doInBackground() {
                return fetchReferenceData();
            }

            @Override
            protected void done() {
                try {
                    ReferenceData data = get();
                    applyReferenceData(data, tableIdToSelect);
                    footerStatus.setText("Đã nạp " + data.tables().size() + " bàn, " + data.products().size()
                            + " món và " + data.employees().size() + " nhân viên");
                    loadSelectedTableInvoice();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError(new IllegalStateException("Đã gián đoạn khi tải dữ liệu.", exception));
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause instanceof RuntimeException runtimeException
                            ? runtimeException
                            : new IllegalStateException("Không thể tải dữ liệu từ database.", cause));
                } finally {
                    refreshInProgress = false;
                    refreshButton.setEnabled(true);
                    refreshButton.setText("🔄 Làm mới dữ liệu (F5)");
                }
            }
        }.execute();
    }

    private void filterProducts() {
        String keyword = productSearch.getText().trim().toLowerCase(Locale.ROOT);
        Product selected = productList.getSelectedValue();
        productListModel.clear();

        for (Product product : products) {
            if (product.getName().toLowerCase(Locale.ROOT).contains(keyword)) {
                productListModel.addElement(product);
                if (selected != null && selected.getId().equals(product.getId())) {
                    productList.setSelectedValue(product, true);
                }
            }
        }
    }

    private void loadSelectedTableInvoice() {
        CafeTable table = (CafeTable) tableSelector.getSelectedItem();
        if (table == null) {
            currentInvoice = null;
            renderInvoice(null);
            return;
        }
        try {
            currentInvoice = invoiceGuiBinding.loadOpenInvoice(table.getId().toString());
            renderInvoice(currentInvoice);
            footerStatus.setText("Đang xem hóa đơn bàn " + table.getTableNumber());
        } catch (ResourceNotFoundException exception) {
            currentInvoice = null;
            renderInvoice(null);
            invoiceStatus.setText("Bàn " + table.getTableNumber() + " chưa có hóa đơn mở · Sẵn sàng nhận khách");
        } catch (RuntimeException exception) {
            currentInvoice = null;
            renderInvoice(null);
            showError(exception);
        }
    }

    private void addSelectedProduct() {
        CafeTable table = (CafeTable) tableSelector.getSelectedItem();
        Product product = productList.getSelectedValue();
        Employee employee = (Employee) employeeSelector.getSelectedItem();

        if (table == null) {
            showMessage("Vui lòng chọn bàn trước khi gọi món.", "Chưa chọn bàn");
            return;
        }
        if (product == null) {
            showMessage("Vui lòng chọn một món trong thực đơn.", "Chưa chọn món");
            return;
        }
        if ((currentInvoice == null || currentInvoice.status() != InvoiceStatus.OPEN) && employee == null) {
            showMessage("Cần chọn nhân viên phục vụ khi mở hóa đơn mới.", "Chưa chọn nhân viên");
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        InvoiceResponse invoice = invoiceGuiBinding.addFood(new AddFoodFormData(
                table.getId().toString(),
                employee == null ? null : employee.getId().toString(),
                null,
                product.getId().toString(),
                Integer.toString(quantity)
        ));

        currentInvoice = invoice;
        renderInvoice(invoice);
        refreshReferenceData(table.getId());
        currentInvoice = invoice;
        renderInvoice(invoice);
        footerStatus.setText("Đã thêm " + quantity + " " + product.getName()
                + " vào hóa đơn bàn " + table.getTableNumber());
    }

    private void checkout() {
        if (currentInvoice == null || currentInvoice.status() != InvoiceStatus.OPEN) {
            showMessage("Bàn đang chọn không có hóa đơn mở để thanh toán.", "Không có hóa đơn mở");
            return;
        }

        String paymentMethod = switch (paymentSelector.getSelectedIndex()) {
            case 1 -> PaymentMethod.BANK_TRANSFER.name();
            case 2 -> PaymentMethod.CARD.name();
            case 3 -> PaymentMethod.E_WALLET.name();
            default -> PaymentMethod.CASH.name();
        };

        InvoiceResponse paidInvoice = invoiceGuiBinding.checkout(new CheckoutFormData(
                currentInvoice.id().toString(),
                paymentMethod,
                discountCode.getText(),
                null,
                null
        ));

        currentInvoice = paidInvoice;
        renderInvoice(paidInvoice);
        refreshReferenceData(paidInvoice.tableId());
        currentInvoice = paidInvoice;
        renderInvoice(paidInvoice);
        footerStatus.setText("Đã thanh toán hóa đơn #" + paidInvoice.id());

        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Thanh toán thành công hóa đơn #" + paidInvoice.id() + "!\n"
                        + "Tổng tiền: " + formatCurrency(paidInvoice.totalAmount()) + "\n\n"
                        + "Bạn có muốn in hóa đơn thẳng ra máy in không?",
                "Thanh toán hoàn tất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            directPrintReceipt();
        } else {
            footerStatus.setText("Thanh toán thành công hóa đơn #" + paidInvoice.id() + " · Sẵn sàng phục vụ tiếp theo");
        }
    }

    private void directPrintReceipt() {
        if (currentInvoice == null) {
            showMessage("Không có hóa đơn nào để in.", "Chưa có hóa đơn");
            return;
        }

        BigDecimal cashGiven = null;
        BigDecimal changeDue = null;
        String cashText = cashGivenField.getText().trim().replace(".", "").replace(",", "");
        if (!cashText.isEmpty()) {
            try {
                cashGiven = new BigDecimal(cashText);
                if (currentInvoice.totalAmount() != null) {
                    changeDue = cashGiven.subtract(currentInvoice.totalAmount()).max(BigDecimal.ZERO);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String receiptText = buildReceiptText(currentInvoice, cashGiven, changeDue);
        JTextArea printArea = new JTextArea(receiptText);
        printArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

        try {
            // In thẳng ra máy in thông qua lệnh in hệ thống, không cần lưu file
            boolean completed = printArea.print(null, null, true, null, null, true);
            if (completed) {
                footerStatus.setText("Đã gửi lệnh in hóa đơn #" + currentInvoice.id() + " ra máy in thành công");
                showMessage("Đã in hóa đơn #" + currentInvoice.id() + " thành công!", "In hóa đơn hoàn tất");
            } else {
                footerStatus.setText("Đã hủy thao tác in hóa đơn #" + currentInvoice.id());
            }
        } catch (PrinterException ex) {
            showError(new IllegalStateException("Lỗi khi gửi lệnh in: " + ex.getMessage(), ex));
        }
    }

    private String buildReceiptText(InvoiceResponse invoice, BigDecimal cashGiven, BigDecimal changeDue) {
        if (invoice == null) {
            return "Chưa có thông tin hóa đơn.";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("                  CHINTYN CAFE                      \n");
        sb.append("         HỆ THỐNG QUẢN LÝ QUÁN CÀ PHÊ               \n");
        sb.append("     Đ/c: 123 Đường Cà Phê, Quận 1, TP. HCM         \n");
        sb.append("               Hotline: 0909 123 456                \n");
        sb.append("====================================================\n");
        sb.append("                 PHIẾU THANH TOÁN                   \n\n");
        sb.append(String.format("Hóa đơn số: #%-10d Bàn: Bàn %s\n", invoice.id(), invoice.tableNumber() != null ? invoice.tableNumber() : "-"));
        sb.append(String.format("Thời gian vào: %s\n", invoice.createdAt() != null ? invoice.createdAt().format(formatter) : "-"));
        if (invoice.paidAt() != null) {
            sb.append(String.format("Thanh toán lúc: %s\n", invoice.paidAt().format(formatter)));
        }
        sb.append(String.format("Thu ngân: %s\n", invoice.employeeName() != null ? invoice.employeeName() : "Hệ thống"));
        sb.append(String.format("Khách hàng: %s\n", invoice.customerName() != null ? invoice.customerName() : "Khách vãng lai"));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-22s %4s %11s %12s\n", "Tên món", "SL", "Đơn giá", "Thành tiền"));
        sb.append("----------------------------------------------------\n");
        if (invoice.details() != null) {
            for (var detail : invoice.details()) {
                String name = detail.productName();
                if (name.length() > 20) {
                    name = name.substring(0, 18) + "..";
                }
                sb.append(String.format("%-22s %4d %11s %12s\n",
                        name,
                        detail.quantity(),
                        formatCurrency(detail.unitPrice()),
                        formatCurrency(detail.lineTotal())));
            }
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-28s %21s\n", "Tạm tính:", formatCurrency(invoice.subtotal())));
        sb.append(String.format("%-28s %21s\n", "Giảm giá:", "-" + formatCurrency(invoice.discount())));
        sb.append("====================================================\n");
        sb.append(String.format("%-26s %23s\n", "TỔNG THANH TOÁN:", formatCurrency(invoice.totalAmount())));
        sb.append("====================================================\n");
        sb.append(String.format("Phương thức: %s\n", invoice.paymentMethod() != null ? invoice.paymentMethod().name() : "TIỀN MẶT"));
        if (cashGiven != null && cashGiven.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-28s %21s\n", "Tiền khách đưa:", formatCurrency(cashGiven)));
            sb.append(String.format("%-28s %21s\n", "Tiền thối lại:", formatCurrency(changeDue != null ? changeDue : BigDecimal.ZERO)));
        }
        sb.append("----------------------------------------------------\n");
        sb.append("           Cảm ơn Quý khách & Hẹn gặp lại!          \n");
        sb.append("               Wifi: ChinTynCafe2026                \n");
        sb.append("====================================================\n");
        return sb.toString();
    }

    private void renderInvoice(InvoiceResponse invoice) {
        invoiceTableModel.setRowCount(0);
        if (invoice == null) {
            invoiceStatus.setText("Chọn bàn để xem hóa đơn");
            updateTotals(null);
            printReceiptButton.setEnabled(false);
            return;
        }

        int index = 1;
        for (InvoiceDetailResponse detail : invoice.details()) {
            invoiceTableModel.addRow(new Object[]{
                    index++,
                    detail.productName(),
                    detail.quantity(),
                    formatCurrency(detail.unitPrice()),
                    formatCurrency(detail.lineTotal())
            });
        }

        boolean isOpen = invoice.status() == InvoiceStatus.OPEN;
        String statusText = isOpen ? "ĐANG PHỤC VỤ" : "ĐÃ THANH TOÁN";
        invoiceStatus.setText("📋 Hóa đơn #" + invoice.id() + " · " + statusText
                + (invoice.createdAt() != null ? " · Giờ vào: " + invoice.createdAt().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
        invoiceStatus.setBackground(isOpen ? ORANGE_BG : GREEN_BG);
        invoiceStatus.setForeground(isOpen ? ORANGE_TEXT : GREEN_TEXT);
        printReceiptButton.setEnabled(true);

        updateTotals(invoice);
        calculateCashChange();
    }

    private void updateTotals(InvoiceResponse invoice) {
        subtotalLabel.setText(invoice == null ? formatCurrency(BigDecimal.ZERO) : formatCurrency(invoice.subtotal()));
        discountLabel.setText(invoice == null ? formatCurrency(BigDecimal.ZERO) : "-" + formatCurrency(invoice.discount()));
        totalLabel.setText(invoice == null ? formatCurrency(BigDecimal.ZERO) : formatCurrency(invoice.totalAmount()));
    }

    private Long selectedTableId() {
        CafeTable table = tableSelector == null ? null : (CafeTable) tableSelector.getSelectedItem();
        return table == null ? null : table.getId();
    }

    private void runAction(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void showError(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "Đã xảy ra lỗi khi xử lý yêu cầu.";
        }
        LOGGER.log(Level.SEVERE, "Không thể xử lý thao tác giao diện", exception);
        showMessage(message, "Thông báo");
        footerStatus.setText("Lỗi: " + message);
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createCard() {
        return new CardPanel();
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        label.setForeground(COFFEE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        return label;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(COFFEE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COFFEE),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        return button;
    }

    private void secondaryButton(JButton button) {
        button.setBackground(new Color(255, 255, 255, 25));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 205, 185)),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
    }

    private void buttonToolbarStyle(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(ESPRESSO);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleInput(JComponent component) {
        component.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        if (component instanceof JTextField textField) {
            textField.setBackground(Color.WHITE);
            textField.setForeground(ESPRESSO);
        } else if (component instanceof JComboBox<?> comboBox) {
            comboBox.setBackground(Color.WHITE);
            comboBox.setForeground(ESPRESSO);
        } else if (component instanceof JSpinner spinner) {
            spinner.setBackground(Color.WHITE);
            spinner.setForeground(ESPRESSO);
        }
    }

    private JScrollPane styledScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void addSummaryRow(JPanel panel, int row, String caption, JLabel value) {
        GridBagConstraints captionConstraints = constraints(0, row);
        captionConstraints.anchor = GridBagConstraints.WEST;
        captionConstraints.insets = new Insets(4, 0, 0, 0);
        panel.add(new JLabel(caption), captionConstraints);

        GridBagConstraints valueConstraints = constraints(1, row);
        valueConstraints.anchor = GridBagConstraints.EAST;
        valueConstraints.weightx = 1.0;
        valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        valueConstraints.insets = new Insets(4, 8, 0, 0);
        panel.add(value, valueConstraints);
    }

    private GridBagConstraints constraints(int gridx, int gridy) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.insets = new Insets(2, 2, 2, 2);
        return c;
    }

    private String formatCurrency(BigDecimal amount) {
        return CURRENCY.format(amount == null ? BigDecimal.ZERO : amount);
    }

    // --- Custom Renderers ---

    private static final class ProductCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            label.setBackground(isSelected ? SELECTED : Color.WHITE);
            label.setForeground(ESPRESSO);

            if (value instanceof Product product) {
                String safeName = product.getName()
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
                String stockWarning = (product.getQuantity() != null && product.getQuantity() <= 5)
                        ? " <font color='#C0392B'>(Sắp hết: " + product.getQuantity() + ")</font>"
                        : " <font color='#7F8C8D'>(Còn: " + product.getQuantity() + ")</font>";

                label.setText("<html><b>" + safeName + "</b><br>"
                        + "<font color='#BE844C'><b>" + CURRENCY.format(product.getPrice()) + "</b></font> &nbsp;·"
                        + stockWarning + "</html>");
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(243, 239, 233)),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)
                ));
            }
            return label;
        }
    }

    private static final class InvoiceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setForeground(ESPRESSO);
            label.setBackground(isSelected ? SELECTED : (row % 2 == 0 ? Color.WHITE : new Color(252, 250, 247)));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(243, 239, 233)),
                    BorderFactory.createEmptyBorder(0, column == 1 ? 10 : 6, 0, 8)
            ));

            if (column == 0 || column == 2) {
                label.setHorizontalAlignment(JLabel.CENTER);
            } else if (column >= 3) {
                label.setHorizontalAlignment(JLabel.RIGHT);
            } else {
                label.setHorizontalAlignment(JLabel.LEFT);
            }
            return label;
        }
    }

    private static final class TableCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            if (value instanceof CafeTable table) {
                boolean isAvail = table.getStatus() == TableStatus.AVAILABLE;
                label.setText("Bàn " + table.getTableNumber() + " · " + (isAvail ? "🟢 Trống" : "🟠 Đang dùng") + " (" + table.getCapacity() + " chỗ)");
                if (!isSelected) {
                    label.setForeground(isAvail ? GREEN_TEXT : ORANGE_TEXT);
                }
            }
            return label;
        }
    }

    private static final class EmployeeCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            if (value instanceof Employee employee) {
                label.setText(employee.getName() + " (" + (employee.getPosition() != null ? employee.getPosition() : "Nhân viên") + ")");
            } else if (value == null && index >= 0) {
                label.setText("Chưa có nhân viên");
            }
            return label;
        }
    }

    private static final class CardPanel extends JPanel {
        private CardPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(71, 48, 33, 12));
            g2.fillRoundRect(2, 3, getWidth() - 5, getHeight() - 5, 16, 16);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 6, 16, 16);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 6, 16, 16);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class GradientHeader extends JPanel {
        private GradientHeader() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new java.awt.GradientPaint(0, 0, ESPRESSO, getWidth(), getHeight(), COFFEE));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fillOval(getWidth() - 220, -110, 320, 320);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private record ReferenceData(
            List<CafeTable> tables,
            List<Employee> employees,
            List<Product> products
    ) {
    }
}
