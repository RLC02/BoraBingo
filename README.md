<p align="center">
  <img src="frontend/assets/01-marca/borabingo-logo.png" alt="BoraBingo Logo" width="320"/>
</p>

# 🎰 Ecossistema de Microsserviços: Bingo Online Concorrente

Este projeto implementa uma arquitetura completa de Microsserviços para um **Jogo de Bingo Online**, contemplando Frontend Web (React Native / Expo / Tamagui), Spring Boot 3, Spring Cloud Gateway, MongoDB com Transações Réplica Set (`@Transactional`), Mensageria RabbitMQ assíncrona, Autenticação segura via `HttpOnly Cookies`, Controle de Acesso Baseado em Perfis (RBAC) e Testes de Carga / Concorrência automatizados em **K6**.

---

## 🏛️ Arquitetura dos Microsserviços

| Microsserviço | Porta | Responsabilidade |
| :--- | :---: | :--- |
| **API Gateway** (`gateway-service`) | `8080` | Roteamento, CORS, extração e validação do JWT via Cookies HttpOnly, autorização de rotas RBAC (`ROLE_ADMIN` vs `ROLE_USER`). |
| **Auth Service** (`auth-service`) | `8081` | Gerenciamento de usuários, hash de senhas (BCrypt), emissão de Tokens JWT gravados estritamente em cookies `HttpOnly`, `SameSite=Lax`. |
| **Bingo Sala Service** (`bingo-sala-service`) | `8082` | Gestão de salas de Bingo, sorteio sequencial de números (1-75), controle de capacidade de cartelas, publicação de eventos de jogo no RabbitMQ. |
| **Cartela Service** (`cartela-service`) | `8083` | Solicitação assíncrona de compra de cartelas, Consumer de fila RabbitMQ com confirmação manual (ACK/NACK), persistência transacional MongoDB (`@Transactional`), geração de matrizes 5x5 e validação de vitória (Bingo). |

---

## 1. 🍪 Cookies e Autenticação

- **Armazenamento:** O Token JWT é gravado exclusivamente nos **Cookies do navegador** (`Application > Cookies > bingo_auth_token`), com as diretivas:
  - `HttpOnly = true` (Impede leitura maliciosa via scripts JS / XSS).
  - `SameSite = Lax` (Protege contra ataques CSRF).
  - `Secure = false` (Permitido para desenvolvimento local, `true` em produção HTTPS).
  - `MaxAge = 86400` (24 horas).
- **Sem LocalStorage:** O Frontend **não** salva tokens de sessão em `localStorage` ou `sessionStorage`.
- **Propagação de Identidade:** O API Gateway intercepta o Cookie, valida a assinatura e injeta os headers internos (`X-Auth-Username`, `X-Auth-Role`, `X-Auth-UserId`) para os microsserviços downstream.

---

## 2. 🗄️ Transações com MongoDB (@Transactional)

- **Configuração:** O MongoDB está configurado em modo Replica Set (`rs0`) no [`docker-compose.yml`](docker-compose.yml:1), pré-requisito obrigatório do MongoDB para suporte a Transações Multi-Documento ACID.
- **Spring Transaction Manager:** Cada microsserviço declara o bean [`MongoTransactionManager`](bingo-sala-service/src/main/java/br/com/fatec/bingo/sala/config/RabbitAndMongoConfig.java:27).
- **Atomicidade & Rollback:** Métodos críticos como [`solicitarCompraCartela`](cartela-service/src/main/java/br/com/fatec/bingo/cartela/service/CartelaService.java:38) e [`processarCompraFila`](cartela-service/src/main/java/br/com/fatec/bingo/cartela/service/CartelaService.java:68) utilizam `@Transactional`. Caso ocorra qualquer exceção durante a verificação de saldo ou capacidade de vagas, o MongoDB desfaz todas as alterações de escrita automaticamente (Rollback), evitando cartelas órfãs ou estados inconsistentes.

---

## 3. 🐇 Mensageria, Filas e Pub/Sub (RabbitMQ)

