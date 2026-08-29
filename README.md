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

## 📌 Sobre o projeto

**RotaCerta** é uma plataforma Full Stack para gestão logística de pedidos de e-commerce, rastreamento de entregas e acompanhamento operacional do fluxo entre loja, centro de distribuição, transportadora, entregador e cliente final.

A solução integra **React + TypeScript**, **Java 21 + Spring Boot**, **PostgreSQL**, **Flyway**, **Docker Compose** e um aplicativo Android em evolução com **Kotlin + Jetpack Compose**.

O principal diferencial atual é o **RotaCerta Live**: quando uma encomenda sai para entrega, o sistema gera um link público temporário e seguro para o cliente acompanhar o pedido e autorizar previamente outra pessoa — familiar, vizinho, porteiro ou terceiro — caso não esteja no endereço. O objetivo é reduzir tentativas de entrega frustradas, reentregas e custo operacional.

---

## 🚀 RotaCerta Live

Fluxo implementado e validado localmente:

```text
Pedido OUT_FOR_DELIVERY
        ↓
Spring Boot cria sessão Live
        ↓
Token público temporário
        ↓
Cliente acessa /live/{token}
        ↓
Acompanha timeline da entrega
        ↓
Autoriza outro recebedor
        ↓
Spring Boot valida a solicitação
        ↓
PostgreSQL persiste a instrução
        ↓
Página Live exibe o recebedor autorizado
```

### Recursos atuais

- link público sem expor o ID sequencial do pedido;
- token aleatório de alta entropia;
- expiração automática em 48 horas;
- encerramento da sessão quando a entrega termina, falha, é devolvida ou cancelada;
- página pública responsiva em `/live/:token`;
- timeline dos eventos logísticos;
- atualização automática a cada 30 segundos;
- autorização de familiar, vizinho, porteiro ou outra pessoa;
- campo de instruções para a entrega;
- persistência no PostgreSQL;
- botão **Abrir Live** no painel operacional;
- CI para frontend e backend.

Exemplo de uso:

```text
Cliente não estará em casa
        ↓
Autoriza: Maria Souza
Relação: Vizinho
Instrução: Pode receber a encomenda na casa ao lado
        ↓
Autorização registrada com sucesso
```

Documentação técnica: [`docs/ROTACERTA_LIVE.md`](docs/ROTACERTA_LIVE.md)

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
- **Nginx**

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
- **Flyway**
- **JPA / Hibernate**
- índices para status e código de rastreamento
- migrations versionadas

### DevOps e infraestrutura

- **Docker**
- **Docker Compose**
- **Nginx**
- Dockerfile multi-stage
- healthcheck do PostgreSQL
- variáveis de ambiente
- **GitHub Actions / Platform CI**

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
│     RotaCerta Live       │
└────────────┬─────────────┘
             │ REST / JSON
             ▼
┌──────────────────────────┐
│     Spring Boot API      │
│        Java 21           │
├──────────────────────────┤
│ Controller               │
│ Service                  │
│ Repository               │
│ DTO / Validation         │
│ Transaction Management   │
└────────────┬─────────────┘
             │ Spring Data JPA
             ▼
┌──────────────────────────┐
│      PostgreSQL 17       │
│   Flyway Migrations      │
└──────────────────────────┘

Docker Compose orquestra frontend, backend e banco.
```

### Fluxo logístico

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
RotaCerta Live
      ↓
Entregue
```

---

## 🖥️ Funcionalidades Web

### Dashboard operacional

- total de pedidos;
- pedidos em separação;
- pedidos em transporte;
- pedidos que saíram para entrega;
- entregas concluídas;
- falhas de entrega;
- últimos pedidos e seus status.

### Gestão de pedidos

- listagem de pedidos;
- cliente e valor da compra;
- código de rastreamento;
- atualização do status logístico;
- geração do link RotaCerta Live.

### Rastreamento tradicional

