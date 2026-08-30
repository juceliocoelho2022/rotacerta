<div align="center">
<img src="docs/assets/rotacerta-banner-top.jpg" alt="RotaCerta Smart Logistics Platform" width="100%"><img src="docs/assets/rotacerta-banner-bottom.jpg" alt="RotaCerta Smart Logistics Platform" width="100%">
</div>

<div align="center">

# 🚚 RotaCerta

### Smart Logistics, Delivery Operations & Drone Simulation Platform

**Uma plataforma Full Stack para gestão de pedidos, clientes, entregas, motoristas, veículos, rotas, ocorrências, relatórios, configurações e simulação operacional de entregas por drone.**

[![Platform CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/platform-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/platform-ci.yml)
[![Android CI](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml/badge.svg)](https://github.com/juceliocoelho2022/rotacerta/actions/workflows/android-ci.yml)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF)

**Java 21 · Spring Boot · React · TypeScript · PostgreSQL · Flyway · Docker · Kotlin · Jetpack Compose**

</div>

---

## 📌 Sobre o projeto

O **RotaCerta** é uma plataforma de logística construída como projeto de engenharia de software e portfólio técnico. A aplicação modela o ciclo de uma operação de entrega, da criação do pedido até a entrega ao cliente, combinando operação terrestre, rastreamento e um módulo de drone estritamente em modo de simulação.

O projeto foi estruturado para demonstrar aplicação prática de:

- arquitetura em camadas;
- modelagem de domínio;
- APIs REST;
- persistência relacional;
- regras de negócio;
- despacho inteligente;
- roteirização;
- rastreabilidade;
- audit trail;
- indicadores operacionais;
- CI com GitHub Actions;
- containerização com Docker.

### Domínio principal

```text
Pedido      = demanda comercial e logística
Entrega     = execução operacional
Rota        = sequência de deslocamento e paradas
Motorista   = responsável pela execução terrestre
Veículo     = recurso operacional da frota
Ocorrência  = exceção operacional auditável
Missão      = execução aérea simulada vinculada a um pedido
```

---

## ⭐ Destaques técnicos

| Área | Implementação |
|---|---|
| Backend | Java 21 + Spring Boot 3.5.5 |
| Frontend | React 19 + TypeScript + Vite |
| Banco | PostgreSQL 17 + Flyway |
| Infra | Docker + Docker Compose + Nginx |
| Mobile | Kotlin + Jetpack Compose |
| CI | GitHub Actions |
| Dispatch | Score por distância, carga, prioridade, SLA e risco |
| Rotas | Haversine + otimização de sequência |
| Drone | Elegibilidade, autorização auditável, timeline e simulação de voo |
| Observabilidade | Spring Boot Actuator |

---

## 🧩 Módulos implementados

### 🧭 Dashboard Executivo

Visão consolidada da operação com indicadores de pedidos, entregas, falhas e andamento logístico.

### 👥 Clientes — Customer Experience

- cadastro e gestão de clientes;
- múltiplos endereços;
- endereço principal;
- recebedores autorizados;
- preferências de entrega;
- janela preferencial;
- instruções de entrega;
- histórico de pedidos;
- KPIs por cliente.

### 🛒 Pedidos

- criação completa de pedidos;
- múltiplos itens;
- preço, peso e volume;
- prioridades `NORMAL`, `HIGH` e `URGENT`;
- modalidades `STANDARD`, `EXPRESS`, `SAME_DAY` e `SCHEDULED`;
- snapshot do endereço;
- data e janela de entrega;
- integração com Smart Dispatch.

Fluxo operacional:

```text
ORDER_CREATED
    ↓
PAYMENT_APPROVED
    ↓
PICKING
    ↓
PACKING
    ↓
READY_FOR_SHIPMENT
    ↓
SHIPPED
    ↓
IN_TRANSIT
    ↓
OUT_FOR_DELIVERY
    ↓
DELIVERED
```

### 🚚 Entregas

- atualização controlada de status;
- confirmação de entrega;
- registro de falha;
- tracking integrado;
- geração de link público temporário.

### ⚡ Smart Dispatch

O motor de despacho considera:

```text
Distância
+ disponibilidade
+ carga/capacidade
+ prioridade
+ modalidade de entrega
+ SLA
+ janela de entrega
+ risco operacional
= score do melhor motorista
```

### 🗺️ Rotas

- sequência de paradas;
- distância estimada;
- ETA;
- comparação antes/depois da otimização;
- aplicação da rota otimizada.

> O cálculo atual utiliza coordenadas persistidas e Haversine. Trânsito externo em tempo real ainda não está integrado.

### 👨‍✈️ Motoristas

- cadastro operacional;
- disponibilidade;
- capacidade atual e máxima;
- localização persistida;
- veículo vinculado;
- foto de perfil persistida;
- acesso a rota e entregas.

### 🚐 Veículos

- cadastro da frota;
- placa e modelo;
- tipo de veículo;
- capacidade;
- odômetro;
- combustível;
- manutenção programada;
- vínculo com motorista;
- status operacional.

Estados suportados:

```text
AVAILABLE
IN_OPERATION
MAINTENANCE
OUT_OF_SERVICE
```

### ⚠️ Ocorrências

Centro operacional de exceções com:

- vínculo opcional com pedido, motorista e veículo;
- severidades `LOW`, `MEDIUM`, `HIGH` e `CRITICAL`;
- categorias operacionais;
- descrição, localização e resolução;
- histórico de abertura e encerramento.

Fluxo:

```text
OPEN → IN_PROGRESS → RESOLVED → CLOSED
```

### 📊 Relatórios

Painel executivo consolidando dados reais de:

- pedidos;
- entregas;
- receita entregue;
- motoristas;
- veículos;
- drones;
- ocorrências;
- distribuição dos pedidos por status.

### ⚙️ Configurações

Configurações persistidas em PostgreSQL para parâmetros como:

- despacho automático;
- SLA padrão;
- RotaCerta Live;
- Drone Simulation;
- canal padrão de notificação;
- alerta de manutenção;
- moeda dos relatórios.

### 📍 RotaCerta Live

Link público temporário para acompanhamento da entrega:

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

### 🚁 Drone Delivery — `SIMULATION_ONLY`

O módulo aéreo foi desenvolvido como **simulador operacional seguro**.

Recursos implementados:

- análise de elegibilidade do pedido;
- peso real da carga;
- alcance e bateria;
- seleção do drone compatível;
- criação e reserva da missão;
- Authorization Audit Trail;
- evidências de autorização;
- fingerprint SHA-256 do contexto;
- Mission Timeline persistida;
- sincronização com o status do pedido;
- Flight Control Center;
- posição simulada por interpolação;
- progresso, distância restante e ETA.

Fluxo da missão:

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

> **Importante:** o projeto não controla drones físicos, não utiliza GPS real no módulo aéreo e não representa aprovação regulatória real de ANAC, DECEA ou ANATEL.

---

## 🏗️ Arquitetura

```text
┌──────────────────────────────────────────────────────┐
│                React 19 + TypeScript                 │
│                                                      │
│ Dashboard • Clientes • Pedidos • Entregas • Rotas   │
│ Motoristas • Veículos • Ocorrências • Relatórios    │
│ Configurações • Drones • Flight Control • Live      │
└──────────────────────────┬───────────────────────────┘
                           │ REST / JSON
                           ▼
┌──────────────────────────────────────────────────────┐
│              Spring Boot API · Java 21               │
│                                                      │
│ Controllers • Services • DTOs • Validation           │
│ JPA/Hibernate • Transactions • Smart Dispatch        │
│ Route Engine • Drone Mission Orchestration           │
│ Mission Timeline • Authorization Audit               │
│ Flight Simulation • Reports                          │
└──────────────────────────┬───────────────────────────┘
                           │ JPA / Hibernate
                           ▼
┌──────────────────────────────────────────────────────┐
│              PostgreSQL 17 + Flyway                  │
└──────────────────────────────────────────────────────┘

Android App
Kotlin • Jetpack Compose • CameraX • ML Kit • Hilt
```

---

## 🗄️ Banco de dados e migrations

O banco é versionado por Flyway.

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
V14  operations suite
```

A V14 adiciona os domínios:

```text
vehicles
incidents
system_settings
```

---

## 🔌 Principais endpoints

### Clientes e pedidos

```http
GET  /api/customers
POST /api/customers
GET  /api/orders
POST /api/orders
GET  /api/orders/{id}/detail
```

### Operação terrestre

```http
GET   /api/drivers
GET   /api/vehicles
POST  /api/vehicles
GET   /api/dispatch/monitoring
POST  /api/dispatch/auto-plan
GET   /api/dispatch/drivers/{driverId}/route
POST  /api/dispatch/drivers/{driverId}/route/optimize
```

### Ocorrências, relatórios e configurações

```http
GET   /api/incidents
POST  /api/incidents
PATCH /api/incidents/{id}/status
GET   /api/reports/operations
GET   /api/settings
PUT   /api/settings/{key}
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

### RotaCerta Live

```http
GET  /api/tracking/{trackingCode}
POST /api/deliveries/{id}/live-link
GET  /api/public/live/{token}
POST /api/public/live/{token}/recipient
```

---

## 🐳 Como executar

### Pré-requisitos

- Docker Desktop
- Docker Compose
- Git

```bash
git clone https://github.com/juceliocoelho2022/rotacerta.git
cd rotacerta
docker compose up -d --build
```

Aguarde a inicialização do backend e valide:

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

| Módulo | URL |
|---|---|
| Dashboard | `http://127.0.0.1:5173/` |
| Clientes | `http://127.0.0.1:5173/customers` |
| Pedidos | `http://127.0.0.1:5173/orders` |
| Entregas | `http://127.0.0.1:5173/deliveries` |
| Rotas | `http://127.0.0.1:5173/routes` |
| Motoristas | `http://127.0.0.1:5173/drivers` |
| Veículos | `http://127.0.0.1:5173/vehicles` |
| Ocorrências | `http://127.0.0.1:5173/incidents` |
| Relatórios | `http://127.0.0.1:5173/reports` |
| Configurações | `http://127.0.0.1:5173/settings` |
| Drone Delivery | `http://127.0.0.1:5173/drones` |
| Backend | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Actuator | `http://localhost:8080/actuator/health` |

---

## ✅ Status do projeto

### Implementado

- [x] Dashboard executivo
- [x] Customer Experience
- [x] Pedidos
- [x] Entregas
- [x] Smart Dispatch
- [x] Route Engine
- [x] Motoristas
- [x] Veículos
- [x] Ocorrências
- [x] Relatórios
- [x] Configurações
- [x] Rastreamento por código
- [x] RotaCerta Live
- [x] Drone Delivery `SIMULATION_ONLY`
- [x] Authorization Audit Trail
- [x] Mission Timeline
- [x] Flight Control Center
- [x] Docker Compose
- [x] GitHub Actions CI
- [x] Android em evolução

### Roadmap

- [ ] Estoque e reserva de SKU
- [ ] Spring Security + JWT
- [ ] perfis `ADMIN`, `CUSTOMER` e `DRIVER`
- [ ] Kafka para eventos logísticos
- [ ] Redis
- [ ] Prometheus + Grafana
- [ ] OpenTelemetry
- [ ] Testcontainers
- [ ] tráfego externo e ETA dinâmico
- [ ] GPS real do motorista
- [ ] notificações reais por e-mail/SMS/WhatsApp
- [ ] integrações regulatórias e meteorológicas reais para drones

---

## 🧠 Conceitos aplicados

`Java` · `Spring Boot` · `REST` · `JPA` · `Hibernate` · `PostgreSQL` · `Flyway` · `React` · `TypeScript` · `Docker` · `DTO Pattern` · `Repository Pattern` · `Domain Modeling` · `Transactions` · `Haversine` · `Geographic Interpolation` · `Audit Trail` · `SHA-256` · `Route Optimization` · `CI/CD`

---

## 👨‍💻 Autor

**Jucelio Farias Coelho**

Projeto desenvolvido para estudo aplicado e portfólio em **Java Backend, Full Stack Development, Android, Banco de Dados, Docker, Logística e Engenharia de Software**.

GitHub: [@juceliocoelho2022](https://github.com/juceliocoelho2022)

---

<div align="center">

### RotaCerta

**Tecnologia conectando cada etapa da operação logística — do pedido à entrega final.**

</div>