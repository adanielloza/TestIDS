package com.bionetttt;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

    /**
     * DataSource para SQLite usando Apache Commons DBCP2.
     * Asegúrate de tener 'sqlite-jdbc' y 'commons-dbcp2' en tu pom.xml.
     */
    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:./data/bionet.db");
        ds.setMaxTotal(5);
        ds.setMaxIdle(2);
        return ds;
    }
}
