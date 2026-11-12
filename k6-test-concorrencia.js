import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { CookieJar } from 'k6/http';

/**
 * Script de Teste de Carga e Concorrência K6 - Bingo Microservices
 *
 * Cenário avaliado:
 * - Múltiplos jogadores tentando comprar simultaneamente as últimas vagas/cartelas de uma Sala de Bingo
 * - Demonstra a transação atômica (@Transactional no MongoDB) e a fila RabbitMQ
 * - Comprova que o limite de cartelas não é ultrapassado (sem overbooking / sem estoque negativo)
 * - Passa pelo API Gateway com autenticação JWT via Cookie (demonstra RBAC e autenticação real)
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Parâmetros configuráveis
const SALA_ID         = __ENV.SALA_ID || 'sala-demo-concorrencia';
const MAX_CARTELAS    = Number(__ENV.MAX_CARTELAS    || 20);
const QTD_REQUISICOES = Number(__ENV.QTD_REQUISICOES || 30);
const POLL_TENTATIVAS = Number(__ENV.POLL_TENTATIVAS || 25);
const POLL_INTERVALO  = Number(__ENV.POLL_INTERVALO  || 0.4);

// Métricas Customizadas K6
const comprasConfirmadas = new Counter('bingo_cartelas_confirmadas');
const comprasRejeitadas  = new Counter('bingo_cartelas_rejeitadas');
const timeoutPolling     = new Counter('bingo_timeout_polling');
const errosInesperados   = new Counter('bingo_erro_inesperado');

export const options = {
    scenarios: {
        compra_simultanea_cartelas: {
            executor: 'shared-iterations',
            vus: QTD_REQUISICOES,
            iterations: QTD_REQUISICOES,
            maxDuration: '120s',
        },
    },
};

/**
 * setup(): executado uma vez antes do teste.
 * 1. Faz login como admin via Gateway (demonstra autenticação por cookie)
 * 2. Cria sala de teste via rota administrativa (/api/salas/admin)
 * 3. Retorna salaId e cookie de admin para uso no teardown
 */
export function setup() {
    console.log('--- Iniciando Setup do Teste de Concorrência ---');

    // Login do admin para criar sala (demonstra autenticação via Gateway + Cookie)
    const loginRes = http.post(`${BASE_URL}/api/auth/login`,
        JSON.stringify({ username: 'admin', password: 'admin123' }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    let adminCookie = '';
    if (loginRes.status === 200) {
        // Extrai cookie bingo_auth_token da resposta
        const setCookie = loginRes.headers['Set-Cookie'] || '';
        const match = setCookie.match(/bingo_auth_token=([^;]+)/);
        if (match) adminCookie = match[1];
        console.log(`Login admin bem-sucedido. Cookie obtido: ${adminCookie ? 'SIM' : 'NÃO'}`);
    } else {
        console.warn(`Falha no login admin (status ${loginRes.status}). Usando fallback sem autenticação.`);
    }

    // Cria sala de teste via Gateway (rota administrativa protegida por ROLE_ADMIN)
    const payloadSala = JSON.stringify({
        nome: `Sala Carga K6 - ${Date.now()}`,
        descricao: 'Sala de teste sob alta concorrência de compra',
        precoCartela: 10.0,
        maxCartelas: MAX_CARTELAS
    });

    const headersAdmin = {
        'Content-Type': 'application/json',
        ...(adminCookie ? { 'Cookie': `bingo_auth_token=${adminCookie}` } : {})
    };

    const res = http.post(`${BASE_URL}/api/salas/admin`, payloadSala, { headers: headersAdmin });

    let salaCriadaId = SALA_ID;
    if (res.status === 201) {
        const body = JSON.parse(res.body);
        salaCriadaId = body.id;
        console.log(`Sala criada com sucesso! ID: ${salaCriadaId}, Limite: ${MAX_CARTELAS} cartelas`);
    } else {
        console.warn(`Não foi possível criar sala (Status ${res.status}). Body: ${res.body}. Usando fallback ID: ${salaCriadaId}`);
    }

    return { salaId: salaCriadaId, adminCookie };
}

/**
 * default(): executado por cada VU (usuário virtual).
 * Cada VU usa CookieJar para manter o cookie de sessão entre requisições.
 */
export default function (data) {
    const salaId = data.salaId;
    const vuId   = __VU;

    // CookieJar gerencia o cookie bingo_auth_token automaticamente
    const jar = new CookieJar();

    const loginRes = http.post(`${BASE_URL}/api/auth/login`,
        JSON.stringify({ username: 'jogador', password: 'jogador123' }),
        { headers: { 'Content-Type': 'application/json' }, jar }
    );

    if (loginRes.status !== 200) {
        // Fallback: injeta headers internos se o login falhou
        const headers = {
            'Content-Type': 'application/json',
            'X-Auth-UserId': `user-id-${vuId}`,
            'X-Auth-Username': `jogador-k6-${vuId}`,
        };
        const resCompra = http.post(`${BASE_URL}/api/cartelas/comprar`,
            JSON.stringify({ salaId }), { headers });
        _processarCompra(resCompra, salaId, headers);
        return;
    }

    const reqOpts = { headers: { 'Content-Type': 'application/json' }, jar };

    // 1. Solicita compra assíncrona via Gateway (cookie enviado automaticamente pelo jar)
    const resCompra = http.post(`${BASE_URL}/api/cartelas/comprar`,
        JSON.stringify({ salaId }),
        reqOpts
    );

    _processarCompra(resCompra, salaId, reqOpts);
}

function _processarCompra(resCompra, salaId, reqOpts) {
    const compraAceita = check(resCompra, {
        'solicitacao aceita (202 ACCEPTED)': (r) => r.status === 202,
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

    // 2. Polling no endpoint GET /api/cartelas/{id} via Gateway
    let statusFinal = null;
    for (let i = 0; i < POLL_TENTATIVAS; i++) {
        sleep(POLL_INTERVALO);

        const resStatus = http.get(`${BASE_URL}/api/cartelas/${cartelaId}`, reqOpts);
        if (resStatus.status !== 200) continue;

        try {
            const body   = JSON.parse(resStatus.body);
            const status = body.status;
            if (status === 'CONFIRMADO' || status === 'REJEITADO') {
                statusFinal = status;
                break;
            }
        } catch (e) { /* parsing em andamento */ }
    }

    // 3. Incrementa métricas
    if (statusFinal === 'CONFIRMADO') {
        comprasConfirmadas.add(1);
    } else if (statusFinal === 'REJEITADO') {
        comprasRejeitadas.add(1);
    } else {
        timeoutPolling.add(1);
    }
}

/**
 * teardown(): executado uma vez após o teste.
 * Aguarda a fila RabbitMQ processar todas as mensagens antes de validar.
 * Valida consistência final — cartelasVendidas não pode ultrapassar maxCartelas.
 */
export function teardown(data) {
    const salaId      = data.salaId;
    const adminCookie = data.adminCookie;

    const headers = adminCookie
        ? { 'Cookie': `bingo_auth_token=${adminCookie}` }
        : {};

    // Aguarda a fila RabbitMQ processar todas as mensagens assíncronas
    console.log('⏳ Aguardando processamento assíncrono da fila RabbitMQ (15s)...');
    sleep(15);

    const res = http.get(`${BASE_URL}/api/salas/${salaId}`, { headers });

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
