-- Script para apagar todo o conteúdo do banco H2 local.
-- Execute este arquivo no H2 Console ou usando a conexão JDBC do backend.

SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE usuario_roles;
TRUNCATE TABLE donations;
TRUNCATE TABLE subscriptions;
TRUNCATE TABLE external_metrics;
TRUNCATE TABLE beneficiados;
TRUNCATE TABLE parceiros;
TRUNCATE TABLE usuario;

SET REFERENTIAL_INTEGRITY TRUE;

-- Se quiser reiniciar os IDs manualmente no H2, descomente as linhas abaixo:
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 1;
ALTER TABLE beneficiados ALTER COLUMN id RESTART WITH 1;
ALTER TABLE donations ALTER COLUMN id RESTART WITH 1;
ALTER TABLE subscriptions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE parceiros ALTER COLUMN id RESTART WITH 1;
ALTER TABLE external_metrics ALTER COLUMN id RESTART WITH 1;
