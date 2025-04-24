package com.bionetttt.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FileTransferRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        // 1) Vigilar y separar CSV/no-CSV
        from("file:input-labs?readLock=changed&moveFailed=error")
            .routeId("file-transfer")
            .choice()
                .when(simple("${file:ext} == 'csv'"))
                    .to("file:error")
                .otherwise()
                    .to("direct:processCsv")
            .end();

        // 2) Parsear CSV y split por filas (salta cabecera)
        from("direct:processCsv")
            .routeId("process-csv")
            .unmarshal().csv()              // formato CSV por defecto
            .split(body()).streaming()
                .filter(header(Exchange.SPLIT_INDEX).isGreaterThan(0))
                .to("direct:insertToDb")
            .end()
            .to("file:processed");

        // 3) Insertar fila a fila en SQLite
        from("direct:insertToDb")
            .routeId("insert-to-db")
            .setHeader("CamelSqlParameters", simple("${body}"))
            .to("sql:"
              + "INSERT OR IGNORE INTO resultados_examenes("
              + "laboratorio_id, paciente_id, tipo_examen, resultado, fecha_examen"
              + ") VALUES("
              + ":#${header.CamelSqlParameters[0]},"
              + ":#${header.CamelSqlParameters[1]},"
              + ":#${header.CamelSqlParameters[2]},"
              + ":#${header.CamelSqlParameters[3]},"
              + ":#${header.CamelSqlParameters[4]}"
              + ")?dataSource=#dataSource");
    }
}
