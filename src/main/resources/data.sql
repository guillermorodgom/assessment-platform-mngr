-- ============================================
-- USUARIOS (mismos que auth, necesarios para FKs)
-- Credenciales: admin/password123, candidato1/password123, candidato2/password123
-- ============================================

INSERT INTO usuarios (username, password, email, nombre_completo) VALUES ('admin', '$2a$10$M0pf.89UgE/aBWVf2aXwp.x0sPmLtPjU9JPD0na7VSCn8yWNl60ma', 'admin@assessment.com', 'Administrador');
INSERT INTO usuarios (username, password, email, nombre_completo) VALUES ('candidato1', '$2a$10$M0pf.89UgE/aBWVf2aXwp.x0sPmLtPjU9JPD0na7VSCn8yWNl60ma', 'candidato1@test.com', 'Juan Perez');
INSERT INTO usuarios (username, password, email, nombre_completo) VALUES ('candidato2', '$2a$10$M0pf.89UgE/aBWVf2aXwp.x0sPmLtPjU9JPD0na7VSCn8yWNl60ma', 'candidato2@test.com', 'Maria Garcia');

-- ============================================
-- CUESTIONARIO DE EJEMPLO
-- ============================================

INSERT INTO cuestionarios (nombre, descripcion, tiempo_limite, cantidad_preguntas, activo, creado_por, created_at)
VALUES ('Evaluacion Java Basico', 'Cuestionario de evaluacion de conocimientos basicos de Java', 3600, 3, TRUE, 1, CURRENT_TIMESTAMP);

-- ============================================
-- PREGUNTAS
-- ============================================

-- Pregunta 1: Opcion unica
INSERT INTO preguntas (titulo, descripcion, tipo_pregunta, lenguaje_permitido, puntaje, cuestionario_id)
VALUES ('Tipos primitivos en Java', 'Cual de los siguientes NO es un tipo primitivo en Java?', 'OPCION_UNICA', NULL, 1.00, 1);

-- Pregunta 2: Opcion multiple
INSERT INTO preguntas (titulo, descripcion, tipo_pregunta, lenguaje_permitido, puntaje, cuestionario_id)
VALUES ('Modificadores de acceso', 'Seleccione los modificadores de acceso validos en Java', 'OPCION_MULTIPLE', NULL, 2.00, 1);

-- Pregunta 3: Codigo
INSERT INTO preguntas (titulo, descripcion, tipo_pregunta, lenguaje_permitido, puntaje, cuestionario_id)
VALUES ('Suma de dos numeros', 'Escriba una funcion que reciba dos numeros enteros por entrada estandar y devuelva su suma', 'CODIGO', 'JAVA', 3.00, 1);

-- ============================================
-- OPCIONES DE RESPUESTA
-- ============================================

-- Opciones para pregunta 1 (opcion unica)
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('int', FALSE, 1);
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('String', TRUE, 1);
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('boolean', FALSE, 1);
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('double', FALSE, 1);

-- Opciones para pregunta 2 (opcion multiple)
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('public', TRUE, 2);
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('private', TRUE, 2);
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('protected', TRUE, 2);
INSERT INTO opciones_respuesta (texto, es_correcta, pregunta_id) VALUES ('internal', FALSE, 2);

-- ============================================
-- CASOS DE PRUEBA (para pregunta de codigo)
-- ============================================

INSERT INTO casos_de_prueba (input, expected_output, pregunta_id) VALUES ('2 3', '5', 3);
INSERT INTO casos_de_prueba (input, expected_output, pregunta_id) VALUES ('0 0', '0', 3);
INSERT INTO casos_de_prueba (input, expected_output, pregunta_id) VALUES ('-1 5', '4', 3);
