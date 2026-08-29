# RotaCerta Live

## Objetivo

O **RotaCerta Live** reduz tentativas de entrega frustradas. Quando um pedido muda para `OUT_FOR_DELIVERY`, o backend cria uma sessão pública temporária e segura. O cliente pode acompanhar a evolução da entrega e autorizar outra pessoa de confiança caso não esteja no endereço.

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
Operação/Driver poderá consultar na próxima integração mobile
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

Disponível apenas quando o pedido está em `OUT_FOR_DELIVERY`.

Resposta:

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

### Autorizar outro recebedor

```http
POST /api/public/live/{token}/recipient
```

```json
{
  "name": "Maria Souza",
  "relationship": "Familiar",
  "instructions": "Apartamento 32. Pode receber por mim."
}
```

## Banco de dados

Migration:

```text
V3__create_live_tracking_sessions.sql
```

Tabela:

```text
delivery_tracking_sessions
```

Armazena token, validade, status da sessão, pessoa autorizada e instruções de entrega.

## Frontend

A rota pública é:

```text
/live/:token
```

A página atualiza os eventos a cada 30 segundos e permite autorizar outra pessoa sem exigir login.

## Próximas evoluções

1. integrar o Android do entregador para visualizar instruções em tempo real;
2. enviar o link automaticamente por e-mail/SMS/WhatsApp usando provedor autorizado;
3. transmitir localização do motorista com privacidade controlada;
4. calcular ETA e quantidade de paradas restantes;
5. confirmar recebimento por PIN/QR Code;
6. medir reentregas evitadas, quilômetros e custo operacional economizado.
