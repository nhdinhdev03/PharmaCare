# PharmaCare Swing GUI Pro

Pro-level Java Swing + SQL Server app (Maven, Java 17). Features:
- FlatLaf look & feel
- Products CRUD with search, sort, validation
- SwingWorker for DB ops (no UI freeze)
- Service layer, utility dialogs
- Ready to expand (Customers, Invoices)

## Run
1) SQL Server → run `db/schema.sql`
2) Edit `src/main/resources/application.properties`
3) `mvn -q exec:java`
