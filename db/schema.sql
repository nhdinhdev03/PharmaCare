/* ===== 0) DB ===== */
IF DB_ID('PharmaCare') IS NULL CREATE DATABASE PharmaCare;
GO
USE PharmaCare;
GO

/* ===== 1) Bảng & Ràng buộc ===== */

IF OBJECT_ID('dbo.InvoiceDetails','U') IS NOT NULL DROP TABLE dbo.InvoiceDetails;
IF OBJECT_ID('dbo.PurchaseDetails','U') IS NOT NULL DROP TABLE dbo.PurchaseDetails;
IF OBJECT_ID('dbo.Invoices','U') IS NOT NULL DROP TABLE dbo.Invoices;
IF OBJECT_ID('dbo.PurchaseOrders','U') IS NOT NULL DROP TABLE dbo.PurchaseOrders;
IF OBJECT_ID('dbo.Medicines','U') IS NOT NULL DROP TABLE dbo.Medicines;
IF OBJECT_ID('dbo.Customers','U') IS NOT NULL DROP TABLE dbo.Customers;
IF OBJECT_ID('dbo.Suppliers','U') IS NOT NULL DROP TABLE dbo.Suppliers;
IF OBJECT_ID('dbo.Categories','U') IS NOT NULL DROP TABLE dbo.Categories;
IF OBJECT_ID('dbo.Users','U') IS NOT NULL DROP TABLE dbo.Users;
IF OBJECT_ID('dbo.Roles','U') IS NOT NULL DROP TABLE dbo.Roles;
GO

-- Roles
CREATE TABLE dbo.Roles (
  RoleId   INT IDENTITY PRIMARY KEY,
  RoleName NVARCHAR(50) NOT NULL UNIQUE
);

-- Users (hash + salt)
CREATE TABLE dbo.Users (
  UserId   INT IDENTITY PRIMARY KEY,
  FullName NVARCHAR(100) NOT NULL,
  Username NVARCHAR(50)  NOT NULL UNIQUE,
  PasswordHash VARBINARY(64) NOT NULL,
  Salt         VARBINARY(16) NOT NULL,
  RoleId   INT NOT NULL FOREIGN KEY REFERENCES dbo.Roles(RoleId),
  IsActive BIT NOT NULL DEFAULT 1
);

-- Categories
CREATE TABLE dbo.Categories (
  CategoryId   INT IDENTITY PRIMARY KEY,
  CategoryName NVARCHAR(100) NOT NULL UNIQUE
);

-- Suppliers
CREATE TABLE dbo.Suppliers (
  SupplierId   INT IDENTITY PRIMARY KEY,
  SupplierName NVARCHAR(150) NOT NULL,
  Phone        NVARCHAR(20),
  Address      NVARCHAR(255)
);

-- Medicines
CREATE TABLE dbo.Medicines (
  MedicineId   INT IDENTITY PRIMARY KEY,
  MedicineName NVARCHAR(200) NOT NULL,
  CategoryId   INT NULL FOREIGN KEY REFERENCES dbo.Categories(CategoryId),
  SupplierId   INT NULL FOREIGN KEY REFERENCES dbo.Suppliers(SupplierId),
  Unit         NVARCHAR(50)  NOT NULL,            -- Hộp, vỉ, viên...
  UnitPrice    DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
  Stock        INT           NOT NULL DEFAULT 0 CHECK (Stock >= 0),
  ExpiryDate   DATE          NOT NULL,
  IsPrescriptionRequired BIT NOT NULL DEFAULT 0,
  IsActive     BIT           NOT NULL DEFAULT 1,
  CONSTRAINT UQ_Medicine UNIQUE (MedicineName, Unit, SupplierId) -- hạn chế trùng
);

-- Customers
CREATE TABLE dbo.Customers (
  CustomerId INT IDENTITY PRIMARY KEY,
  FullName   NVARCHAR(150) NOT NULL,
  Phone      NVARCHAR(20),
  Address    NVARCHAR(255)
);

