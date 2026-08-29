# RotaCerta 🚚📦

> **Smart E-commerce Logistics & Delivery Platform**

[![Android CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

## 📌 Sobre o projeto

**RotaCerta** é uma plataforma Full Stack para gestão logística de pedidos de e-commerce, rastreamento de entregas e acompanhamento operacional do fluxo entre loja, centro de distribuição, transportadora, entregador e cliente final.

O projeto foi desenvolvido para demonstrar, de forma prática, a integração entre **Frontend React**, **Backend Java com Spring Boot**, **PostgreSQL** e **Docker**, utilizando uma arquitetura baseada em API REST e separação clara de responsabilidades.

O repositório também contém a evolução do aplicativo Android do entregador, construído com **Kotlin + Jetpack Compose**, formando um ecossistema que pode atender operação web, cliente e mobile.

---

## 🎯 Objetivos

- Gerenciar pedidos e entregas de e-commerce.
- Acompanhar o status logístico de cada pedido.
- Gerar e consultar códigos de rastreamento.
- Registrar o histórico de eventos de cada entrega.
- Disponibilizar um dashboard operacional para a empresa.
- Permitir atualização de status pela central de operações.
- Integrar frontend, backend e banco de dados em uma aplicação real.
- Executar todo o ambiente de forma reproduzível com Docker Compose.
- Evoluir o projeto para autenticação, mensageria, observabilidade e rastreamento em tempo real.

---

## 🧱 Stack tecnológica

### Frontend

- **React 19**
- **TypeScript**
- **Vite**
- **React Router**
- **Axios**
- **Lucide React**
- **CSS responsivo**
- **Nginx** para servir o build em container

### Backend

- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Web**
- **Spring Data JPA**
- **Spring Validation**
- **Spring Transaction Management**
- **Spring Boot Actuator**
- **Hibernate ORM**
- **Maven**
- **Swagger / OpenAPI**

### Banco de dados

- **PostgreSQL 17**
- **Flyway** para versionamento do schema
- Relacionamentos com **JPA/Hibernate**
- Índices para status e código de rastreamento

### DevOps e infraestrutura

- **Docker**
- **Docker Compose**
- **Dockerfile multi-stage** para o backend
- **Nginx** no container do frontend
- Healthcheck do PostgreSQL
- Variáveis de ambiente para configuração da aplicação

### Mobile

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Navigation Compose**
- **Hilt**
- **CameraX**
- **ML Kit Barcode Scanning**
- **ViewModel + StateFlow**
- **JUnit / MockK**

---

## 🏗️ Arquitetura

```text
                    ┌──────────────────────────┐
                    │      React + Vite        │
                    │   Painel Operacional     │
                    └────────────┬─────────────┘
                                 │
                            REST / JSON
                                 │
                    ┌────────────▼─────────────┐
                    │     Spring Boot API      │
                    │        Java 21           │
                    ├──────────────────────────┤
                    │ Controller               │
                    │ Service                  │
                    │ Repository               │
                    │ DTO / Validation         │
                    └────────────┬─────────────┘
                                 │
                          Spring Data JPA
                                 │
                    ┌────────────▼─────────────┐
                    │      PostgreSQL 17       │
                    │   Flyway Migrations      │
                    └──────────────────────────┘

              Docker Compose orquestra o ambiente
```

### Fluxo de negócio

```text
Pedido criado
      ↓
Pagamento aprovado
      ↓
Separação
      ↓
Embalagem
      ↓
Pronto para envio
      ↓
Em transporte
      ↓
Saiu para entrega
      ↓
Entregue
```

---

## 🖥️ Funcionalidades Web

### Dashboard operacional

O dashboard consome dados reais da API Spring Boot e apresenta:

- total de pedidos;
- pedidos em separação;
- pedidos em transporte;
- pedidos que saíram para entrega;
- entregas concluídas;
- falhas de entrega;
- últimos pedidos e seus respectivos status.

### Gestão de pedidos

- listagem de pedidos;
- identificação do cliente;
- valor da compra;
- código de rastreamento;
- atualização do status logístico.

### Rastreamento

O cliente ou operador pode consultar uma entrega pelo código de rastreamento e visualizar a timeline completa de eventos.

Código disponível nos dados de demonstração:

```text
RC-2026-SP-8F29A73
```

---

## 🔌 API REST

### Dashboard

```http
GET /api/dashboard
```

### Pedidos

```http
GET /api/orders
GET /api/orders/{id}
```

### Rastreamento

```http
GET /api/tracking/{trackingCode}
```

### Entregas

```http
PATCH /api/deliveries/{id}/status
POST  /api/deliveries/{id}/confirm
POST  /api/deliveries/{id}/failure
```

Exemplo de atualização de status:

```json
{
  "status": "OUT_FOR_DELIVERY",
  "location": "São Paulo/SP"
}
```

---

## 🗄️ Modelo de dados atual

```text
Customer
   │
   └── 1:N ── Order
                │
                └── 1:N ── TrackingEvent
```

Principais tabelas:

```text
customers
orders
tracking_events
flyway_schema_history
```

O relacionamento `Order -> Customer` utiliza carregamento `LAZY`, com os serviços de leitura executados dentro de transações `readOnly`, evitando acesso a proxies Hibernate fora da sessão.

---

## 🐳 Executando com Docker

### Pré-requisitos

- Docker Desktop
- Docker Compose

Clone o repositório:

```bash
git clone https://github.com/juceliocoelho2022/rotacerta.git
cd rotacerta
```

Suba o ambiente:

```bash
docker compose up -d --build
```

Verifique os containers:

```bash
docker compose ps
```

Serviços esperados:

```text
rotacerta-postgres
rotacerta-backend
rotacerta-frontend
```

### Endereços

| Serviço | Endereço |
|---|---|
| Frontend React | `http://localhost:5173` |
| Backend Spring Boot | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Actuator Health | `http://localhost:8080/actuator/health` |
| PostgreSQL | `localhost:5432` |

Teste de saúde:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Resposta esperada:

```text
status
------
UP
```

---

## 💻 Executando em desenvolvimento

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Crie um arquivo `.env` no frontend quando necessário:

```env
VITE_API_URL=http://localhost:8080
```

---

## 📁 Estrutura do repositório

```text
rotacerta/
│
├── app/                         # Aplicativo Android do entregador
│
├── backend/                     # Java 21 + Spring Boot
│   ├── src/main/java/com/rotacerta/api/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                    # React + TypeScript + Vite
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── styles/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── database/
│   └── init.sql
│
├── docker-compose.yml
├── ARCHITECTURE.md
└── README.md
```

---

## ✅ Status atual

### Implementado

- [x] Frontend React + TypeScript
- [x] Dashboard operacional
- [x] Integração React → Spring Boot
- [x] Backend Java 21 + Spring Boot
- [x] API REST
- [x] Spring Data JPA
- [x] PostgreSQL
- [x] Flyway
- [x] Rastreamento por código
- [x] Histórico de eventos
- [x] Atualização de status
- [x] Swagger/OpenAPI
- [x] Spring Boot Actuator
- [x] Dockerfile backend
- [x] Dockerfile frontend
- [x] Docker Compose
- [x] Aplicativo Android em evolução

### Roadmap

- [ ] Spring Security + JWT
- [ ] Perfis `ADMIN`, `CUSTOMER` e `DRIVER`
- [ ] Apache Kafka para eventos de entrega
- [ ] Notificações assíncronas
- [ ] Redis para cache
- [ ] Prometheus + Grafana
- [ ] OpenTelemetry
- [ ] Testcontainers
- [ ] CI/CD completo para Web + Backend
- [ ] Integração definitiva do Android com a API
- [ ] Google Maps / Maps Compose
- [ ] Rastreamento em tempo real
- [ ] Comprovante de entrega por QR Code/PIN/foto

---

## 🧠 Conceitos de Engenharia de Software aplicados

- Arquitetura em camadas
- DTO Pattern
- Repository Pattern
- Injeção de dependência
- API REST
- HTTP / JSON
- Transações com Spring
- Lazy Loading com JPA/Hibernate
- Versionamento de banco com Flyway
- Containerização
- Configuração por variáveis de ambiente
- Separação entre frontend e backend
- Conventional Commits
- Evolução incremental por sprints

---

## 👨‍💻 Autor

**Jucelio Farias Coelho**

Projeto de portfólio e estudo aplicado em **Desenvolvimento Full Stack, Java Backend, Android, Banco de Dados, Docker e Engenharia de Software**.

GitHub: [juceliocoelho2022](https://github.com/juceliocoelho2022)

---

### RotaCerta

**Tecnologia conectando cada etapa da entrega — do pedido ao cliente final.**
