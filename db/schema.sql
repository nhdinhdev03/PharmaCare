CREATE DATABASES PharmaCare;

USE PharmaCare; 
--1. Roles – Phân quyền



CREATE TABLE Roles (
    RoleId INT IDENTITY PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL UNIQUE
);
--2. Users – Nhân viên



CREATE TABLE Users (
    UserId INT IDENTITY PRIMARY KEY,
    FullName NVARCHAR(100) NOT NULL,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    PasswordHash VARBINARY(64) NOT NULL,
    Salt VARBINARY(16) NOT NULL,
    RoleId INT NOT NULL REFERENCES Roles(RoleId),
    IsActive BIT NOT NULL DEFAULT 1
);
--3. Categories – Nhóm thuốc



CREATE TABLE Categories (
    CategoryId INT IDENTITY PRIMARY KEY,
    CategoryName NVARCHAR(100) NOT NULL UNIQUE
);
--4. Suppliers – Nhà cung cấp



CREATE TABLE Suppliers (
    SupplierId INT IDENTITY PRIMARY KEY,
    SupplierName NVARCHAR(150) NOT NULL,
    Phone NVARCHAR(20),
    Address NVARCHAR(255)
);
--5. Medicines – Thuốc



CREATE TABLE Medicines (
    MedicineId INT IDENTITY PRIMARY KEY,
    MedicineName NVARCHAR(200) NOT NULL,
    CategoryId INT REFERENCES Categories(CategoryId),
    SupplierId INT REFERENCES Suppliers(SupplierId),
    Unit NVARCHAR(50) NOT NULL, -- Hộp, vỉ, viên
    UnitPrice DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
    Stock INT NOT NULL DEFAULT 0 CHECK (Stock >= 0),
    ExpiryDate DATE NOT NULL,
    IsPrescriptionRequired BIT NOT NULL DEFAULT 0, -- Có cần đơn thuốc không
    IsActive BIT NOT NULL DEFAULT 1
);
--6. Customers – Khách hàng



CREATE TABLE Customers (
    CustomerId INT IDENTITY PRIMARY KEY,
    FullName NVARCHAR(150) NOT NULL,
    Phone NVARCHAR(20),
    Address NVARCHAR(255)
);
--7. Invoices – Hóa đơn bán



CREATE TABLE Invoices (
    InvoiceId INT IDENTITY PRIMARY KEY,
    CustomerId INT REFERENCES Customers(CustomerId),
    UserId INT NOT NULL REFERENCES Users(UserId), -- Nhân viên bán
    InvoiceDate DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    TotalAmount DECIMAL(18,2) NOT NULL DEFAULT 0
);
--8. InvoiceDetails – Chi tiết hóa đơn



CREATE TABLE InvoiceDetails (
    InvoiceDetailId INT IDENTITY PRIMARY KEY,
    InvoiceId INT NOT NULL REFERENCES Invoices(InvoiceId) ON DELETE CASCADE,
    MedicineId INT NOT NULL REFERENCES Medicines(MedicineId),
    Quantity INT NOT NULL CHECK (Quantity > 0),
    UnitPrice DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
    LineTotal AS (Quantity * UnitPrice) PERSISTED
);
--9. PurchaseOrders – Phiếu nhập hàng



CREATE TABLE PurchaseOrders (
    PurchaseOrderId INT IDENTITY PRIMARY KEY,
    SupplierId INT NOT NULL REFERENCES Suppliers(SupplierId),
    UserId INT NOT NULL REFERENCES Users(UserId), -- Nhân viên nhập hàng
    OrderDate DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    TotalAmount DECIMAL(18,2) NOT NULL DEFAULT 0
);
--10. PurchaseDetails – Chi tiết nhập hàng



CREATE TABLE PurchaseDetails (
    PurchaseDetailId INT IDENTITY PRIMARY KEY,
    PurchaseOrderId INT NOT NULL REFERENCES PurchaseOrders(PurchaseOrderId) ON DELETE CASCADE,
    MedicineId INT NOT NULL REFERENCES Medicines(MedicineId),
    Quantity INT NOT NULL CHECK (Quantity > 0),
    UnitPrice DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
    LineTotal AS (Quantity * UnitPrice) PERSISTED
);



IF DB_ID('PharmaCare') IS NULL CREATE DATABASE PharmaCare;
GO
USE PharmaCare;
GO
IF OBJECT_ID('dbo.Products','U') IS NULL
BEGIN
  CREATE TABLE dbo.Products (
    ProductId INT IDENTITY PRIMARY KEY,
    Name NVARCHAR(200) NOT NULL,
    UnitPrice DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
    Stock INT NOT NULL DEFAULT 0 CHECK (Stock >= 0),
    IsActive BIT NOT NULL DEFAULT 1
  );
  INSERT INTO dbo.Products(Name, UnitPrice, Stock, IsActive)
  VALUES (N'Paracetamol 500mg', 12000, 100, 1),
         (N'Vitamin C 1000', 25000, 60, 1);
END
GO