-- Invoices (bổ sung Subtotal/Discount/Tax)
CREATE TABLE dbo.Invoices (
  InvoiceId    INT IDENTITY PRIMARY KEY,
  CustomerId   INT NULL FOREIGN KEY REFERENCES dbo.Customers(CustomerId),
  UserId       INT NOT NULL FOREIGN KEY REFERENCES dbo.Users(UserId),
  InvoiceDate  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
  Subtotal     DECIMAL(18,2) NOT NULL DEFAULT 0,
  Discount     DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (Discount >= 0),
  Tax          DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (Tax >= 0),
  TotalAmount  DECIMAL(18,2) NOT NULL DEFAULT 0
);

-- InvoiceDetails
CREATE TABLE dbo.InvoiceDetails (
  InvoiceDetailId INT IDENTITY PRIMARY KEY,
  InvoiceId  INT NOT NULL FOREIGN KEY REFERENCES dbo.Invoices(InvoiceId) ON DELETE CASCADE,
  MedicineId INT NOT NULL FOREIGN KEY REFERENCES dbo.Medicines(MedicineId),
  Quantity   INT NOT NULL CHECK (Quantity > 0),
  UnitPrice  DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
  LineTotal AS (Quantity * UnitPrice) PERSISTED
);

-- PurchaseOrders
CREATE TABLE dbo.PurchaseOrders (
  PurchaseOrderId INT IDENTITY PRIMARY KEY,
  SupplierId INT NOT NULL FOREIGN KEY REFERENCES dbo.Suppliers(SupplierId),
  UserId     INT NOT NULL FOREIGN KEY REFERENCES dbo.Users(UserId),
  OrderDate  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
  TotalAmount DECIMAL(18,2) NOT NULL DEFAULT 0
);

-- PurchaseDetails
CREATE TABLE dbo.PurchaseDetails (
  PurchaseDetailId INT IDENTITY PRIMARY KEY,
  PurchaseOrderId INT NOT NULL FOREIGN KEY REFERENCES dbo.PurchaseOrders(PurchaseOrderId) ON DELETE CASCADE,
  MedicineId INT NOT NULL FOREIGN KEY REFERENCES dbo.Medicines(MedicineId),
  Quantity   INT NOT NULL CHECK (Quantity > 0),
  UnitPrice  DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),
  LineTotal AS (Quantity * UnitPrice) PERSISTED
);

-- INDEXES (tối ưu tra cứu & join)
CREATE INDEX IX_Users_RoleId           ON dbo.Users(RoleId) INCLUDE (IsActive);
CREATE INDEX IX_Medicines_Name         ON dbo.Medicines(MedicineName);
CREATE INDEX IX_Medicines_Category     ON dbo.Medicines(CategoryId);
CREATE INDEX IX_Medicines_Supplier     ON dbo.Medicines(SupplierId);
CREATE INDEX IX_Customers_Phone        ON dbo.Customers(Phone);
CREATE INDEX IX_Invoices_Date          ON dbo.Invoices(InvoiceDate);
CREATE INDEX IX_InvDetails_Invoice     ON dbo.InvoiceDetails(InvoiceId);
CREATE INDEX IX_InvDetails_Medicine    ON dbo.InvoiceDetails(MedicineId);
CREATE INDEX IX_PurDetails_Order       ON dbo.PurchaseDetails(PurchaseOrderId);
CREATE INDEX IX_PurDetails_Medicine    ON dbo.PurchaseDetails(MedicineId);

/* ===== 2) SP tạo user (Salt + Hash) & seed Roles/Users ===== */
IF OBJECT_ID('dbo.sp_CreateUser','P') IS NOT NULL DROP PROCEDURE dbo.sp_CreateUser;
GO
CREATE PROCEDURE dbo.sp_CreateUser
  @FullName NVARCHAR(100),
  @Username NVARCHAR(50),
  @Password NVARCHAR(200),
  @RoleName NVARCHAR(50)
AS
BEGIN
  SET NOCOUNT ON;
  DECLARE @RoleId INT = (SELECT RoleId FROM dbo.Roles WHERE RoleName=@RoleName);
  IF @RoleId IS NULL
  BEGIN
    RAISERROR('Role not found', 16, 1); RETURN;
  END
  DECLARE @Salt VARBINARY(16) = CRYPT_GEN_RANDOM(16);
  DECLARE @Hash VARBINARY(64) = HASHBYTES('SHA2_256', @Salt + CONVERT(VARBINARY(4000), @Password));
  INSERT INTO dbo.Users(FullName, Username, PasswordHash, Salt, RoleId)
  VALUES(@FullName, @Username, @Hash, @Salt, @RoleId);