O cliente ou operador pode consultar uma entrega pelo código de rastreamento e visualizar a timeline completa de eventos.

Código de demonstração:

```text
RC-2026-SP-8F29A73
```

### RotaCerta Live

O cliente recebe uma experiência dedicada para uma entrega em andamento, podendo acompanhar os eventos e registrar previamente outro recebedor.

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

### RotaCerta Live

```http
POST /api/deliveries/{id}/live-link
GET  /api/public/live/{token}
POST /api/public/live/{token}/recipient
```

Exemplo de autorização:

```json
{
  "name": "Maria Souza",
  "relationship": "Vizinho",
  "instructions": "Pode receber a encomenda na casa ao lado."
}
```

---

## 🗄️ Modelo de dados

```text
Customer
   │
   └── 1:N ── Order
                │
                ├── 1:N ── TrackingEvent
                │
                └── 1:N ── DeliveryTrackingSession
```

Principais tabelas:

```text
customers
orders
tracking_events
delivery_tracking_sessions
flyway_schema_history
```

O relacionamento `Order -> Customer` utiliza carregamento `LAZY`, com serviços de leitura executados dentro de transações `readOnly` para evitar acesso a proxies Hibernate fora da sessão.

---

## 🐳 Executando com Docker

### Pré-requisitos

- Docker Desktop
- Docker Compose
- Git

Clone o repositório:

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

### Atenção à porta 5173

Se existir outro processo Vite local usando `localhost:5173`, ele pode interceptar o acesso ao frontend Docker. Para identificar processos na porta:

```powershell
Get-NetTCPConnection -LocalPort 5173 -State Listen | ForEach-Object {
    $processo = Get-CimInstance Win32_Process -Filter "ProcessId=$($_.OwningProcess)"
    [PSCustomObject]@{
        PID = $_.OwningProcess
        Name = $processo.Name
        CommandLine = $processo.CommandLine
    }
}
```

Encerre somente o processo Vite conflitante e mantenha o Docker Desktop ativo.

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
- [x] Dashboard operacional
- [x] Backend Java 21 + Spring Boot
- [x] API REST
- [x] Spring Data JPA
- [x] PostgreSQL 17
- [x] Flyway
- [x] Docker Compose
- [x] Swagger / OpenAPI
- [x] Spring Boot Actuator
- [x] Rastreamento por código
- [x] Histórico de eventos
- [x] Atualização de status
- [x] RotaCerta Live
- [x] Link público temporário
- [x] Timeline pública da entrega
- [x] Autorização de recebedor alternativo
- [x] Persistência da autorização no PostgreSQL
- [x] Atualização automática a cada 30 segundos
- [x] CI para frontend e backend
- [x] Aplicativo Android em evolução

### Próximas sprints

- [ ] Exibir recebedor autorizado no app Android do entregador
- [ ] PIN temporário de confirmação de entrega
- [ ] GPS do motorista
- [ ] ETA e quantidade de paradas restantes
- [ ] Mapa em tempo real com privacidade controlada
- [ ] Spring Security + JWT
- [ ] Perfis `ADMIN`, `CUSTOMER` e `DRIVER`
- [ ] Apache Kafka para eventos logísticos
- [ ] Notificações por canal autorizado
- [ ] Redis
- [ ] Prometheus + Grafana
- [ ] OpenTelemetry
- [ ] Testcontainers
- [ ] Métricas de reentregas evitadas e economia operacional

---

## 🧠 Conceitos aplicados

- Arquitetura em camadas
- DTO Pattern
- Repository Pattern
- Injeção de dependência
- API REST
- HTTP / JSON
- Spring Transaction Management
- Lazy Loading com JPA/Hibernate
- Flyway Database Migrations
- Containerização
- Configuração por variáveis de ambiente
- Single Page Application
- Token público temporário
- Separação entre frontend e backend
- CI com GitHub Actions
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
