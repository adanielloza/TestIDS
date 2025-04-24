package com.bionetttt.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SchemaRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer://init?repeatCount=1&delay=500")
          .routeId("initialize-schema")
          .to("sql:classpath:db/schema.sql?dataSource=#dataSource&separator=;")
          .log("✅ SQLite schema initialized");
    }
}