END
GO

INSERT INTO dbo.Roles(RoleName) VALUES (N'Admin'), (N'Staff');

EXEC dbo.sp_CreateUser @FullName=N'Quản trị viên', @Username=N'admin', @Password=N'Admin@123', @RoleName=N'Admin';
EXEC dbo.sp_CreateUser @FullName=N'Nhân viên bán hàng', @Username=N'staff1', @Password=N'Staff@123', @RoleName=N'Staff';

 /* ===== 3) Seed Categories/Suppliers/Customers/Medicines ===== */
INSERT INTO dbo.Categories(CategoryName) VALUES
 (N'Kháng sinh'),(N'Giảm đau hạ sốt'),(N'Vitamin – Khoáng'),(N'Tiêu hóa'),(N'Hô hấp');

INSERT INTO dbo.Suppliers(SupplierName, Phone, Address) VALUES
 (N'Công ty Dược A', N'0909123456', N'Q.1, TP.HCM'),
 (N'Công ty Dược B', N'0909234567', N'Q. Bình Thạnh, TP.HCM');

INSERT INTO dbo.Customers(FullName, Phone, Address) VALUES
 (N'Nguyễn Văn An', N'0911000111', N'TP.HCM'),
 (N'Trần Thị Bình', N'0911222333', N'Bình Dương');

-- Thuốc (đơn giá chỉ minh hoạ)
INSERT INTO dbo.Medicines(MedicineName, CategoryId, SupplierId, Unit, UnitPrice, Stock, ExpiryDate, IsPrescriptionRequired, IsActive)
SELECT N'Amoxicillin 500mg', c.CategoryId, s.SupplierId, N'Hộp', 35000, 0, DATEADD(MONTH, 12, CAST(GETDATE() AS date)), 1, 1
FROM dbo.Categories c CROSS JOIN dbo.Suppliers s
WHERE c.CategoryName=N'Kháng sinh' AND s.SupplierName=N'Công ty Dược A';

INSERT INTO dbo.Medicines(MedicineName, CategoryId, SupplierId, Unit, UnitPrice, Stock, ExpiryDate, IsPrescriptionRequired, IsActive)
SELECT N'Paracetamol 500mg', c.CategoryId, s.SupplierId, N'Hộp', 12000, 0, DATEADD(MONTH, 18, CAST(GETDATE() AS date)), 0, 1
FROM dbo.Categories c CROSS JOIN dbo.Suppliers s
WHERE c.CategoryName=N'Giảm đau hạ sốt' AND s.SupplierName=N'Công ty Dược A';

INSERT INTO dbo.Medicines(MedicineName, CategoryId, SupplierId, Unit, UnitPrice, Stock, ExpiryDate, IsPrescriptionRequired, IsActive)
SELECT N'Vitamin C 1000mg', c.CategoryId, s.SupplierId, N'Hộp', 25000, 0, DATEADD(MONTH, 10, CAST(GETDATE() AS date)), 0, 1
FROM dbo.Categories c CROSS JOIN dbo.Suppliers s
WHERE c.CategoryName=N'Vitamin – Khoáng' AND s.SupplierName=N'Công ty Dược B';

-- Một thuốc sắp hết hạn để test cảnh báo
INSERT INTO dbo.Medicines(MedicineName, CategoryId, SupplierId, Unit, UnitPrice, Stock, ExpiryDate, IsPrescriptionRequired, IsActive)
SELECT N'Omeprazole 20mg', c.CategoryId, s.SupplierId, N'Hộp', 28000, 0, DATEADD(DAY, 20, CAST(GETDATE() AS date)), 0, 1
FROM dbo.Categories c CROSS JOIN dbo.Suppliers s
WHERE c.CategoryName=N'Tiêu hóa' AND s.SupplierName=N'Công ty Dược B';

 /* ===== 4) Triggers: tự động cập nhật tồn kho & tổng tiền ===== */