### Fluxo de Mensagens:
1. **Compra Assíncrona de Cartelas:**
   - **Publisher:** [`CartelaService.solicitarCompraCartela()`](cartela-service/src/main/java/br/com/fatec/bingo/cartela/service/CartelaService.java:60) publica na Exchange `bingo.exchange` com a routing key `bingo.cartela.compra.solicitada`.
   - **Queue:** `bingo.compra.cartela.queue` (com Dead Letter Queue associada `bingo.compra.cartela.dlq`).
   - **Consumer:** [`CompraCartelaConsumer.processarCompraCartela()`](cartela-service/src/main/java/br/com/fatec/bingo/cartela/consumer/CompraCartelaConsumer.java:23) consome as mensagens com `ackMode = MANUAL`, garantindo que o ACK só é emitido se o processamento for confirmado com êxito. Em caso de falha irrecuperável, emite `basicNack` direcionando à DLQ.

2. **Eventos de Sala e Sorteio (Pub/Sub):**
   - **Publisher:** [`SalaService`](bingo-sala-service/src/main/java/br/com/fatec/bingo/sala/service/SalaService.java:55) publica eventos como `bingo.sala.criada`, `bingo.sala.iniciada`, `bingo.numero.sorteado` e `bingo.sala.finalizada`.
   - **Consumers:** Fila `bingo.sala.eventos.queue` e `bingo.sorteio.eventos.queue`.

---

## 4. 🔄 Ciclo de Vida e Controle de Status

Nenhuma entidade de mensagem ou cartela possui status nulo:
- **`PENDENTE`:** Cartela registrada no MongoDB imediatamente após a solicitação do usuário, antes de entrar na esteira da fila.
- **`PROCESSANDO`:** O Consumer RabbitMQ retira a mensagem da fila e inicia as validações de concorrência e capacidade da sala.
- **`CONFIRMADO`:** Validações aceitas, matriz 5x5 do bingo gerada e contagem da sala incrementada.
- **`REJEITADO`:** Capacidade esgotada ou concorrência atingida. O motivo fica registrado em `motivoRejeicao`.

---

## 5. ⚡ Testes de Carga e Concorrência com K6

O script [`k6-test-concorrencia.js`](k6-test-concorrencia.js:1) simula o cenário onde **30 jogadores virtuais (VUs)** tentam comprar simultaneamente as últimas **20 cartelas disponíveis** em uma sala:

```bash
# Executando o teste K6
k6 run k6-test-concorrencia.js
```

### Resultados Validados:
- **`bingo_cartelas_confirmadas`:** Exatamente **20** (limite da sala).
- **`bingo_cartelas_rejeitadas`:** Exatamente **10** (30 - 20 = 10 rejeitadas sem erro no sistema).
- **Consistência final no banco:** O endpoint `GET /api/salas/{id}` confirma que `cartelasVendidas == 20`, comprovando ausência total de overbooking ou concorrência descontrolada.

---

## 6. 🛡️ Controle de Acesso (RBAC): Admin vs Usuário Comum

- **Regras:**
  - `ROLE_USER`: Visualiza salas, compra cartelas, marca números e reivindica vitória (Bater Bingo).
  - `ROLE_ADMIN`: Cria novas salas, altera limites de vagas, inicia partidas e comanda o sorteio das bolas.
- **Proteção em Profundidade:**
  - O [`AuthenticationGlobalFilter`](gateway-service/src/main/java/br/com/fatec/bingo/gateway/filter/AuthenticationGlobalFilter.java:59) bloqueia no backend requisições para `/api/salas/admin/**` que não contenham `ROLE_ADMIN`, retornando HTTP `403 FORBIDDEN`.

---

## 🚀 Como Executar o Projeto

### 1. Iniciar Infraestrutura (MongoDB Replica Set & RabbitMQ):
```bash
docker-compose up -d
```

### 2. Iniciar os Microsserviços Spring Boot:
```bash
# Terminal 1 - Gateway Service
cd gateway-service && ./mvnw spring-boot:run

# Terminal 2 - Auth Service
cd auth-service && ./mvnw spring-boot:run

# Terminal 3 - Bingo Sala Service
cd bingo-sala-service && ./mvnw spring-boot:run

# Terminal 4 - Cartela Service
cd cartela-service && ./mvnw spring-boot:run
```

### 3. Iniciar o Frontend Web Expo:
```bash
cd frontend
npm install
npm run web
```
Acesse no navegador: `http://localhost:8081` (ou a porta indicada pelo Expo Web).
