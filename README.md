# RotaCerta 🚚📦

> **Smart E-commerce Logistics & Delivery Platform**

[![Platform CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/platform-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/platform-ci.yml)
[![Android CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

## 📌 Visão geral

**RotaCerta** é uma plataforma Full Stack para gestão de pedidos, clientes, entregas, despacho inteligente, rotas e rastreamento de última milha.

O projeto simula o fluxo operacional de um e-commerce desde a criação do pedido até a confirmação da entrega, mantendo separadas três responsabilidades importantes:

```text
Pedido   = demanda comercial e logística
Entrega  = execução operacional
Rota     = deslocamento e sequência de paradas
```

A solução integra **React + TypeScript**, **Java 21 + Spring Boot**, **PostgreSQL 17**, **Flyway**, **Docker Compose** e um aplicativo Android em evolução com **Kotlin + Jetpack Compose**.

O objetivo é demonstrar, em um único projeto de portfólio, conceitos de **engenharia de software, APIs REST, modelagem de domínio, persistência, logística, otimização operacional, rastreamento e experiência do cliente**.

---

## ✨ Principais módulos

### 🧭 Dashboard executivo

Painel central com visão operacional dos pedidos e entregas:

- total de pedidos;
- pedidos em preparação;
- entregas em trânsito;
- pedidos que saíram para entrega;
- entregas concluídas;
- falhas e ocorrências;
- indicadores derivados de dados reais da API.

### 👥 Customer Experience

Módulo `/customers` com gestão real de clientes:

- cadastro de cliente;
- telefone, e-mail, status e avaliação;
- múltiplos endereços;
- endereço principal;
- pessoas autorizadas a receber;
- histórico de pedidos;
- preferências de entrega;
- canal de notificação;
- janela preferencial de recebimento;
- instruções de entrega;
- KPIs por cliente;
- painel lateral operacional.

### 🛒 Pedidos

Módulo `/orders` com centro operacional e criação completa de pedidos.

Recursos implementados:

- KPIs reais da API;
- busca por pedido, cliente ou código de rastreio;
- filtros por status, prioridade e modalidade;
- prioridades `NORMAL`, `HIGH` e `URGENT`;
- modalidades `STANDARD`, `EXPRESS`, `SAME_DAY` e `SCHEDULED`;
- múltiplos itens por pedido;
- quantidade, preço, peso e volume por item;
- cálculo de valor total, peso total, volume total e volumes;
- snapshot do endereço de entrega;
- data e janela de entrega por pedido;
- instruções herdadas das preferências do cliente;
- painel lateral com visão detalhada do pedido;
- integração com RotaCerta Live quando o pedido está `OUT_FOR_DELIVERY`.

#### Wizard de criação

```text
Cliente
   ↓
Produtos
   ↓
Endereço
   ↓
Entrega
   ↓
Revisão
   ↓
Criar pedido
```

A criação é conectada diretamente a `POST /api/orders` e o novo pedido entra no fluxo com status `ORDER_CREATED`.

### 🚚 Entregas

Módulo `/deliveries` para acompanhamento operacional:

- pedidos elegíveis à operação logística;
- filtros e monitoramento;
- atribuição de motorista;
- recálculo operacional;
- acompanhamento do status da entrega;
- integração com o Smart Dispatch.

### 🗺️ Rotas

Módulo `/routes` para visualizar e otimizar as paradas de cada motorista:

- rota atual;
- sequência de paradas;
- distância estimada;
- tempo estimado;
- comparação antes/depois da otimização;
- aplicação da nova sequência;
- representação visual operacional da rota.

> O cálculo atual utiliza coordenadas persistidas, distância de Haversine, velocidade média configurada e carga do motorista. **Trânsito externo em tempo real ainda não está integrado.**

### ⚡ Smart Dispatch

Motor de despacho para recomendar e atribuir pedidos a motoristas disponíveis.

O cálculo considera atualmente:

- distância entre motorista e destino;
- capacidade e carga atual;
- prioridade do pedido/local;
- SLA operacional;
- ETA estimado;
- risco operacional;
- disponibilidade do motorista.

Fluxo:

```text
Pedido elegível
      ↓
Localização de entrega
      ↓
Motoristas disponíveis
      ↓
Score de despacho
      ↓
Melhor candidato
      ↓
Atribuição
      ↓
Rota do motorista
      ↓
Otimização das paradas
```

### 📍 RotaCerta Live

Quando a encomenda sai para entrega, o sistema pode gerar um link público temporário para acompanhamento.

```text
OUT_FOR_DELIVERY
      ↓
Sessão Live
      ↓
Token público temporário
      ↓
/live/{token}
      ↓
Timeline da entrega
      ↓
Recebedor alternativo
```

Recursos atuais:

- token público aleatório;
- expiração automática;
- timeline da entrega;
- atualização periódica da página;
- autorização de familiar, vizinho, porteiro ou terceiro;
- instruções para o entregador;
- encerramento da sessão quando o fluxo termina;
- persistência no PostgreSQL.

Documentação técnica: [`docs/ROTACERTA_LIVE.md`](docs/ROTACERTA_LIVE.md)

---

## 🧱 Stack tecnológica

### Frontend

- React 19
- TypeScript
- Vite
- React Router
- Axios
- Lucide React
- CSS responsivo
- Nginx

### Backend

- Java 21
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Transactions
- Spring Boot Actuator
- Hibernate ORM
- Maven
- Swagger / OpenAPI

### Banco de dados

- PostgreSQL 17
- Flyway
- JPA / Hibernate
- migrations versionadas
- constraints e índices de suporte operacional

### Infraestrutura e CI

- Docker
- Docker Compose
- Dockerfiles multi-stage
- Nginx
- healthcheck do PostgreSQL
- GitHub Actions
- Platform CI para backend e frontend
- Android CI

### Mobile

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- CameraX
- ML Kit Barcode Scanning
- ViewModel + StateFlow
- JUnit / MockK

---

## 🏗️ Arquitetura

```text
┌─────────────────────────────────────────┐
│              React + Vite               │
│ Dashboard • Clientes • Pedidos          │
│ Entregas • Rotas • RotaCerta Live      │
└──────────────────┬──────────────────────┘
                   │ REST / JSON
                   ▼
┌─────────────────────────────────────────┐
│          Spring Boot API / Java 21      │
├─────────────────────────────────────────┤
│ Controllers                             │
│ Services / regras de negócio            │
│ DTOs + Validation                       │
│ Spring Data Repositories                │
│ Transaction Management                  │
│ Smart Dispatch / Route Engine           │
└──────────────────┬──────────────────────┘
                   │ JPA / Hibernate
                   ▼
┌─────────────────────────────────────────┐
│              PostgreSQL 17              │
│           Flyway Migrations             │
└─────────────────────────────────────────┘

Docker Compose orquestra frontend, backend e banco.
```

---

## 🔄 Fluxo de negócio

```text
Cliente
  ↓
Endereço + preferência de recebimento
  ↓
Pedido
  ↓
Itens + prioridade + modalidade
  ↓
Preparação
  ↓
Pedido elegível ao despacho
  ↓
Smart Dispatch
  ↓
Motorista
  ↓
Rota otimizada
  ↓
OUT_FOR_DELIVERY
  ↓
RotaCerta Live
  ↓
Entrega concluída
```

---

## 🔌 API REST

### Dashboard

```http
GET /api/dashboard
```

### Clientes

```http
GET  /api/customers
GET  /api/customers/{id}
POST /api/customers
PUT  /api/customers/{id}

GET  /api/customers/{id}/addresses
POST /api/customers/{id}/addresses

GET  /api/customers/{id}/authorized-recipients
POST /api/customers/{id}/authorized-recipients

GET /api/customers/{id}/preferences
PUT /api/customers/{id}/preferences

GET /api/customers/{id}/orders
```

### Pedidos

```http
GET  /api/orders
GET  /api/orders/{id}
GET  /api/orders/{id}/detail
POST /api/orders
```

Exemplo conceitual de criação:

```json
{
  "customerId": 1,
  "addressId": 1,
  "priority": "HIGH",
  "deliveryType": "SCHEDULED",
  "deliveryDate": "2026-08-30",
  "windowStart": "14:00:00",
  "windowEnd": "18:00:00",
  "items": [
    {
      "productName": "Notebook",
      "quantity": 1,
      "unitPrice": 4299.00,
      "weightKg": 2.8,
      "volumeM3": 0.015
    }
  ]
}
```

### Entregas

```http
PATCH /api/deliveries/{id}/status
POST  /api/deliveries/{id}/confirm
POST  /api/deliveries/{id}/failure
```

### Smart Dispatch

```http
GET   /api/dispatch/monitoring
POST  /api/dispatch/auto-plan
POST  /api/dispatch/orders/{orderId}/assign
GET   /api/dispatch/orders/{orderId}
GET   /api/dispatch/drivers/{driverId}/route
POST  /api/dispatch/drivers/{driverId}/route/optimize
POST  /api/dispatch/drivers/{driverId}/route/apply
PATCH /api/dispatch/drivers/{driverId}/location
```

### Rastreamento

```http
GET /api/tracking/{trackingCode}
```

### RotaCerta Live

```http
POST /api/deliveries/{id}/live-link
GET  /api/public/live/{token}
POST /api/public/live/{token}/recipient
```

---

## 🗄️ Modelo de dados

Domínios principais:

```text
Customer
 ├── CustomerAddress
 ├── AuthorizedRecipient
 ├── DeliveryPreference
 └── Order
      ├── OrderItem
      ├── OrderDeliveryDetails
      ├── TrackingEvent
      ├── LiveTrackingSession
      ├── DeliveryLocation
      └── DeliveryAssignment
             └── Driver
```

Tabelas relevantes:

```text
customers
customer_addresses
authorized_recipients
delivery_preferences
orders
order_items
order_delivery_details
tracking_events
delivery_tracking_sessions
delivery_locations
delivery_assignments
drivers
flyway_schema_history
```

A criação de um pedido grava um **snapshot do endereço e da janela de entrega**, evitando que alterações futuras no cadastro do cliente mudem o histórico logístico do pedido.

---

## 🧬 Flyway

O banco evolui por migrations versionadas.

```text
V1  create schema
V2  seed demo data
V3  live tracking sessions
V4  smart dispatch
V5  delivery operations metadata
V6  driver vehicle metadata
V7  route sequence
V8  customer experience
V9  order operations
```

A migration `V9__create_order_operations.sql` adiciona prioridade, modalidade, itens e snapshot de entrega aos pedidos, além de realizar backfill compatível para os registros anteriores.

---

## 🐳 Executando localmente

### Pré-requisitos

- Docker Desktop
- Docker Compose
- Git

Clone:

```bash
git clone https://github.com/juceliocoelho2022/rotacerta.git
cd rotacerta
```

Suba o ambiente:

```bash
docker compose up -d --build
```

Verifique:

```bash
docker compose ps
```

Serviços esperados:

```text
rotacerta-postgres   healthy
rotacerta-backend    running
rotacerta-frontend   running
```

O Spring Boot pode levar alguns segundos após o container entrar em `Up`. Aguarde a inicialização antes de testar o Actuator.

```powershell
Start-Sleep -Seconds 15
Invoke-RestMethod http://localhost:8080/actuator/health
```

Resposta esperada:

```text
status
------
UP
```

### URLs locais

| Serviço | Endereço |
|---|---|
| Dashboard | `http://localhost:5173/` |
| Clientes | `http://localhost:5173/customers` |
| Pedidos | `http://localhost:5173/orders` |
| Entregas | `http://localhost:5173/deliveries` |
| Rotas | `http://localhost:5173/routes` |
| Backend | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Actuator | `http://localhost:8080/actuator/health` |
| PostgreSQL | `localhost:5432` |

---

## 📁 Estrutura

```text
rotacerta/
│
├── app/                         # Android / Kotlin / Compose
├── backend/                     # Java 21 + Spring Boot
│   ├── src/main/java/com/rotacerta/api/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── src/main/resources/db/migration/
│
├── frontend/                    # React + TypeScript + Vite
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       └── styles/
│
├── database/
├── docs/
│   └── ROTACERTA_LIVE.md
│
├── docker-compose.yml
└── README.md
```

---

## ✅ Status atual

### Implementado e validado

- [x] Frontend React + TypeScript
- [x] Backend Java 21 + Spring Boot
- [x] PostgreSQL + Flyway
- [x] Docker Compose
- [x] Swagger / OpenAPI
- [x] Actuator
- [x] Dashboard executivo
- [x] Customer Experience
- [x] Cadastro de clientes
- [x] Múltiplos endereços
- [x] Recebedores autorizados
- [x] Preferências e janela de entrega
- [x] Centro operacional de Pedidos
- [x] Wizard de novo pedido
- [x] Criação real de pedido pela interface
- [x] Itens, prioridade e modalidade de entrega
- [x] Snapshot do destino
- [x] Gestão operacional de Entregas
- [x] Smart Dispatch
- [x] Route Engine com otimização de sequência
- [x] Rastreamento por código
- [x] Histórico de eventos
- [x] RotaCerta Live
- [x] CI de frontend e backend
- [x] Aplicativo Android em evolução

### Em evolução

- [ ] integrar pedidos recém-criados ao Smart Dispatch de forma automática/controlada
- [ ] utilizar janela de entrega como restrição explícita no Route Engine
- [ ] tela operacional completa de Motoristas
- [ ] gestão de Frota/Veículos
- [ ] prova de entrega com recebedor, data/hora e evidência
- [ ] GPS em tempo real do motorista
- [ ] tráfego externo e ETA dinâmico
- [ ] notificações reais por e-mail/SMS/WhatsApp
- [ ] Spring Security + JWT
- [ ] perfis `ADMIN`, `CUSTOMER` e `DRIVER`
- [ ] Kafka para eventos logísticos
- [ ] Redis
- [ ] Prometheus + Grafana
- [ ] OpenTelemetry
- [ ] Testcontainers

---

## 🧠 Conceitos aplicados

- arquitetura em camadas;
- modelagem orientada ao domínio;
- DTO Pattern;
- Repository Pattern;
- injeção de dependência;
- API REST;
- validação de entrada;
- transações Spring;
- JPA/Hibernate;
- Flyway Database Migrations;
- snapshots para preservação histórica;
- cálculo de distância geográfica com Haversine;
- heurística de despacho;
- otimização de sequência de paradas;
- containerização;
- configuração por variáveis de ambiente;
- SPA com React;
- CI com GitHub Actions;
- Conventional Commits;
- evolução incremental por branches e pull requests.

---

## 👨‍💻 Autor

**Jucelio Farias Coelho**

Projeto de portfólio e estudo aplicado em **Java Backend, Desenvolvimento Full Stack, Android, Banco de Dados, Docker, Logística e Engenharia de Software**.

GitHub: [juceliocoelho2022](https://github.com/juceliocoelho2022)

---

### RotaCerta

**Tecnologia conectando cada etapa da entrega — do pedido ao cliente final.**
