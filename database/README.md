# PostgreSQL — RotaCerta

O banco principal da plataforma é PostgreSQL 17.

- `init.sql` prepara extensões do banco durante a criação do container.
- O schema da aplicação é versionado pelo Flyway no backend em `backend/src/main/resources/db/migration`.
- As migrations criam clientes, pedidos, eventos de rastreamento, índices e dados de demonstração.

O PostgreSQL é iniciado pelo `docker-compose.yml` e exposto localmente na porta `5432`.
