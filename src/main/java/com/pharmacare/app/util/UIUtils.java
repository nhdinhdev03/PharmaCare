package com.pharmacare.app.util;

import javax.swing.*;

public class UIUtils {
  public static void info(java.awt.Component p, String msg) {
    JOptionPane.showMessageDialog(p, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
  }
  public static void error(java.awt.Component p, Throwable ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
  }
}
