package com.pharmacare.app.util;

import java.sql.*;
import java.io.InputStream;
import java.util.Properties;

public class DB {
  private static String URL;
  private static String USER;
  private static String PASS;

  static {
    try {
      Properties p = new Properties();
      try (InputStream in = DB.class.getClassLoader().getResourceAsStream("application.properties")) {
        if (in != null) p.load(in);
      }
      URL = p.getProperty("db.url");
      USER = p.getProperty("db.user");
      PASS = p.getProperty("db.password");
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    } catch (Exception ex) {
      throw new RuntimeException("Failed to init DB config", ex);
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASS);
  }
}
