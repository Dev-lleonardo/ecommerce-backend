# E-commerce Backend API

API REST de e-commerce feita com Java 21, Spring Boot 3.2.5, Spring Security, JWT, MySQL, Flyway e Swagger/OpenAPI.

## Funcionalidades

- Cadastro e login em `/auth/register` e `/auth/login`.
- Autenticacao stateless com Bearer Token JWT.
- Controle de acesso por roles `ROLE_USER` e `ROLE_ADMIN`.
- CRUD de categorias e produtos para admin.
- Consulta de categorias/produtos para usuarios autenticados.
- Carrinho por usuario: ver, adicionar item, alterar quantidade, remover item e limpar.
- Pedidos criados a partir do carrinho.
- Admin pode listar todos os pedidos e confirmar pedidos.
- Usuario comum so acessa seus proprios pedidos.

## Configuracao

1. Copie `.env.example` para `.env`.
2. Troque todas as senhas e defina um `JWT_SECRET` com pelo menos 32 caracteres.
3. Para rodar com Docker:

```bash
docker compose up --build
```

4. Para rodar pela IDE ou Maven, suba o MySQL e execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

## Swagger

Com a API rodando, acesse:

```text
http://localhost:8080/swagger-ui.html
```

Use o botao `Authorize` e informe:

```text
Bearer SEU_TOKEN_JWT
```

## Fluxo rapido de teste

1. Registrar usuario em `POST /auth/register`.
2. Fazer login em `POST /auth/login`.
3. Fazer login com o admin configurado no `.env`.
4. Como admin, criar categoria em `POST /categories`.
5. Como admin, criar produto em `POST /products`.
6. Como usuario, adicionar item em `POST /cart/items`.
7. Como usuario, consultar carrinho em `GET /cart`.
8. Como usuario, criar pedido em `POST /orders`.
9. Como admin, confirmar pedido em `PATCH /orders/{id}/confirm`.

## Testes

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```

## Seguranca

- Nunca versione `.env` com credenciais reais.
- Rotacione `DB_PASSWORD`, `ADMIN_PASSWORD` e `JWT_SECRET` antes de publicar ou fazer deploy.
- Em producao, use HTTPS, CORS restrito ao dominio real e segredos gerenciados pelo ambiente.
