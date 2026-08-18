# RotaCerta 🛵

App de entregador para **iFood e encomendas**: o entregador lê o endereço do
cliente por **QR Code ou código de barras** e o app monta a **rota de entrega**,
uma parada após a outra.

## Funcionalidades

- **Ler endereço** por QR Code e pelos principais códigos de barras (Code 128,
  Code 39/93, EAN‑13/8, ITF, UPC, PDF417, Aztec, Data Matrix) via CameraX + ML Kit.
- **Montagem automática da rota** — agrupa endereços próximos (CEP / cidade /
  bairro) e ordena as paradas, deixando as concluídas ao final.
- **Rota do dia** com paradas restantes, entregues e progresso.
- **Próxima parada** em destaque, com atalho para o mapa.
- **Concluir entregas** (entregue / não entregue), reabrir e remover paradas.
- **Histórico** da sessão.
- Status **online/offline** do entregador.

## Formatos de endereço aceitos na leitura

O conteúdo lido é interpretado em vários formatos (chaves em pt/en):

1. **JSON**: `{"tipo":"ifood","nome":"João","endereco":"Rua A, 123","bairro":"Centro","cidade":"Natal","cep":"59000-000","pedido":"#4821"}`
2. **Linhas** `Chave: valor` (Tipo, Nome, Endereço, Bairro, Cidade, CEP…)
3. **Delimitado** por `|` ou `;`: `iFood|João|Rua A, 123|Centro|Natal|59000-000`
4. **Texto livre** — usado como endereço.

## Stack

- **Android** · Kotlin · **Jetpack Compose** (Material 3)
- **Hilt** (injeção de dependência)
- **CameraX** + **ML Kit Barcode Scanning**
- **Navigation Compose**
- Arquitetura em camadas (domain / ui) — a rota é mantida em memória na sessão.

## Estrutura

```
app/src/main/java/com/jucelio/rotacerta/
├── MainActivity.kt
├── RotaCertaApplication.kt
├── domain/
│   ├── model/delivery/      # Delivery, DeliveryType, DeliveryStatus
│   └── usecase/delivery/    # ScannedDeliveryParser, DeliveryRoutePlanner
└── ui/
    ├── RotaCertaApp.kt       # navegação (grafo com ViewModel compartilhado)
    ├── theme/                # tema Compose (dark grafite + verde)
    └── delivery/             # Home, Rota, Scanner, Histórico, bottom bar, tema
```

## Como rodar

Requer Android Studio (JDK 17, Android SDK 35).

```bash
./gradlew assembleDebug        # gera o APK de debug
./gradlew test                 # roda os testes unitários
```

## Testes

Cobrem o parser de endereço, o planejador de rota e o ViewModel
(status online, histórico e progresso).

---

Projeto criado por Jucelio Farias Coelho.
