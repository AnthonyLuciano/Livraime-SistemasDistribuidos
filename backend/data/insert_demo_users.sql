-- ========================================================================
-- Script SQL para inserir usuários DEMO e ADMIN com beneficiados
-- Database: Livraime
-- Descrição: Cria usuários de teste com dados vinculados
-- 
-- IMPORTANTE: Este script remove os dados antigos de demo/admin e os recria
-- Se você não quer deletar dados, remova as linhas de DELETE abaixo
-- 
-- Se receber erro "duplicate key value violates unique constraint":
-- 1. Execute o bloco de LIMPEZA DE DADOS (linhas 7-21) separadamente
-- 2. Depois execute o resto do script
-- ========================================================================

-- ========================================================================
-- 0. LIMPAR DADOS ANTIGOS (obrigatório para evitar duplicatas)
-- ========================================================================
-- Deletar beneficiados dos usuários demo e admin
DELETE FROM beneficiados 
WHERE cliente_assinante_id IN (
    SELECT id FROM usuario WHERE email IN ('demo@livrai-me.org', 'admin@livrai-me.org')
);

-- Deletar roles dos usuários demo e admin
DELETE FROM usuario_roles 
WHERE usuario_id IN (
    SELECT id FROM usuario WHERE email IN ('demo@livrai-me.org', 'admin@livrai-me.org')
);

-- Deletar usuários demo e admin
DELETE FROM usuario WHERE email IN ('demo@livrai-me.org', 'admin@livrai-me.org');

-- Ajustar sequência do Postgres para evitar chaves duplicadas em tabelas com ID serial/identity
SELECT setval(pg_get_serial_sequence('beneficiados', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM beneficiados;
SELECT setval(pg_get_serial_sequence('usuario', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM usuario;

-- ========================================================================
-- 1. INSERIR USUÁRIO DEMO
-- ========================================================================
-- Hash BCrypt de "demo123": $2a$10$gzMSDlETJjMT5henJw2Yau0t/CFMMY635XRB95ENvXmKHZgyHNjwm
-- CPF Demo: 000.000.000-00
INSERT INTO usuario (
    nome,
    email,
    cpf,
    senha,
    ativo,
    email_verificado,
    codigo_verificacao,
    data_cadastro,
    street,
    endereco_numero,
    complement,
    neighborhood,
    city,
    state,
    zip_code,
    area_code,
    telefone_numero,
    plano
) VALUES (
    'Demo User',
    'demo@livrai-me.org',
    '000.000.000-00',
    '$2b$10$5vGj12lAglBHoxr7L5Gggef0czZcAQ5HMx87VbF5yN.sFAsU0s3aW',
    true,
    true,
    'VERIFIED',
    NOW(),
    'Rua Demo',
    '123',
    'Apartamento 456',
    'Centro',
    'Natal',
    'RN',
    '59000-000',
    '84',
    '999999999',
    NULL
);

-- ========================================================================
-- 2. INSERIR PAPEL (ROLE) DO USUÁRIO DEMO
-- ========================================================================
-- Recuperar o ID do usuário demo inserido
-- A tabela de roles é ElementCollection, então precisa de entry na tabela "usuario_roles"
INSERT INTO usuario_roles (usuario_id, roles)
VALUES ((SELECT id FROM usuario WHERE email = 'demo@livrai-me.org'), 'USER');

-- ========================================================================
-- 3. INSERIR USUÁRIO ADMIN
-- ========================================================================
-- Hash BCrypt de "admin123": $2a$10$1wqtqB8oaq6H7vA5Harbfew5A.3bIb9gciA6Y0gPtjM5baUvE9wcm
INSERT INTO usuario (
    nome,
    email,
    cpf,
    senha,
    ativo,
    email_verificado,
    codigo_verificacao,
    data_cadastro,
    street,
    endereco_numero,
    complement,
    neighborhood,
    city,
    state,
    zip_code,
    area_code,
    telefone_numero,
    plano
) VALUES (
    'Admin User',
    'admin@livrai-me.org',
    '111.111.111-11',
    '$2b$10$lSVCTT.I9XH4hz.7q6bYP.Kxbu.gNM4vfAnZBPsXrzLCu8wgugpuO',
    true,
    true,
    'VERIFIED',
    NOW(),
    'Rua Admin',
    '999',
    'Sala 001',
    'Centro',
    'Natal',
    'RN',
    '59000-000',
    '84',
    '988888888',
    NULL
);

-- ========================================================================
-- 4. INSERIR PAPEL (ROLE) DO USUÁRIO ADMIN
-- ========================================================================
INSERT INTO usuario_roles (usuario_id, roles)
VALUES ((SELECT id FROM usuario WHERE email = 'admin@livrai-me.org'), 'ADMIN');

-- ========================================================================
-- 5. INSERIR BENEFICIADOS PARA O USUÁRIO DEMO
-- ========================================================================
-- Beneficiado 1
INSERT INTO beneficiados (
    nome,
    idade,
    data_nascimento,
    estado,
    descricao_necessidades,
    ativo,
    cliente_assinante_id
) VALUES (
    'João Demo',
    12,
    '2014-03-15',
    'Rio Grande do Norte',
    'Necessita de material escolar e uniforme para a escola',
    true,
    (SELECT id FROM usuario WHERE email = 'demo@livrai-me.org')
);

-- Beneficiado 2
INSERT INTO beneficiados (
    nome,
    idade,
    data_nascimento,
    estado,
    descricao_necessidades,
    ativo,
    cliente_assinante_id
) VALUES (
    'Maria Demo',
    10,
    '2016-07-22',
    'Rio Grande do Norte',
    'Interessada em livros infantis e atividades educativas',
    true,
    (SELECT id FROM usuario WHERE email = 'demo@livrai-me.org')
);

-- Beneficiado 3
INSERT INTO beneficiados (
    nome,
    idade,
    data_nascimento,
    estado,
    descricao_necessidades,
    ativo,
    cliente_assinante_id
) VALUES (
    'Pedro Demo',
    15,
    '2009-11-08',
    'Rio Grande do Norte',
    'Gosta de esportes e precisa de tênis e equipamentos esportivos',
    true,
    (SELECT id FROM usuario WHERE email = 'demo@livrai-me.org')
);

-- ========================================================================
-- 6. VERIFICAÇÃO (comentários de teste)
-- ========================================================================
-- Para verificar os dados inseridos, execute:
-- SELECT * FROM usuario WHERE email IN ('demo@livrai-me.org', 'admin@livrai-me.org');
-- SELECT * FROM beneficiados WHERE cliente_assinante_id = (SELECT id FROM usuario WHERE email = 'demo@livrai-me.org');
-- SELECT * FROM usuario_roles WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'demo@livrai-me.org');
-- SELECT * FROM usuario_roles WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@livrai-me.org');

-- ========================================================================
-- NOTAS IMPORTANTES:
-- ========================================================================
-- 1. Email Demo: demo@livrai-me.org | Senha: demo123
-- 2. Email Admin: admin@livrai-me.org | Senha: admin123
-- 3. Os usuários já estão com email_verificado = true e ativo = true
-- 4. Cada usuário demo possui 3 beneficiados associados
-- 5. Se precisar gerar um novo hash BCrypt, use a função:
--    https://bcrypt-generator.com/ ou use sua aplicação Spring
-- ========================================================================
