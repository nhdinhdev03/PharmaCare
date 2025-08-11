package com.pharmacare.app.ui;

import com.pharmacare.app.model.Product;
import com.pharmacare.app.service.ProductService;
import com.pharmacare.app.util.UIUtils;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ProductPanel extends javax.swing.JPanel {

    private final ProductService service = new ProductService();
    private final DefaultTableModel model = new DefaultTableModel(
        new Object[]{"ID","Name","Price","Stock","Active"}, 0) {
        @Override public boolean isCellEditable(int r, int c){ return false; }
        @Override public Class<?> getColumnClass(int c){
            return switch (c) {
                case 0 -> Integer.class;
                case 2 -> Double.class;
                case 3 -> Integer.class;
                case 4 -> Boolean.class;
                default -> String.class;
            };
        }
    };
    private TableRowSorter<DefaultTableModel> sorter;

    public ProductPanel() {
        initComponents();
        table.setModel(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        spPrice.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 100.0d));
        spStock.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        cbActive.setSelected(true);

        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnRefresh.addActionListener(e -> loadData());
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){ applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ applyFilter(); }
        });

        loadData();
    }

    private void applyFilter(){
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(kw), 1));
    }

    private void loadData() {
        btnRefresh.setEnabled(false);
        new SwingWorker<List<Product>, Void>() {
            @Override protected List<Product> doInBackground() throws Exception {
                return service.listAll();
            }
            @Override protected void done() {
                try {
                    List<Product> list = get();
                    model.setRowCount(0);
                    for (Product p : list) {
                        model.addRow(new Object[]{p.getProductId(), p.getName(), p.getUnitPrice(), p.getStock(), p.isActive()});
                    }
                } catch (Exception ex) { UIUtils.error(ProductPanel.this, ex); }
                finally { btnRefresh.setEnabled(true); }
            }
        }.execute();
    }

    private void addProduct() {
        try {
            Product p = new Product();
            p.setName(tfName.getText().trim());
            p.setUnitPrice(((Number)spPrice.getValue()).doubleValue());
            p.setStock(((Number)spStock.getValue()).intValue());
            p.setActive(cbActive.isSelected());
            service.add(p);
            loadData();
            UIUtils.info(this, "Inserted!");
        } catch (Exception ex) { UIUtils.error(this, ex); }
    }

    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.info(this, "Select a row first."); return; }
        try {
            int modelRow = table.convertRowIndexToModel(row);
            Integer id = (Integer) model.getValueAt(modelRow, 0);
            Product p = new Product();
            p.setProductId(id);
            p.setName(tfName.getText().trim());
            p.setUnitPrice(((Number)spPrice.getValue()).doubleValue());
            p.setStock(((Number)spStock.getValue()).intValue());
            p.setActive(cbActive.isSelected());
            service.update(p);
            loadData();
            UIUtils.info(this, "Updated.");
        } catch (Exception ex) { UIUtils.error(this, ex); }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.info(this, "Select a row first."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(modelRow, 0);
        if (javax.swing.JOptionPane.showConfirmDialog(this, "Delete product #"+id+"?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION)
                == javax.swing.JOptionPane.YES_OPTION) {
            try { service.delete(id); loadData(); }
            catch (Exception ex) { UIUtils.error(this, ex); }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        southPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        spPrice = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spStock = new javax.swing.JSpinner();
        cbActive = new javax.swing.JCheckBox();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        table.setAutoCreateRowSorter(true);
        jScrollPane1.setViewportView(table);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel4.setText("Search");

        jLabel1.setText("Name");

        jLabel2.setText("Price");

        jLabel3.setText("Stock");

        cbActive.setText("Active");

        btnAdd.setText("Add");

        btnUpdate.setText("Update");

        btnDelete.setText("Delete");

        btnRefresh.setText("Refresh");

        javax.swing.GroupLayout southPanelLayout = new javax.swing.GroupLayout(southPanel);
        southPanel.setLayout(southPanelLayout);
        southPanelLayout.setHorizontalGroup(
            southPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(southPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spStock, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbActive)
                .addGap(18, 18, 18)
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        southPanelLayout.setVerticalGroup(
            southPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(southPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(southPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(spPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(spStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbActive)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(southPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox cbActive;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel southPanel;
    private javax.swing.JSpinner spPrice;
    private javax.swing.JSpinner spStock;
    private javax.swing.JTable table;
    private javax.swing.JTextField tfName;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables
}
