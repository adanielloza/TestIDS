package db;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        // Carga src/main/resources/sql/schema.sql
        ClassPathResource script = new ClassPathResource("sql/schema.sql");
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, script);
        }
    }
}