-- Nhập hàng: tăng tồn, cập nhật tổng PO
IF OBJECT_ID('dbo.trg_PurchaseDetails_AIUD','TR') IS NOT NULL DROP TRIGGER dbo.trg_PurchaseDetails_AIUD;
GO
CREATE TRIGGER dbo.trg_PurchaseDetails_AIUD
ON dbo.PurchaseDetails
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
  SET NOCOUNT ON;

  -- Điều chỉnh tồn kho theo thay đổi (INSERT/DELETE/UPDATE)
  ;WITH D AS (
    SELECT MedicineId, SUM(Quantity) AS Q FROM inserted GROUP BY MedicineId
  ),
  R AS (
    SELECT MedicineId, SUM(Quantity) AS Q FROM deleted  GROUP BY MedicineId
  )
  UPDATE m
    SET m.Stock = m.Stock + ISNULL(d.Q,0) - ISNULL(r.Q,0)
  FROM dbo.Medicines m
  LEFT JOIN D d ON d.MedicineId = m.MedicineId
  LEFT JOIN R r ON r.MedicineId = m.MedicineId
  WHERE ISNULL(d.Q,0) <> ISNULL(r.Q,0);

  -- Cập nhật TotalAmount của PurchaseOrders
  UPDATE po
    SET po.TotalAmount = x.SumLine
  FROM dbo.PurchaseOrders po
  CROSS APPLY (
    SELECT SUM(LineTotal) AS SumLine
    FROM dbo.PurchaseDetails pd
    WHERE pd.PurchaseOrderId = po.PurchaseOrderId
  ) x;
END
GO

-- Bán hàng: giảm tồn, cập nhật Subtotal/Total
IF OBJECT_ID('dbo.trg_InvoiceDetails_AIUD','TR') IS NOT NULL DROP TRIGGER dbo.trg_InvoiceDetails_AIUD;
GO
CREATE TRIGGER dbo.trg_InvoiceDetails_AIUD
ON dbo.InvoiceDetails
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
  SET NOCOUNT ON;

  -- Điều chỉnh tồn (hoá đơn bán ra: INSERT => giảm; DELETE => tăng)
  ;WITH D AS (
    SELECT MedicineId, SUM(Quantity) AS Q FROM inserted GROUP BY MedicineId
  ),
  R AS (
    SELECT MedicineId, SUM(Quantity) AS Q FROM deleted GROUP BY MedicineId
  )
  UPDATE m
    SET m.Stock = m.Stock - ISNULL(d.Q,0) + ISNULL(r.Q,0)
  FROM dbo.Medicines m
  LEFT JOIN D d ON d.MedicineId = m.MedicineId
  LEFT JOIN R r ON r.MedicineId = m.MedicineId
  WHERE ISNULL(d.Q,0) <> ISNULL(r.Q,0);

  -- Nếu có giảm tồn dưới 0 -> chặn (rollback)
  IF EXISTS (SELECT 1 FROM dbo.Medicines WHERE Stock < 0)
  BEGIN
    RAISERROR (N'Tồn kho âm! Kiểm tra lại số lượng bán.', 16, 1);
    ROLLBACK TRANSACTION;
    RETURN;
  END

  -- Cập nhật Subtotal/TotalAmount của Invoices (Total = Subtotal - Discount + Tax)
  UPDATE inv
    SET inv.Subtotal = ISNULL(x.SumLine,0),
        inv.TotalAmount = ISNULL(x.SumLine,0) - inv.Discount + inv.Tax
  FROM dbo.Invoices inv
  OUTER APPLY (
    SELECT SUM(LineTotal) AS SumLine
    FROM dbo.InvoiceDetails d
    WHERE d.InvoiceId = inv.InvoiceId
  ) x;
END
GO

/* ===== 5) Seed nghiệp vụ: Nhập hàng & Bán hàng mẫu ===== */

-- Nhập 2 PO
INSERT INTO dbo.PurchaseOrders(SupplierId, UserId)
VALUES ((SELECT SupplierId FROM dbo.Suppliers WHERE SupplierName=N'Công ty Dược A'),
        (SELECT UserId FROM dbo.Users WHERE Username=N'admin')),
       ((SELECT SupplierId FROM dbo.Suppliers WHERE SupplierName=N'Công ty Dược B'),
        (SELECT UserId FROM dbo.Users WHERE Username=N'staff1'));

