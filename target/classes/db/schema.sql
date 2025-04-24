CREATE TABLE IF NOT EXISTS resultados_examenes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  laboratorio_id INTEGER NOT NULL,
  paciente_id INTEGER NOT NULL,
  tipo_examen TEXT NOT NULL,
  resultado TEXT NOT NULL,
  fecha_examen TEXT NOT NULL,
  UNIQUE(laboratorio_id, paciente_id, tipo_examen, fecha_examen)
);

CREATE TABLE IF NOT EXISTS log_cambios_resultados (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  operacion TEXT NOT NULL,
  paciente_id INTEGER NOT NULL,
  tipo_examen TEXT NOT NULL,
  fecha TEXT NOT NULL
);

CREATE TRIGGER IF NOT EXISTS trg_log_insert
AFTER INSERT ON resultados_examenes
BEGIN
  INSERT INTO log_cambios_resultados(
    operacion,paciente_id,tipo_examen,fecha
  ) VALUES(
    'INSERT', NEW.paciente_id, NEW.tipo_examen, datetime('now')
  );
END;

CREATE TRIGGER IF NOT EXISTS trg_log_update
AFTER UPDATE ON resultados_examenes
BEGIN
  INSERT INTO log_cambios_resultados(
    operacion,paciente_id,tipo_examen,fecha
  ) VALUES(
    'UPDATE', NEW.paciente_id, NEW.tipo_examen, datetime('now')
  );
END;
