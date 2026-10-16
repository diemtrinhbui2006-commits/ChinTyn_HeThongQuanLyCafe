package com.example.BuiThiDiemTrinh_TH5.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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

@Component
@ConditionalOnProperty(name = "app.gui.enabled", havingValue = "true")
public class CafeDesktopWindow {

    private static final Logger LOGGER = Logger.getLogger(CafeDesktopWindow.class.getName());
    private static final Color COFFEE = new Color(91, 58, 41);
    private static final Color ESPRESSO = new Color(54, 37, 30);
    private static final Color CARAMEL = new Color(190, 132, 76);
    private static final Color CREAM = new Color(250, 247, 241);
    private static final Color SOFT_CREAM = new Color(246, 241, 233);
    private static final Color BORDER = new Color(231, 224, 215);
    private static final Color SELECTED = new Color(250, 241, 228);
    private static final Color MUTED = new Color(105, 105, 105);
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    private final CafeTableRepository tableRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final InvoiceGuiBinding invoiceGuiBinding;
    private final DefaultListModel<Product> productListModel = new DefaultListModel<>();
    private final DefaultTableModel invoiceTableModel = new DefaultTableModel(
            new Object[]{"Món", "SL", "Đơn giá", "Thành tiền"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JFrame frame;
    private JButton refreshButton;
    private JComboBox<CafeTable> tableSelector;
    private JComboBox<Employee> employeeSelector;
    private JComboBox<String> paymentSelector;
    private JTextField productSearch;
    private JTextField discountCode;
    private JSpinner quantitySpinner;
    private JList<Product> productList;
    private JTable invoiceTable;
    private JLabel invoiceStatus;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private JLabel footerStatus;
    private List<Product> products = List.of();
    private InvoiceResponse currentInvoice;
    private boolean updatingTableSelector;
    private boolean refreshInProgress;

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
        frame = new JFrame("ChinTyn Cafe | Quản lý gọi món");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 650));
        frame.setSize(1280, 820);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(CREAM);
        frame.setLayout(new BorderLayout(0, 0));
        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(createWorkspace(), BorderLayout.CENTER);
        frame.add(createFooter(), BorderLayout.SOUTH);

