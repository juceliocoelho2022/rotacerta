# RotaCerta Live

## Objetivo

O **RotaCerta Live** foi criado para reduzir tentativas de entrega frustradas e custos de reentrega.

Quando um pedido muda para `OUT_FOR_DELIVERY`, o backend cria uma sessão pública temporária. O cliente recebe um link de acompanhamento, visualiza os eventos da entrega e pode autorizar antecipadamente outra pessoa de confiança caso não esteja no endereço.

## Status

**MVP implementado e validado localmente.**

Fluxos já testados:

- criação do link público para pedido em `OUT_FOR_DELIVERY`;
- acesso à página `/live/{token}`;
- consulta da timeline do pedido;
- atualização automática a cada 30 segundos;
- autorização de recebedor alternativo;
- persistência da autorização no PostgreSQL;
- exibição do recebedor autorizado na própria página Live;
- execução integrada com React, Spring Boot, PostgreSQL e Docker Compose.

## Fluxo

```text
Pedido OUT_FOR_DELIVERY
        ↓
Spring Boot cria sessão temporária
        ↓
Token público aleatório de alta entropia
        ↓
Link /live/{token}
        ↓
Cliente acompanha a timeline
        ↓
Cliente pode autorizar familiar/vizinho/porteiro
        ↓
Instrução persiste no PostgreSQL
        ↓
Página Live exibe o recebedor autorizado
        ↓
Próxima integração: Android do entregador
```

## Segurança do link

- O link não usa o ID sequencial do pedido.
- O token é composto por dois UUIDs aleatórios sem hífen.
- A sessão expira após 48 horas.
- A sessão é encerrada automaticamente quando a entrega é concluída, falha, é devolvida ou cancelada.
- O frontend alerta o usuário para não compartilhar o link publicamente.

## Endpoints

### Criar/obter link Live

```http
POST /api/deliveries/{id}/live-link
```

Disponível quando o pedido está em `OUT_FOR_DELIVERY`.

Exemplo de resposta:

```json
{
  "publicUrl": "http://localhost:5173/live/<token>",
  "expiresAt": "2026-08-31T12:00:00Z"
}
```

### Acompanhar entrega

```http
GET /api/public/live/{token}
```

A resposta inclui:

- código de rastreamento;
- número do pedido;
- cliente;
- status atual;
- validade da sessão;
- recebedor alternativo, quando houver;
- instruções da entrega;
- histórico de eventos.

### Autorizar outro recebedor

```http
POST /api/public/live/{token}/recipient
```

Exemplo:

```json
{
  "name": "Maria Souza",
  "relationship": "Vizinho",
  "instructions": "Pode receber a encomenda na casa ao lado."
}
```

Após a gravação, a página Live exibe o bloco **Recebedor autorizado**.

## Banco de dados

Migration:

```text
V3__create_live_tracking_sessions.sql
```

Tabela:

```text
delivery_tracking_sessions
```

A sessão armazena:

- token público;
- pedido relacionado;
- validade;
- status ativo/inativo;
- nome do recebedor alternativo;
- relação com o destinatário;
- instruções de entrega.

## Frontend

Rota pública:

```text
/live/:token
```

A página possui:

- cabeçalho de entrega em andamento;
- status atual;
- código de rastreamento;
- atualização automática;
- timeline dos eventos;
- formulário de autorização;
- confirmação visual da pessoa autorizada.

## Ambiente validado

```text
Frontend:   React + TypeScript + Vite + Nginx
Backend:    Java 21 + Spring Boot
Banco:      PostgreSQL 17 + Flyway
Infra:      Docker + Docker Compose
```

Containers esperados:

```text
rotacerta-postgres   healthy
rotacerta-backend    running
rotacerta-frontend   running
```

Healthcheck:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Resposta:

```text
status
------
UP
```

## Observação sobre desenvolvimento local

Evite deixar uma instância antiga do Vite usando a porta `5173` ao mesmo tempo que o frontend Docker.

Para identificar conflitos:

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

Se houver um `node.exe` executando um Vite antigo, encerre apenas esse PID. Não encerre o processo do Docker Desktop.

## Próximas evoluções

1. integrar o Android do entregador para visualizar recebedor e instruções;
2. gerar PIN temporário de confirmação de entrega;
3. transmitir localização do motorista com privacidade controlada;
4. calcular ETA e quantidade de paradas restantes;
5. adicionar mapa em tempo real;
6. enviar o link automaticamente por canal autorizado;
7. medir reentregas evitadas, quilômetros e custo operacional economizado;
8. evoluir eventos logísticos para arquitetura assíncrona com Kafka.