-- Chi tiết nhập: tăng tồn qua trigger
INSERT INTO dbo.PurchaseDetails(PurchaseOrderId, MedicineId, Quantity, UnitPrice)
SELECT po.PurchaseOrderId, m.MedicineId, 200, m.UnitPrice
FROM dbo.PurchaseOrders po CROSS JOIN dbo.Medicines m
WHERE po.PurchaseOrderId = (SELECT MIN(PurchaseOrderId) FROM dbo.PurchaseOrders)
  AND m.MedicineName IN (N'Paracetamol 500mg', N'Amoxicillin 500mg');

INSERT INTO dbo.PurchaseDetails(PurchaseOrderId, MedicineId, Quantity, UnitPrice)
SELECT po.PurchaseOrderId, m.MedicineId, 120, m.UnitPrice
FROM dbo.PurchaseOrders po CROSS JOIN dbo.Medicines m
WHERE po.PurchaseOrderId = (SELECT MAX(PurchaseOrderId) FROM dbo.PurchaseOrders)
  AND m.MedicineName IN (N'Vitamin C 1000mg', N'Omeprazole 20mg');

-- Tạo 1 hóa đơn bán
INSERT INTO dbo.Invoices(CustomerId, UserId, Discount, Tax)
VALUES ((SELECT CustomerId FROM dbo.Customers WHERE FullName=N'Nguyễn Văn An'),
        (SELECT UserId FROM dbo.Users WHERE Username=N'staff1'),
        2000, 1500);

DECLARE @InvId INT = SCOPE_IDENTITY();

INSERT INTO dbo.InvoiceDetails(InvoiceId, MedicineId, Quantity, UnitPrice)
SELECT @InvId, m.MedicineId, 3, m.UnitPrice
FROM dbo.Medicines m WHERE m.MedicineName=N'Paracetamol 500mg';

INSERT INTO dbo.InvoiceDetails(InvoiceId, MedicineId, Quantity, UnitPrice)
SELECT @InvId, m.MedicineId, 1, m.UnitPrice
FROM dbo.Medicines m WHERE m.MedicineName=N'Vitamin C 1000mg';

-- Kiểm tra kết quả nhanh
-- SELECT * FROM dbo.Medicines;
-- SELECT * FROM dbo.Invoices;
-- SELECT * FROM dbo.InvoiceDetails;

/* ===== 6) VIEW tiện ích ===== */

-- Thuốc sắp hết hạn trong 30 ngày
IF OBJECT_ID('dbo.vMedicines_NearExpiry','V') IS NOT NULL DROP VIEW dbo.vMedicines_NearExpiry;
GO
CREATE VIEW dbo.vMedicines_NearExpiry AS
SELECT MedicineId, MedicineName, Unit, Stock, ExpiryDate, DATEDIFF(DAY, CAST(GETDATE() AS date), ExpiryDate) AS DaysToExpire
FROM dbo.Medicines
WHERE ExpiryDate <= DATEADD(DAY, 30, CAST(GETDATE() AS date)) AND IsActive=1;

-- Thuốc tồn thấp (ngưỡng 20)
IF OBJECT_ID('dbo.vMedicines_LowStock','V') IS NOT NULL DROP VIEW dbo.vMedicines_LowStock;
GO
CREATE VIEW dbo.vMedicines_LowStock AS
SELECT MedicineId, MedicineName, Unit, Stock
FROM dbo.Medicines
WHERE Stock < 20 AND IsActive=1;

-- Doanh thu theo ngày
IF OBJECT_ID('dbo.vRevenue_Daily','V') IS NOT NULL DROP VIEW dbo.vRevenue_Daily;
GO
CREATE VIEW dbo.vRevenue_Daily AS
SELECT CAST(InvoiceDate AT TIME ZONE 'UTC' AS DATE) AS Ngay,
       SUM(TotalAmount) AS DoanhThu
FROM dbo.Invoices
GROUP BY CAST(InvoiceDate AT TIME ZONE 'UTC' AS DATE);
GO
