package com.pharmacare.app.ui;

import com.pharmacare.app.dao.ProductDAO;
import com.pharmacare.app.model.Product;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ProductPanel extends javax.swing.JPanel {

    private final ProductDAO dao = new ProductDAO();
    private final DefaultTableModel model = new DefaultTableModel(
        new Object[]{"ID","Name","Price","Stock","Active"}, 0) {
        @Override public boolean isCellEditable(int r, int c){ return false; }
    };

    public ProductPanel() {
        initComponents();
        table.setModel(model);
        refresh();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
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

        jScrollPane1.setViewportView(table);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Name");
        jLabel2.setText("Price");
        spPrice.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));
        jLabel3.setText("Stock");
        spStock.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        cbActive.setText("Active");

        btnAdd.setText("Add");
        btnUpdate.setText("Update");
        btnDelete.setText("Delete");
        btnRefresh.setText("Refresh");

        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnRefresh.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelection());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(spPrice)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(spStock, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cbActive)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)))
                .addContainerGap(220, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(spStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbActive))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Product> list = dao.findAll();
                model.setRowCount(0);
                for (Product p : list) {
                    model.addRow(new Object[]{p.getProductId(), p.getName(), p.getUnitPrice(), p.getStock(), p.isActive()});
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        });
    }

    private void addProduct() {
        try {
            Product p = new Product();
            p.setName(tfName.getText().trim());
            p.setUnitPrice(((Number)spPrice.getValue()).doubleValue());
            p.setStock(((Number)spStock.getValue()).intValue());
            p.setActive(cbActive.isSelected());
            dao.insert(p);
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first"); return; }
        try {
            Product p = new Product();
            p.setProductId((Integer) model.getValueAt(row, 0));
            p.setName(tfName.getText().trim());
            p.setUnitPrice(((Number)spPrice.getValue()).doubleValue());
            p.setStock(((Number)spStock.getValue()).intValue());
            p.setActive(cbActive.isSelected());
            dao.update(p);
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first"); return; }
        int id = (Integer) model.getValueAt(row, 0);
        if (javax.swing.JOptionPane.showConfirmDialog(this, "Delete product #" + id + "?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION)==javax.swing.JOptionPane.YES_OPTION) {
            try { dao.delete(id); refresh(); } catch (Exception ex) { showError(ex); }
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            tfName.setText(String.valueOf(model.getValueAt(row, 1)));
            spPrice.setValue(((Number)model.getValueAt(row, 2)).doubleValue());
            spStock.setValue(((Number)model.getValueAt(row, 3)).intValue());
            cbActive.setSelected((Boolean)model.getValueAt(row, 4));
        }
    }

    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Variables declaration - do not modify
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox cbActive;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner spPrice;
    private javax.swing.JSpinner spStock;
    private javax.swing.JTable table;
    private javax.swing.JTextField tfName;
    // End of variables declaration
}
