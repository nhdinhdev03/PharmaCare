package com.pharmacare.app.dao;

import com.pharmacare.app.model.Product;
import com.pharmacare.app.util.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
  public List<Product> findAll() throws SQLException {
    String sql = "SELECT ProductId, Name, UnitPrice, Stock, IsActive FROM dbo.Products ORDER BY ProductId DESC";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<Product> list = new ArrayList<>();
      while (rs.next()) {
        Product p = new Product();
        p.setProductId(rs.getInt("ProductId"));
        p.setName(rs.getString("Name"));
        p.setUnitPrice(rs.getDouble("UnitPrice"));
        p.setStock(rs.getInt("Stock"));
        p.setActive(rs.getBoolean("IsActive"));
        list.add(p);
      }
      return list;
    }
  }

  public int insert(Product p) throws SQLException {
    String sql = "INSERT INTO dbo.Products(Name, UnitPrice, Stock, IsActive) VALUES(?, ?, ?, ?)";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, p.getName());
      ps.setDouble(2, p.getUnitPrice());
      ps.setInt(3, p.getStock());
      ps.setBoolean(4, p.isActive());
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) {
        return k.next() ? k.getInt(1) : -1;
      }
    }
  }

  public void update(Product p) throws SQLException {
    String sql = "UPDATE dbo.Products SET Name=?, UnitPrice=?, Stock=?, IsActive=? WHERE ProductId=?";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, p.getName());
      ps.setDouble(2, p.getUnitPrice());
      ps.setInt(3, p.getStock());
      ps.setBoolean(4, p.isActive());
      ps.setInt(5, p.getProductId());
      ps.executeUpdate();
    }
  }

  public void delete(int id) throws SQLException {
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.Products WHERE ProductId=?")) {
      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }
}
