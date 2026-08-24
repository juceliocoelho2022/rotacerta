# RotaCerta 🛵📦

**RotaCerta** é uma plataforma de logística last-mile em desenvolvimento para entregadores de e-commerce, marketplaces, restaurantes, transportadoras e operações independentes.

O objetivo é permitir que o entregador **escaneie encomendas**, organize as paradas e execute a rota de entrega com mais eficiência. A evolução do projeto inclui **otimização inteligente de rotas**, **rastreamento em tempo real** e acompanhamento da encomenda pelo remetente e destinatário.

## 🎯 Problema que o projeto resolve

Entregadores que trabalham com várias encomendas precisam transformar uma lista de endereços em uma sequência prática de entregas. Fazer isso manualmente aumenta deslocamentos, tempo de rota e risco de erro.

O RotaCerta nasce para concentrar esse fluxo em um único aplicativo.

## ✅ Funcionalidades já implementadas

- Leitura de QR Code e códigos de barras com **CameraX + ML Kit**.
- Interpretação de endereço em JSON, texto estruturado e texto livre.
- Cadastro das entregas lidas pelo scanner.
- Organização automática das paradas.
- Tela de rota do dia.
- Controle de entregas pendentes, concluídas e não entregues.
- Próxima parada em destaque.
- Histórico da sessão.
- Status online/offline do entregador.
- Interface Android moderna com **Kotlin + Jetpack Compose + Material 3**.
- Injeção de dependência com **Hilt**.
- Testes unitários para regras principais do domínio.

## 🚀 Visão do produto

Fluxo pretendido:

```text
Escanear encomendas
        ↓
Identificar destinatários e endereços
        ↓
Montar lista de paradas
        ↓
Otimizar a sequência de entregas
        ↓
Iniciar rota
        ↓
Compartilhar localização durante a entrega
        ↓
Confirmar entrega
        ↓
Atualizar rastreamento do pedido
```

## 🗺️ Próximas evoluções

- Google Maps SDK / Maps Compose.
- Geocodificação de endereços.
- Otimização real de múltiplas paradas.
- Cálculo baseado em distância, tempo, trânsito, prioridade e janela de entrega.
- Persistência local com Room.
- Backend em Java + Spring Boot.
- PostgreSQL + PostGIS.
- API REST para pedidos, rotas e entregadores.
- Autenticação com Spring Security + JWT.
- Rastreamento em tempo real durante rota ativa.
- Portal web para remetente e destinatário.
- Comprovante de entrega por PIN, assinatura ou foto.
- Notificações de status da encomenda.
- Dashboard operacional.

> O projeto não depende de acesso não autorizado a plataformas de terceiros. Integrações com marketplaces e serviços de entrega deverão utilizar APIs oficiais ou dados fornecidos de forma autorizada.

## 🧠 Otimização de rotas

A evolução do RotaCerta pretende tratar a roteirização como um problema de logística, aproximando-se de cenários de **Vehicle Routing Problem (VRP)**.

Critérios previstos:

```text
custo da rota = distância + tempo + trânsito + prioridade + restrições
```

Isso permite evoluir o projeto de um simples mapa de entregas para uma solução de engenharia de software aplicada à logística.

## 🧱 Stack atual

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- CameraX
- ML Kit Barcode Scanning
- ViewModel
- JUnit / MockK

### Stack planejada para a plataforma

- Java
- Spring Boot
- Spring Security
- REST API
- WebSocket
- PostgreSQL
- PostGIS
- React
- Docker

## 🏗️ Estrutura atual

```text
app/src/main/java/com/jucelio/rotacerta/
├── MainActivity.kt
├── RotaCertaApplication.kt
├── domain/
│   ├── model/delivery/
│   └── usecase/delivery/
└── ui/
    ├── RotaCertaApp.kt
    ├── theme/
    └── delivery/
```

A arquitetura será expandida gradualmente para separar apresentação, domínio, dados, integrações e serviços externos.

## 📥 Formatos aceitos pelo scanner

Exemplo JSON:

```json
{
  "tipo": "ecommerce",
  "nome": "Cliente Exemplo",
  "endereco": "Rua Exemplo, 123",
  "bairro": "Centro",
  "cidade": "São Paulo",
  "cep": "01000-000",
  "pedido": "RC-0001"
}
```

Também são aceitos:

- linhas no formato `Chave: valor`;
- valores delimitados por `|` ou `;`;
- texto livre usado como endereço.

## ▶️ Como executar

Requisitos:

- Android Studio
- JDK 17
- Android SDK 35

No terminal do projeto:

```bash
./gradlew assembleDebug
./gradlew test
```

No Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

## 📌 Status

**Fase atual:** MVP Android / estruturação do produto.

O projeto será desenvolvido de forma incremental, priorizando código executável, testes e evolução arquitetural em etapas pequenas.

## 👨‍💻 Autor

**Jucelio Farias Coelho**

Projeto de portfólio focado em desenvolvimento Android, backend Java, arquitetura de software, dados e logística.
