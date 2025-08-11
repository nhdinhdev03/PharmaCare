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
