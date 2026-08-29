# RotaCerta Smart Dispatch

O Smart Dispatch é o motor de decisão logística do RotaCerta para seleção automática de motoristas, cálculo de ETA, monitoramento operacional e otimização inicial de rotas de última milha.

## Score logístico

A seleção considera quatro fatores principais:

```text
score = distância * 8
      + ocupação do motorista * 20
      + pressão de SLA * 15
      - prioridade do pedido * 2
```

Quanto menor o score, melhor a combinação para a entrega.

## Recursos implementados

- Cadastro de motoristas com latitude, longitude, disponibilidade, carga atual e capacidade máxima.
- Coordenadas, prioridade e SLA por pedido.
- Seleção automática do melhor motorista.
- Cálculo de distância pela fórmula de Haversine.
- ETA inicial com velocidade urbana média configurada no serviço.
- Reotimização de sequência com heurística nearest-neighbor para múltiplas paradas.
- Atualização de posição do motorista.
- Endpoint consolidado para o painel administrativo.
- Dashboard em tempo quase real com atualização a cada 15 segundos.
- Visualização de pedidos, motoristas, rotas, KPI, score, ETA, SLA e alertas.

## Endpoints

```http
GET  /api/dispatch/monitoring
POST /api/dispatch/orders/{orderId}/assign
GET  /api/dispatch/orders/{orderId}
GET  /api/dispatch/drivers/{driverId}/route
PATCH /api/dispatch/drivers/{driverId}/location
```

### Exemplo de atualização de localização

```json
{
  "latitude": -23.550520,
  "longitude": -46.633308
}
```

## Próximas evoluções

- Mapas reais com engine de roteamento viário.
- Trânsito em tempo real no ETA.
- WebSocket/SSE para telemetria em vez de polling.
- VRP com janelas de tempo e capacidade.
- Redis para estado de alta frequência.
- Kafka para eventos `driver.location.updated`, `delivery.assigned` e `route.reoptimized`.
- Histórico de performance por motorista e região.
- Previsão de ETA com machine learning.
