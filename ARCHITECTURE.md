# Arquitetura — RotaCerta

O RotaCerta evolui de forma incremental para uma arquitetura em camadas, com separação clara entre regras de negócio, infraestrutura e interface.

## Objetivos

- reduzir acoplamento entre Android UI e regras de negócio;
- facilitar testes unitários;
- permitir substituir armazenamento em memória por Room/PostgreSQL sem reescrever telas;
- preparar integração com API Spring Boot;
- manter navegação centralizada;
- escalar o projeto por funcionalidades sem transformar o app em um conjunto de arquivos acoplados.

## Estrutura atual

```text
com.jucelio.rotacerta
├── core/
│   └── navigation/          # contratos e rotas globais
├── domain/
│   ├── model/               # entidades e modelos de negócio
│   └── usecase/             # regras de leitura e planejamento de entregas
├── presentation/
│   ├── navigation/          # NavHost raiz
│   └── splash/              # Splash + SplashViewModel
├── ui/
│   ├── delivery/            # telas atuais da feature de entregas
│   └── theme/               # Material 3 e identidade visual
├── MainActivity.kt
└── RotaCertaApplication.kt
```

A pasta `ui/delivery` é uma camada legada funcional que será migrada gradualmente para `presentation/delivery`. A migração será feita por sprint para evitar uma refatoração grande sem ganho funcional imediato.

## Fluxo de dependências

```text
Presentation/UI
      ↓
    Domain
      ↑
     Data
```

A camada `domain` não deve conhecer Android, Compose, CameraX, Retrofit, Room ou detalhes de banco de dados.

## MVVM

Cada feature de tela deve seguir a direção:

```text
Composable Screen
      ↓ eventos
ViewModel
      ↓
Use Cases
      ↓
Repository Contract
      ↓
Repository Implementation
```

O estado exposto pelo ViewModel deve ser imutável para a UI.

## Navegação

As rotas globais ficam em `core/navigation/AppRoutes.kt`.

A Splash é o novo ponto de entrada:

```text
Splash
  ↓
Delivery Graph
  ├── Home
  ├── Scanner
  ├── Rota
  └── Histórico
```

## Próximas evoluções

### Sprint 2
- autenticação do entregador;
- sessão de usuário;
- tela de login;
- perfil básico.

### Sprint 3
- persistência local com Room;
- repository contract no domínio;
- implementação em `data/local`;
- sincronização offline-first.

### Sprint 4
- Google Maps/Maps Compose;
- geocodificação dos endereços;
- cálculo de distância real;
- GPS do entregador.

### Sprint 5
- backend Java + Spring Boot;
- PostgreSQL/PostGIS;
- autenticação JWT;
- API REST;
- rastreamento de entregas.

### Sprint 6
- otimização de rota com VRP;
- janelas de entrega;
- prioridade;
- trânsito;
- reotimização da rota.

## Convenções de Git

Branches:

```text
feat/<funcionalidade>
fix/<correcao>
refactor/<descricao>
chore/<descricao>
```

Commits seguem Conventional Commits:

```text
feat: ...
fix: ...
refactor: ...
build: ...
ci: ...
docs: ...
```

Pull requests devem passar por testes e build do Android CI antes do merge.
