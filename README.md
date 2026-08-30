# RotaCerta 🚚📦🚁

> **Smart E-commerce Logistics, Delivery & Drone Simulation Platform**

[![Platform CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/platform-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/platform-ci.yml)
[![Android CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

## 📌 Visão geral

**RotaCerta** é uma plataforma Full Stack para gestão de pedidos, clientes, entregas, despacho inteligente, rotas, rastreamento e simulação operacional de entregas por drone.

O projeto representa o fluxo de um e-commerce desde a criação do pedido até a confirmação da entrega e mantém separadas responsabilidades importantes do domínio:

```text
Pedido   = demanda comercial e logística
Entrega  = execução operacional
Rota     = deslocamento e sequência de paradas
Missão   = execução aérea simulada vinculada a um pedido
```

A solução integra **React + TypeScript**, **Java 21 + Spring Boot**, **PostgreSQL 17**, **Flyway**, **Docker Compose** e um aplicativo Android em evolução com **Kotlin + Jetpack Compose**.

O objetivo é demonstrar conceitos de **engenharia de software, APIs REST, modelagem de domínio, persistência, logística, otimização operacional, rastreamento, auditabilidade e experiência do cliente** em um projeto de portfólio executável.

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
- indicadores derivados da API.

### 👥 Customer Experience

Módulo `/customers` com:

- cadastro de cliente;
- telefone, e-mail, status e avaliação;
- múltiplos endereços;
- endereço principal;
- pessoas autorizadas a receber;
- histórico de pedidos;
- preferências de entrega;
- canal de notificação;
- janela preferencial;
- instruções de entrega;
- KPIs por cliente.

### 🛒 Pedidos

Módulo `/orders` com centro operacional e criação completa de pedidos.

Recursos implementados:

- busca e filtros;
- prioridades `NORMAL`, `HIGH` e `URGENT`;
- modalidades `STANDARD`, `EXPRESS`, `SAME_DAY` e `SCHEDULED`;
- múltiplos itens;
- preço, peso e volume por item;
- cálculo de total, peso, volume e volumes;
- snapshot do endereço de entrega;
- data e janela de entrega;
- instruções herdadas das preferências do cliente;
- integração com RotaCerta Live.

Fluxo de criação:

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

### 🚚 Entregas

Módulo `/deliveries` para acompanhamento operacional:

- atualização controlada de status;
- confirmação de entrega;
- registro de falha;
- integração com Smart Dispatch;
- sincronização com tracking.

### 🗺️ Rotas

Módulo `/routes` para visualizar e otimizar as paradas de motoristas:

- sequência de paradas;
- distância estimada;
- tempo estimado;
- comparação antes/depois;
- aplicação da sequência otimizada.

> O cálculo atual utiliza coordenadas persistidas, Haversine e velocidade média configurada. Trânsito externo em tempo real ainda não está integrado.

### ⚡ Smart Dispatch

Motor de despacho que considera:

- distância entre motorista e destino;
- capacidade e carga atual;
- prioridade;
- SLA;
- ETA;
- risco operacional;
- disponibilidade.

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
Otimização
```

### 📍 RotaCerta Live

Quando o pedido sai para entrega, o sistema pode gerar um link público temporário:

```text
OUT_FOR_DELIVERY
      ↓
Sessão Live
      ↓
Token temporário
      ↓
/live/{token}
      ↓
Timeline
      ↓
Recebedor alternativo
```

Recursos:

- token aleatório;
- expiração automática;
- timeline da entrega;
- atualização periódica;
- recebedor alternativo;
- instruções ao entregador;
- encerramento automático da sessão.

Documentação técnica: [`docs/ROTACERTA_LIVE.md`](docs/ROTACERTA_LIVE.md)

---

## 🚁 Drone Delivery — SIMULATION_ONLY

O módulo `/drones` implementa um protótipo operacional seguro de entrega por drone.

**Importante:** o projeto não controla drones físicos, não fornece autorização regulatória real e não substitui ANAC, DECEA, ANATEL, meteorologia, geofencing, análise de obstáculos ou avaliação de zona segura.

### Elegibilidade

Antes de criar uma missão, o backend valida:

- pedido em `READY_FOR_SHIPMENT`;
- peso real dos itens;
- coordenadas do destino;
- missão já existente para o pedido;
- drone disponível;
- capacidade de carga;
- alcance de ida e volta;
- reserva mínima de bateria.

Checks externos permanecem marcados como `PENDING_EXTERNAL`.

### Estados da missão

```text
PLANNED
   ↓
AUTHORIZED
   ↓
LOADING
   ↓
READY_FOR_TAKEOFF
   ↓
IN_FLIGHT
   ↓
APPROACHING
   ↓
LOWERING_PACKAGE
   ↓
DELIVERED
   ↓
RETURNING
   ↓
COMPLETED
```

Ao entrar em `IN_FLIGHT`, o pedido avança de `READY_FOR_SHIPMENT` para `SHIPPED`.

Ao entrar em `DELIVERED`, o orquestrador confirma automaticamente o pedido como `DELIVERED` e registra o tracking correspondente.

Ao finalizar a missão, o drone retorna para `AVAILABLE`.

### Authorization Audit Trail

A autorização simulada é auditável e registra:

- responsável;
- decisão;
- data/hora;
- validade;
- justificativa;
- versão da política;
- modo `SIMULATION_ONLY`;
- checks internos;
- checks externos pendentes;
- evidências;
- snapshot imutável do contexto;
- fingerprint SHA-256.

A aprovação exige evidência e `AUTHORIZED → LOADING` só ocorre com autorização ativa e contexto compatível.

### Mission Timeline

A migration V13 adiciona uma timeline operacional persistida para cada missão.

Eventos registrados:

```text
MISSION_CREATED
AUTHORIZATION_APPROVED / AUTHORIZATION_REJECTED
STATUS_CHANGED → LOADING
STATUS_CHANGED → READY_FOR_TAKEOFF
STATUS_CHANGED → IN_FLIGHT
STATUS_CHANGED → APPROACHING
STATUS_CHANGED → LOWERING_PACKAGE
STATUS_CHANGED → DELIVERED
STATUS_CHANGED → RETURNING
STATUS_CHANGED → COMPLETED
```

Missões antigas recebem apenas fatos históricos que podem ser reconstruídos com segurança; o sistema não inventa timestamps intermediários.

### ✈️ Flight Control Center

A tela `/drones` também possui uma **Central de Voo** com mapa operacional interno.

Recursos:

- seleção de missão;
- origem e destino;
- rota aérea representada em canvas SVG;
- posição simulada do drone;
- progresso da missão;
- progresso do trecho;
- distância restante;
- ETA restante;
- fase atual;
- atualização automática a cada 5 segundos;
- indicação explícita da fonte da posição.

A posição é calculada por **interpolação entre origem e destino** usando o estado atual da missão e o tempo desde a última transição.

```text
Missão
   ↓
DroneFlightSimulationService
   ↓
Interpolação simulada
   ↓
Latitude / longitude calculadas
   ↓
Progresso + distância restante + ETA
   ↓
REST API
   ↓
React Flight Control Center
```

Não há GPS real, telemetria física nem integração com provedor de mapa externo nesta implementação.

### 🎬 Demonstração operacional gravada

Uma demonstração em vídeo do módulo foi validada cobrindo o início do fluxo operacional:

```text
Pedido elegível
   ↓
Análise de peso + coordenadas + distância + ETA
   ↓
DR-001 recomendado
   ↓
Criação da missão simulada
   ↓
Drone reservado (`RESERVED`)
   ↓
Missão `PLANNED`
   ↓
Authorization Audit Trail
```

A gravação comprova a integração entre pedido, motor de elegibilidade, seleção de drone, reserva operacional e governança da autorização. A **Flight Control Center** complementa essa demonstração com o acompanhamento visual da rota e da posição simulada durante `IN_FLIGHT`.

---

## 🧱 Stack tecnológica

### Frontend

- React 19
- TypeScript
- Vite
- React Router
- Axios
- Lucide React
- SVG operacional
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
- constraints e índices

### Infraestrutura e CI

- Docker
- Docker Compose
- Dockerfiles multi-stage
- Nginx
- healthcheck do PostgreSQL
- GitHub Actions
- Platform CI
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
┌─────────────────────────────────────────────┐
│                React + Vite                 │
│ Dashboard • Clientes • Pedidos • Entregas  │
│ Rotas • Live • Drones • Flight Control     │
└───────────────────┬─────────────────────────┘
                    │ REST / JSON
                    ▼
┌─────────────────────────────────────────────┐
│          Spring Boot API / Java 21          │
├─────────────────────────────────────────────┤
│ Controllers                                 │
│ Services / regras de negócio                │
│ DTOs + Validation                           │
│ Spring Data Repositories                    │
│ Transaction Management                      │
│ Smart Dispatch / Route Engine               │
│ Drone Mission Orchestration                 │
│ Drone Mission Timeline                      │
│ Flight Simulation Service                   │
└───────────────────┬─────────────────────────┘
                    │ JPA / Hibernate
                    ▼
┌─────────────────────────────────────────────┐
│               PostgreSQL 17                 │
│             Flyway Migrations               │
└─────────────────────────────────────────────┘
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
GET  /api/customers/{id}/preferences
PUT  /api/customers/{id}/preferences
GET  /api/customers/{id}/orders
```

### Pedidos

```http
GET  /api/orders
GET  /api/orders/{id}
GET  /api/orders/{id}/detail
POST /api/orders
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

### Rastreamento / Live

```http
GET  /api/tracking/{trackingCode}
POST /api/deliveries/{id}/live-link
GET  /api/public/live/{token}
POST /api/public/live/{token}/recipient
```

### Drone Delivery

```http
GET   /api/drone-delivery/drones
GET   /api/drone-delivery/missions
GET   /api/drone-delivery/orders/{orderId}/eligibility
POST  /api/drone-delivery/orders/{orderId}/missions
GET   /api/drone-delivery/missions/{missionId}/authorizations
POST  /api/drone-delivery/missions/{missionId}/authorizations
GET   /api/drone-delivery/missions/{missionId}/timeline
GET   /api/drone-delivery/missions/{missionId}/flight-simulation
PATCH /api/drone-delivery/missions/{missionId}/status
```

Exemplo de resposta da simulação de voo:

```json
{
  "missionId": 3,
  "droneCode": "DR-001",
  "status": "IN_FLIGHT",
  "mode": "SIMULATION_ONLY",
  "phase": "OUTBOUND",
  "progressPercent": 42.5,
  "legProgressPercent": 37.5,
  "currentLatitude": -23.55145,
  "currentLongitude": -46.65509,
  "remainingDistanceKm": 3.71,
  "remainingEtaMinutes": 7,
  "positionSource": "SIMULATED_INTERPOLATION"
}
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
      ├── DeliveryAssignment → Driver
      └── DroneMission → Drone
             ├── DroneMissionAuthorization
             │      └── DroneAuthorizationEvidence
             └── DroneMissionEvent
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
drones
drone_missions
drone_mission_authorizations
drone_authorization_evidence
drone_mission_events
flyway_schema_history
```

---

## 🧬 Flyway

```text
V1   create schema
V2   seed demo data
V3   live tracking sessions
V4   smart dispatch
V5   delivery operations metadata
V6   driver vehicle metadata
V7   route sequence
V8   customer experience
V9   order operations
V10  driver photo
V11  drone delivery
V12  drone authorization audit
V13  drone mission timeline
```

A posição simulada do Flight Control Center é calculada em runtime e, portanto, não exige migration adicional.

---

## 🐳 Executando localmente

### Pré-requisitos

- Docker Desktop
- Docker Compose
- Git

```bash
git clone https://github.com/juceliocoelho2022/rotacerta.git
cd rotacerta
docker compose up -d --build
```

Verifique:

```bash
docker compose ps
```

O backend pode levar cerca de 20–30 segundos para concluir a inicialização local.

```powershell
Start-Sleep -Seconds 30
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
| Motoristas | `http://localhost:5173/drivers` |
| Drone Delivery / Flight Control | `http://localhost:5173/drones` |
| Backend | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Actuator | `http://localhost:8080/actuator/health` |
| PostgreSQL | `localhost:5432` |

---

## ✅ Status atual

### Implementado e validado

- [x] React + TypeScript
- [x] Java 21 + Spring Boot
- [x] PostgreSQL + Flyway
- [x] Docker Compose
- [x] Swagger / OpenAPI
- [x] Actuator
- [x] Dashboard executivo
- [x] Customer Experience
- [x] Centro operacional de Pedidos
- [x] Gestão de Entregas
- [x] Smart Dispatch
- [x] Route Engine
- [x] Rastreamento por código
- [x] RotaCerta Live
- [x] Centro operacional de Motoristas
- [x] foto de motorista persistida
- [x] Drone Delivery em `SIMULATION_ONLY`
- [x] elegibilidade de drone
- [x] Authorization Audit Trail
- [x] sincronização missão `DELIVERED` → pedido `DELIVERED`
- [x] Mission Timeline com 10 eventos em missão nova
- [x] Flight Control Center com posição interpolada
- [x] CI de frontend e backend
- [x] aplicativo Android em evolução

### Em evolução

- [ ] GPS real do motorista
- [ ] telemetria física de drone
- [ ] mapa externo / tiles geográficos
- [ ] tráfego externo e ETA dinâmico
- [ ] integrações regulatórias e meteorológicas reais para drones
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
- validação;
- transações Spring;
- JPA/Hibernate;
- Flyway;
- snapshots para preservação histórica;
- SHA-256 para fingerprint de contexto;
- audit trail;
- Haversine;
- interpolação geográfica simulada;
- heurística de despacho;
- otimização de sequência;
- containerização;
- SPA React;
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
