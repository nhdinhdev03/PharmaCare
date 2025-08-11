package com.pharmacare.app;

import com.formdev.flatlaf.FlatLightLaf;      // <-- import thư viện thật
import com.pharmacare.app.ui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    // Khởi tạo Look & Feel FlatLaf
    FlatLightLaf.setup();

    // Mở UI
    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
  }
}
