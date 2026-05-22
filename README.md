# Livraime (sistemas distribuidos)
# https://github.com/AnthonyLuciano/Livraime-SistemasDistribuidos

Plataforma de doação e assinatura de livros que conecta doadores a crianças carentes e incentiva a leitura através do envio mensal de livros e brindes.

Este repositório contém dois projetos principais:

- `backend/` — API construída com Spring Boot (Java + Maven).
- `frontend/` — SPA construída com React + Vite + TypeScript.

Este README fornece um guia rápido de instalação e uso para desenvolvimento local.

## Pré-requisitos

- Git
- Node.js (recomendado v18+)
- npm (ou yarn/pnpm)
- Java JDK 21 (conforme `pom.xml`)
- Maven (pode usar o wrapper `./mvnw` incluído)

Opcionalmente você pode usar o Bun para o frontend se preferir, mas as instruções abaixo usam `npm`.

## Configuração rápida

Clone o repositório:

```bash
git clone https://github.com/AnthonyLuciano/Livraime-SistemasDistribuidos.git
cd /Livraime-SistemasDistribuidos
```

### Backend (API)

1. Entre na pasta do backend:

```bash
cd backend
```

2. Configuração de ambiente

- O backend usa H2 (arquivo) por padrão e já está configurado em `src/main/resources/application.properties`.
- Porta padrão: `8081` (definida em `application.properties`).
- Se quiser alterar variáveis (ex.: SMTP), edite `application.properties` ou forneça variáveis de ambiente.

3. Executar em modo desenvolvimento (com o wrapper Maven):

```bash
./mvnw spring-boot:run
```

4. Construir jar e executar:

```bash
./mvnw -DskipTests package
java -jar target/Livraime-0.0.1-SNAPSHOT.jar
```

5. Endpoints úteis

- Swagger / OpenAPI UI: por padrão está habilitado por `springdoc`, ver `/swagger-ui.html` ou `/swagger-ui/index.html` (dependendo da versão).
- H2 Console (debug): `http://localhost:8081/h2-console`

> Observação: o controller de administração está mapeado sob `/api/admins` (veja `AdminController`). As rotas de ativar/desativar parceiros estão em `/api/admins/partners/{id}/disable` e `/api/admins/partners/{id}/enable`.

### Frontend (UI)

1. Abra outro terminal e vá para a pasta do frontend:

```bash
cd frontend
```

2. Instale dependências:

```bash
npm install
```

3. Variáveis de ambiente

O frontend usa `VITE_BASE_API_URL` para apontar para a API. Há um exemplo em `.env.development`:

```env
VITE_BASE_API_URL="http://localhost:8081/api"
```

Garanta que a URL aponte para o backend em execução (porta 8081 por padrão).

4. Executar em desenvolvimento:

```bash
npm run dev
```

### Fluxo de desenvolvimento recomendado

1. Inicie o backend (`./mvnw spring-boot:run`).
2. Inicie o frontend (`npm run dev`).
3. Abra o navegador em `http://localhost:5173` e acesse a área de administração para testar recursos (Gerenciar Parceiros / Usuários).

## Funcionalidades administrativas relevantes

- Edição de usuário: `frontend` já contém componentes que chamam endpoints administrativos para desativar/reativar usuários.
- Edição de parceiro: `frontend` agora usa endpoints administrativos para desativar/reativar parceiros (`/api/admins/partners/...`).

Se for necessário que o botão "Salvar Alterações" do diálogo de parceiro persista mudanças no backend, implemente um `PATCH` para `/api/admins/partners/{id}` (há um handler em `AdminController` mapeado para `/partners/{id}`).

## Testes e verificação rápida

- Verifique logs do backend para ver as requisições recebidas (Spring Boot imprime SQL e logs no console).
- No frontend, abra DevTools -> Console para ver erros ou warnings (por exemplo, avisos de campos controlados sem onChange que já foram corrigidos).

## Dicas e resolução de problemas

- Se o frontend não encontrar a API, confirme `VITE_BASE_API_URL` e que o backend está na porta correta (`8081`).
- Se houver problemas de versão Java, instale JDK 21 ou ajuste `<java.version>` no `pom.xml` conforme seu JDK instalado.
- Para resetar o banco H2: pare a aplicação e remova o arquivo `backend/data/database.mv.db` (o arquivo exato depende da configuração). O JPA está configurado como `update`.

## Contribuição

1. Crie uma branch a partir de `dev` ou `main` (dependendo do fluxo):

```bash
git checkout -b feat/minha-nova-funcionalidade
```

2. Faça commits descritivos e abra um Pull Request no GitHub.

## Contato

Se precisar de ajuda com rotas específicas do backend ou integração frontend-backend, envie detalhes do erro e posso ajudar a ajustar os endpoints e chamadas Axios.

---

Arquivo de referência:
- `backend/src/main/resources/application.properties` (configura porta, datasource e mail)
- `frontend/.env.development` (configura `VITE_BASE_API_URL`)

