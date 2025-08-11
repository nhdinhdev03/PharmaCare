package com.pharmacare.app.service;

import com.pharmacare.app.dao.ProductDAO;
import com.pharmacare.app.model.Product;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
  private final ProductDAO dao = new ProductDAO();

  public List<Product> listAll() throws SQLException { return dao.findAll(); }
  public int add(Product p) throws SQLException { validate(p); return dao.insert(p); }
  public void update(Product p) throws SQLException {
    if (p.getProductId()==null) throw new IllegalArgumentException("Missing product id");
    validate(p); dao.update(p);
  }
  public void delete(int id) throws SQLException { dao.delete(id); }

  private void validate(Product p) {
    if (p.getName()==null || p.getName().isBlank()) throw new IllegalArgumentException("Name is required");
    if (p.getUnitPrice() < 0) throw new IllegalArgumentException("Price must be >= 0");
    if (p.getStock() < 0) throw new IllegalArgumentException("Stock must be >= 0");
  }
}
