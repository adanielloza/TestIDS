package com.bionetttt;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SchemaRunner {
    public static void createDatabase(String dbFile, String schemaPath) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
             Statement stmt = conn.createStatement()) {
            String sql = new String(Files.readAllBytes(Paths.get(schemaPath)));
            stmt.executeUpdate(sql);
        }
    }
}
