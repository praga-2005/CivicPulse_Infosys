import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbFix {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/civicpulse_db";
        String user = "postgres";
        String password = "praga@2005";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "ALTER TABLE grievances DROP CONSTRAINT fkgjwoo1cnmtlr526fkjhx7g6qh";
            stmt.executeUpdate(sql);
            System.out.println("Foreign key dropped successfully!");

            // Also check for department foreign key just in case
            try {
                stmt.executeUpdate("ALTER TABLE grievances DROP CONSTRAINT fk_department");
            } catch (Exception e) {}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