        try {
            refreshReferenceData(null);
        } catch (RuntimeException exception) {
            showError(exception);
        }

        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new GradientHeader();
        header.setLayout(new BorderLayout(16, 0));
        header.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));

        JLabel brandMark = new JLabel("C");
        brandMark.setHorizontalAlignment(JLabel.CENTER);
        brandMark.setOpaque(true);
        brandMark.setBackground(CARAMEL);
        brandMark.setForeground(Color.WHITE);
        brandMark.setFont(new Font(Font.SERIF, Font.BOLD, 24));
        brandMark.setPreferredSize(new Dimension(48, 48));
        brandMark.setBorder(BorderFactory.createLineBorder(new Color(225, 184, 135)));

        JLabel title = new JLabel("CHINTYN CAFE");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        JLabel subtitle = new JLabel("QUẢN LÝ GỌI MÓN  ·  THANH TOÁN");
        subtitle.setForeground(new Color(225, 211, 195));
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

        refreshButton = new JButton("Làm mới dữ liệu");
        secondaryButton(refreshButton);
        refreshButton.addActionListener(event -> refreshReferenceDataInBackground(selectedTableId()));
        header.add(titles, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(CREAM);
        workspace.setBorder(BorderFactory.createEmptyBorder(22, 24, 10, 24));

        GridBagConstraints menuConstraints = constraints(0, 0);
        menuConstraints.weightx = 0.46;
        menuConstraints.weighty = 1;
        menuConstraints.fill = GridBagConstraints.BOTH;
        menuConstraints.insets = new Insets(0, 0, 0, 8);
        workspace.add(createMenuPanel(), menuConstraints);

        GridBagConstraints orderConstraints = constraints(1, 0);
        orderConstraints.weightx = 0.54;
        orderConstraints.weighty = 1;
        orderConstraints.fill = GridBagConstraints.BOTH;
        orderConstraints.insets = new Insets(0, 8, 0, 0);
        workspace.add(createOrderPanel(), orderConstraints);
        return workspace;
    }

    private JPanel createMenuPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 12));

        productSearch = new JTextField();
        productSearch.setToolTipText("Nhập tên món để tìm");
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

        JPanel searchPanel = new JPanel(new BorderLayout(0, 8));
        searchPanel.setOpaque(false);
        searchPanel.add(sectionTitle("Thực đơn"), BorderLayout.NORTH);
        searchPanel.add(productSearch, BorderLayout.CENTER);

        productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setCellRenderer(new ProductCellRenderer());
        productList.setBackground(Color.WHITE);
        productList.setFixedCellHeight(68);
        productList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setOpaque(false);
        addPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        styleInput(quantitySpinner);
        JButton addButton = primaryButton("Thêm món vào bàn");
        addButton.addActionListener(event -> runAction(this::addSelectedProduct));
        GridBagConstraints quantityLabelConstraints = constraints(0, 0);
        quantityLabelConstraints.weightx = 1;
        quantityLabelConstraints.anchor = GridBagConstraints.WEST;
        addPanel.add(new JLabel("Số lượng"), quantityLabelConstraints);
        GridBagConstraints quantityInputConstraints = constraints(1, 0);
        quantityInputConstraints.insets = new Insets(0, 8, 0, 8);
        addPanel.add(quantitySpinner, quantityInputConstraints);
        addPanel.add(addButton, constraints(2, 0));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(styledScrollPane(productList), BorderLayout.CENTER);
        panel.add(addPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createOrderPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 12));

        JPanel orderHeader = new JPanel(new GridBagLayout());
        orderHeader.setOpaque(false);
        GridBagConstraints tableLabelConstraints = constraints(0, 0);
        tableLabelConstraints.anchor = GridBagConstraints.WEST;
        orderHeader.add(new JLabel("Bàn"), tableLabelConstraints);
        tableSelector = new JComboBox<>();
        tableSelector.setRenderer(new TableCellRenderer());
        styleInput(tableSelector);
        tableSelector.addActionListener(event -> {
            if (!updatingTableSelector) {
                loadSelectedTableInvoice();
            }
        });
        GridBagConstraints tableSelectorConstraints = constraints(1, 0);
        tableSelectorConstraints.weightx = 1;
        tableSelectorConstraints.fill = GridBagConstraints.HORIZONTAL;
        tableSelectorConstraints.insets = new Insets(0, 8, 0, 0);
        orderHeader.add(tableSelector, tableSelectorConstraints);

        GridBagConstraints employeeLabelConstraints = constraints(0, 1);
        employeeLabelConstraints.anchor = GridBagConstraints.WEST;
        employeeLabelConstraints.insets = new Insets(10, 0, 0, 0);
        orderHeader.add(new JLabel("Nhân viên"), employeeLabelConstraints);
        employeeSelector = new JComboBox<>();
        employeeSelector.setRenderer(new EmployeeCellRenderer());
        styleInput(employeeSelector);
        GridBagConstraints employeeSelectorConstraints = constraints(1, 1);
        employeeSelectorConstraints.fill = GridBagConstraints.HORIZONTAL;
        employeeSelectorConstraints.insets = new Insets(10, 8, 0, 0);
        orderHeader.add(employeeSelector, employeeSelectorConstraints);

        invoiceStatus = new JLabel("Chọn bàn để xem hóa đơn");
        invoiceStatus.setForeground(COFFEE);
        invoiceStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        invoiceStatus.setOpaque(true);
        invoiceStatus.setBackground(SOFT_CREAM);
        invoiceStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 11, 8, 11)
        ));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(sectionTitle("Hóa đơn hiện tại"), BorderLayout.NORTH);
        titlePanel.add(orderHeader, BorderLayout.CENTER);
        titlePanel.add(invoiceStatus, BorderLayout.SOUTH);

        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(38);
        invoiceTable.setFillsViewportHeight(true);
        invoiceTable.setShowGrid(false);
        invoiceTable.setIntercellSpacing(new Dimension(0, 0));
        invoiceTable.setSelectionBackground(SELECTED);
        invoiceTable.setSelectionForeground(ESPRESSO);
        invoiceTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        invoiceTable.setForeground(ESPRESSO);
        invoiceTable.getTableHeader().setReorderingAllowed(false);
        invoiceTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
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
        discountLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        totalLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        totalLabel.setForeground(CARAMEL);
        addSummaryRow(panel, 0, "Tạm tính", subtotalLabel);
        addSummaryRow(panel, 1, "Giảm giá", discountLabel);
        addSummaryRow(panel, 2, "Khách cần trả", totalLabel);

        discountCode = new JTextField();
        discountCode.setToolTipText("Mã giảm giá (không bắt buộc)");
        styleInput(discountCode);
        paymentSelector = new JComboBox<>(new String[]{
                "Tiền mặt", "Chuyển khoản", "Thẻ", "Ví điện tử"
        });
        styleInput(paymentSelector);
        JPanel checkoutFields = new JPanel(new GridBagLayout());
        checkoutFields.setOpaque(false);
        GridBagConstraints codeConstraints = constraints(0, 0);
        codeConstraints.weightx = 1;
        codeConstraints.fill = GridBagConstraints.HORIZONTAL;
        checkoutFields.add(discountCode, codeConstraints);
        GridBagConstraints paymentConstraints = constraints(1, 0);
        paymentConstraints.weightx = 1;
        paymentConstraints.fill = GridBagConstraints.HORIZONTAL;
        paymentConstraints.insets = new Insets(0, 8, 0, 0);
        checkoutFields.add(paymentSelector, paymentConstraints);

        JButton checkoutButton = primaryButton("Thanh toán");
        checkoutButton.addActionListener(event -> runAction(this::checkout));
        GridBagConstraints fieldsConstraints = constraints(0, 3);
        fieldsConstraints.gridwidth = 2;
        fieldsConstraints.weightx = 1;
        fieldsConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldsConstraints.insets = new Insets(12, 0, 8, 0);
        panel.add(checkoutFields, fieldsConstraints);
        GridBagConstraints buttonConstraints = constraints(0, 4);
        buttonConstraints.gridwidth = 2;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(checkoutButton, buttonConstraints);
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 28, 14, 28));
        footerStatus = new JLabel("Sẵn sàng");
        footerStatus.setForeground(MUTED);
        footerStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        panel.add(footerStatus, BorderLayout.WEST);
        return panel;
    }

    private void refreshReferenceData(Long tableIdToSelect) {
        ReferenceData data = fetchReferenceData();
        applyReferenceData(data, tableIdToSelect);
        footerStatus.setText("Đã tải " + data.tables().size() + " bàn, " + data.products().size()
                + " món và " + data.employees().size() + " nhân viên");
        loadSelectedTableInvoice();
    }

    private ReferenceData fetchReferenceData() {
        List<CafeTable> tables = tableRepository.findAll();
        List<Employee> employees = employeeRepository.findAll();
        List<Product> availableProducts = productRepository.findAll().stream()
                .filter(product -> product.isAvailable() && product.getQuantity() != null && product.getQuantity() > 0)
                .toList();
        return new ReferenceData(tables, employees, availableProducts);
    }

    private void applyReferenceData(ReferenceData data, Long tableIdToSelect) {
        Employee previouslySelectedEmployee = (Employee) employeeSelector.getSelectedItem();
        Long employeeIdToSelect = previouslySelectedEmployee == null ? null : previouslySelectedEmployee.getId();
        products = data.products();
        updatingTableSelector = true;
        try {
            tableSelector.removeAllItems();
            for (CafeTable table : data.tables()) {
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
            for (Employee employee : data.employees()) {
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

        filterProducts();
    }

    private void refreshReferenceDataInBackground(Long tableIdToSelect) {
        if (refreshInProgress) {
            return;
        }
        refreshInProgress = true;
        refreshButton.setEnabled(false);
        refreshButton.setText("Đang tải...");
        footerStatus.setText("Đang tải danh sách bàn, món và nhân viên...");

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
                    footerStatus.setText("Đã tải " + data.tables().size() + " bàn, " + data.products().size()
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
                    refreshButton.setText("Làm mới dữ liệu");
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
            footerStatus.setText("Đang xem hóa đơn mở của bàn " + table.getTableNumber());
        } catch (ResourceNotFoundException exception) {
            currentInvoice = null;
            renderInvoice(null);
            invoiceStatus.setText("Bàn chưa có hóa đơn mở");
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
            showMessage("Cần có nhân viên phục vụ khi mở hóa đơn mới.", "Chưa chọn nhân viên");
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
                + " vào hóa đơn #" + invoice.id());
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
        showMessage("Thanh toán thành công. Tổng tiền: " + formatCurrency(paidInvoice.totalAmount()),
                "Thanh toán hoàn tất");
    }

    private void renderInvoice(InvoiceResponse invoice) {
        invoiceTableModel.setRowCount(0);
        if (invoice == null) {
            invoiceStatus.setText("Chọn bàn để xem hóa đơn");
            updateTotals(null);
            return;
        }

        for (InvoiceDetailResponse detail : invoice.details()) {
            invoiceTableModel.addRow(new Object[]{
                    detail.productName(),
                    detail.quantity(),
                    formatCurrency(detail.unitPrice()),
                    formatCurrency(detail.lineTotal())
            });
        }
        String status = invoice.status() == InvoiceStatus.PAID ? "Đã thanh toán" : "Đang mở";
        invoiceStatus.setText("Hóa đơn #" + invoice.id() + "  ·  " + status);
        updateTotals(invoice);
    }

    private void updateTotals(InvoiceResponse invoice) {
        subtotalLabel.setText(invoice == null ? formatCurrency(BigDecimal.ZERO) : formatCurrency(invoice.subtotal()));
        discountLabel.setText(invoice == null ? formatCurrency(BigDecimal.ZERO) : formatCurrency(invoice.discount()));
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
        LOGGER.log(Level.SEVERE, "Không thể xử lý thao tác giao diện hóa đơn", exception);
        showMessage(message, "Không thể hoàn tất thao tác");
        footerStatus.setText("Thao tác thất bại: " + message);
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createCard() {
        return new CardPanel();
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        label.setForeground(COFFEE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return label;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(COFFEE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COFFEE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        return button;
    }

    private void secondaryButton(JButton button) {
        button.setBackground(new Color(255, 255, 255, 20));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(208, 187, 166)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
    }

    private void styleInput(JComponent component) {
        component.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
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
            spinner.setPreferredSize(new Dimension(76, 38));
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
        captionConstraints.insets = new Insets(8, 0, 0, 0);
        panel.add(new JLabel(caption), captionConstraints);
        GridBagConstraints valueConstraints = constraints(1, row);
        valueConstraints.anchor = GridBagConstraints.EAST;
        valueConstraints.weightx = 1;
        valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        valueConstraints.insets = new Insets(8, 8, 0, 0);
        panel.add(value, valueConstraints);
    }

    private GridBagConstraints constraints(int gridx, int gridy) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.insets = new Insets(2, 2, 2, 2);
        return constraints;
    }

    private String formatCurrency(BigDecimal amount) {
        return CURRENCY.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static final class ProductCellRenderer extends DefaultListCellRenderer {

        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
            );
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            label.setBackground(isSelected ? SELECTED : Color.WHITE);
            label.setForeground(ESPRESSO);
            if (value instanceof Product product) {
                String safeName = product.getName()
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
                label.setText("<html><b>" + safeName + "</b><br>"
                        + CURRENCY.format(product.getPrice()) + " &nbsp; · &nbsp; Còn "
                        + product.getQuantity() + "</html>");
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(243, 239, 233)),
                        BorderFactory.createEmptyBorder(9, 12, 9, 12)
                ));
            }
            return label;
        }
    }

    private static final class InvoiceCellRenderer extends DefaultTableCellRenderer {

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );
            label.setOpaque(true);
            label.setForeground(ESPRESSO);
            label.setBackground(isSelected ? SELECTED : (row % 2 == 0 ? Color.WHITE : new Color(252, 250, 247)));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(243, 239, 233)),
                    BorderFactory.createEmptyBorder(0, column == 0 ? 14 : 8, 0, 10)
            ));
            label.setHorizontalAlignment(column >= 2 ? JLabel.RIGHT : JLabel.LEFT);
            return label;
        }
    }

    private static final class TableCellRenderer extends DefaultListCellRenderer {

        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
            );
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if (value instanceof CafeTable table) {
                label.setText("Bàn " + table.getTableNumber() + " · " + tableStatusLabel(table.getStatus()));
                if (!isSelected) {
                    label.setForeground(table.getStatus() == TableStatus.AVAILABLE ? new Color(48, 125, 87) : ESPRESSO);
                }
            }
            return label;
        }

        private static String tableStatusLabel(TableStatus status) {
            return switch (status) {
                case AVAILABLE -> "Trống";
                case OCCUPIED -> "Đang dùng";
                case RESERVED -> "Đã đặt";
                case OUT_OF_SERVICE -> "Ngừng phục vụ";
            };
        }
    }

    private static final class EmployeeCellRenderer extends DefaultListCellRenderer {

        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
            );
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if (value instanceof Employee employee) {
                label.setText(employee.getName());
            } else if (value == null && index >= 0) {
                label.setText("Chưa có nhân viên");
            }
            return label;
        }
    }

    private static final class CardPanel extends JPanel {

        private CardPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(71, 48, 33, 16));
            g2.fillRoundRect(2, 4, getWidth() - 5, getHeight() - 6, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 7, 18, 18);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 7, 18, 18);
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
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval(getWidth() - 190, -120, 300, 300);
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
