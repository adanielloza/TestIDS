package com.bionetttt.routes;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;

public class DatabaseIngestionRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("file:input?noop=true") // Define a route starting point
          .split(body().tokenize("\n")).streaming()
          .log("Ingestando ${file:name}")
          // Separa líneas
          .split(body().tokenize("\n")).streaming()
            // Saltar índice 0 (cabecera)
            .filter(exchange -> (Integer)exchange.getProperty("CamelSplitIndex") > 0)
            .process(exchange -> {
                String[] cols = exchange.getIn().getBody(String.class).split(",");
                Map<String,Object> params = new HashMap<>();
                params.put("laboratorio_id", cols[0]);
                params.put("paciente_id",    cols[1]);
                params.put("tipo_examen",    cols[2]);
                params.put("resultado",      cols[3]);
                params.put("fecha_examen",   cols[4]);
                exchange.getIn().setBody(params);
            })
            // Inserta; si ya existe, UNIQUE lanza excepción y Camel lo moverá a error/
            .to("sql:INSERT INTO resultados_examenes "
                + "(laboratorio_id,paciente_id,tipo_examen,resultado,fecha_examen) "
                + "VALUES (:#laboratorio_id,:#paciente_id,:#tipo_examen,:#resultado,:#fecha_examen)"
                + "?dataSource=#sqliteDs")
          .end()
          .log("Ingestión completa de ${file:name}");
    }
}
