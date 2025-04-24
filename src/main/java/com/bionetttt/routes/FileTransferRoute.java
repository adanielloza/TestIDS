package com.bionetttt.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FileTransferRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        // 1) Any exception during parsing or insert → route original to error/
        onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "❌ Error with file ${header.CamelFileName}: ${exception.message}")
            .toD("file:error/${header.CamelFileName}")
            .stop();

        // 2) Poll input-labs, move on success/failure
        from("file:input-labs"
             + "?readLock=changed"
             + "&move=processed/${file:name}"
             + "&moveFailed=error/${file:name}")
          .routeId("file-transfer")
          .choice()
            // only .csv (any case) gets processed
            .when(header("CamelFileName").regex("(?i).*\\.csv$"))
              .log("✅ CSV detected, processing: ${file:name}")
              .to("direct:processCsv")
            .otherwise()
              // non-csv will go to error via moveFailed *after* stop()
              .log("🔴 Not a CSV, skipping: ${file:name}")
              .stop()
          .end();

        // 3) Parse & split, skip header row
        from("direct:processCsv")
          .routeId("process-csv")
          .unmarshal().csv()
          .split(body()).streaming()
            .filter(header(Exchange.SPLIT_INDEX).isGreaterThan(0))
            .setHeader("CamelSqlParameters", simple("${body}"))
            .to("direct:insertToDb")
          .end()
          .log("✅ Finished processing all rows of ${header.CamelFileName}");

        // 4) Insert each row into SQLite
        from("direct:insertToDb")
          .routeId("insert-to-db")
          .to("sql:INSERT OR IGNORE INTO resultados_examenes("
              + "laboratorio_id,paciente_id,tipo_examen,resultado,fecha_examen"
              + ") VALUES("
              + ":#${header.CamelSqlParameters[0]},"
              + ":#${header.CamelSqlParameters[1]},"
              + ":#${header.CamelSqlParameters[2]},"
              + ":#${header.CamelSqlParameters[3]},"
              + ":#${header.CamelSqlParameters[4]}"
              + ")?dataSource=#dataSource")
          .log("⚙️ Inserted row: ${header.CamelSqlParameters}");
    }
}
