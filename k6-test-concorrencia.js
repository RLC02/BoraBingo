import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

/**
 * Script de Teste de Carga e Concorrência K6 - Bingo Microservices
 * 
 * Cenário avaliado:
 * - Múltiplos jogadores tentando comprar simultaneamente as últimas vagas/cartelas de uma Sala de Bingo
 * - Demonstra a transação atômica (@Transactional no MongoDB) e a fila RabbitMQ
 * - Comprova que o limite de cartelas não é ultrapassado (sem overbooking / sem estoque negativo)
 */

const BASE_URL_GATEWAY = __ENV.BASE_URL_GATEWAY || 'http://localhost:8080';
const BASE_URL_SALA = __ENV.BASE_URL_SALA || 'http://localhost:8082';
const BASE_URL_CARTELA = __ENV.BASE_URL_CARTELA || 'http://localhost:8083';

// Parâmetros configuráveis
const SALA_ID = __ENV.SALA_ID || 'sala-demo-concorrencia';
const MAX_CARTELAS = Number(__ENV.MAX_CARTELAS || 20); // Limite de cartelas da sala
const QTD_REQUISICOES = Number(__ENV.QTD_REQUISICOES || 30); // 30 usuários disputando 20 vagas

const POLL_TENTATIVAS = Number(__ENV.POLL_TENTATIVAS || 25);
const POLL_INTERVALO = Number(__ENV.POLL_INTERVALO || 0.4);

// Métricas Customizadas K6
const comprasConfirmadas = new Counter('bingo_cartelas_confirmadas');
const comprasRejeitadas = new Counter('bingo_cartelas_rejeitadas');
const timeoutPolling = new Counter('bingo_timeout_polling');
const errosInesperados = new Counter('bingo_erro_inesperado');

export const options = {
    scenarios: {
        compra_simultanea_cartelas: {
            executor: 'shared-iterations',
            vus: QTD_REQUISICOES,
            iterations: QTD_REQUISICOES,
            maxDuration: '60s',
        },
    },
};

export function setup() {
    console.log('--- Iniciando Setup do Teste de Concorrência ---');
    // Cria uma sala de teste com capacidade limitada de cartelas
    const payloadSala = JSON.stringify({
        nome: `Sala Carga K6 - ${Date.now()}`,
        descricao: 'Sala de teste sob alta concorrência de compra',
        precoCartela: 10.0,
        maxCartelas: MAX_CARTELAS
    });

    const params = { headers: { 'Content-Type': 'application/json' } };
    const res = http.post(`${BASE_URL_SALA}/api/salas/admin`, payloadSala, params);

    let salaCriadaId = SALA_ID;
    if (res.status === 201) {
        const body = JSON.parse(res.body);
        salaCriadaId = body.id;
        console.log(`Sala de teste criada com sucesso! ID: ${salaCriadaId}, Limite Vagas: ${MAX_CARTELAS}`);
    } else {
        console.warn(`Não foi possível criar sala via setup (Status ${res.status}). Usando fallback ID: ${salaCriadaId}`);
    }

    return { salaId: salaCriadaId };
}

export default function (data) {
    const salaId = data.salaId;
    const clientId = `jogador-k6-${__VU}`;
    const payload = JSON.stringify({
        salaId: salaId
    });

    const headers = {
        'Content-Type': 'application/json',
        'X-Auth-UserId': `user-id-${__VU}`,
        'X-Auth-Username': clientId
    };

    // 1. Envia solicitação assíncrona de compra para a API de Cartelas
    const resCompra = http.post(`${BASE_URL_CARTELA}/api/cartelas/comprar`, payload, { headers });

    const compraAceita = check(resCompra, {
        'solicitacao aceita (202 ACCEPTED / 2xx)': (r) => r.status >= 200 && r.status < 300,
    });

    if (!compraAceita) {
        errosInesperados.add(1);
        return;
    }

    let cartelaId;
    try {
        cartelaId = JSON.parse(resCompra.body).id;
    } catch (e) {
        errosInesperados.add(1);
        return;
    }

    // 2. Realiza Polling no endpoint GET /api/cartelas/{id} até a fila RabbitMQ processar o status
    let statusFinal = null;
    for (let i = 0; i < POLL_TENTATIVAS; i++) {
        sleep(POLL_INTERVALO);

        const resStatus = http.get(`${BASE_URL_CARTELA}/api/cartelas/${cartelaId}`);
        if (resStatus.status !== 200) continue;

        try {
            const body = JSON.parse(resStatus.body);
            const status = body.status;
            if (status === 'CONFIRMADO' || status === 'REJEITADO') {
                statusFinal = status;
                break;
            }
        } catch (e) {
            // parsing em andamento
        }
    }

    // 3. Incrementa métricas conforme o status consolidado
    if (statusFinal === 'CONFIRMADO') {
        comprasConfirmadas.add(1);
    } else if (statusFinal === 'REJEITADO') {
        comprasRejeitadas.add(1);
    } else {
        timeoutPolling.add(1);
    }
}

export function teardown(data) {
    const salaId = data.salaId;
    // Consulta o estado final da Sala (GET de validação de consistência)
    const res = http.get(`${BASE_URL_SALA}/api/salas/${salaId}`);
    
    if (res.status === 200) {
        const sala = JSON.parse(res.body);
        console.log('======================================================');
        console.log('       RELATÓRIO DE CONCORRÊNCIA E TRANSAÇÃO          ');
        console.log('======================================================');
        console.log(`Capacidade máxima de cartelas: ${MAX_CARTELAS}`);
        console.log(`Cartelas vendidas registradas: ${sala.cartelasVendidas}`);
        console.log(`Requisições simultâneas:       ${QTD_REQUISICOES}`);
        console.log('======================================================');
        console.log('MÉTRICAS DO K6:');
        console.log(`  bingo_cartelas_confirmadas -> Esperado: ${Math.min(MAX_CARTELAS, QTD_REQUISICOES)}`);
        console.log(`  bingo_cartelas_rejeitadas  -> Esperado: ${Math.max(0, QTD_REQUISICOES - MAX_CARTELAS)}`);
        console.log('  bingo_timeout_polling      -> Esperado: 0');
        console.log('======================================================');

        if (sala.cartelasVendidas > MAX_CARTELAS) {
            console.error('ERRO CRÍTICO: Cartelas vendidas ULTRAPASSARAM o limite! Falha de concorrência.');
        } else {
            console.log('SUCESSO: Limite de cartelas respeitado estritamente sem overbooking!');
        }
    }
}
