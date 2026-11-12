import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  ScrollView,
  ActivityIndicator,
  Image,
  Platform,
  Animated,
} from 'react-native';
import api from './src/services/api';

// ─── Brand tokens (mirrors assets/07-estilo/paleta.json) ────────────────────
const C = {
  purple: '#6541D9',
  ink:    '#221B3D',
  lime:   '#D8F36A',
  cream:  '#FBF8F1',
  lilac:  '#BAA5F5',
  muted:  '#706780',
  line:   '#E5DEF1',
  tile:   '#F2EEF9',
};

// ─── Asset helpers ────────────────────────────────────────────────────────────
const getBallImage = (num) => {
  const pad = String(num).padStart(2, '0');
  // require() needs static strings — use a lookup map
  const BALLS = {
    '01': require('./assets/04-bolas/png/bola-01.png'),
    '02': require('./assets/04-bolas/png/bola-02.png'),
    '03': require('./assets/04-bolas/png/bola-03.png'),
    '04': require('./assets/04-bolas/png/bola-04.png'),
    '05': require('./assets/04-bolas/png/bola-05.png'),
    '06': require('./assets/04-bolas/png/bola-06.png'),
    '07': require('./assets/04-bolas/png/bola-07.png'),
    '08': require('./assets/04-bolas/png/bola-08.png'),
    '09': require('./assets/04-bolas/png/bola-09.png'),
    '10': require('./assets/04-bolas/png/bola-10.png'),
    '11': require('./assets/04-bolas/png/bola-11.png'),
    '12': require('./assets/04-bolas/png/bola-12.png'),
    '13': require('./assets/04-bolas/png/bola-13.png'),
    '14': require('./assets/04-bolas/png/bola-14.png'),
    '15': require('./assets/04-bolas/png/bola-15.png'),
    '16': require('./assets/04-bolas/png/bola-16.png'),
    '17': require('./assets/04-bolas/png/bola-17.png'),
    '18': require('./assets/04-bolas/png/bola-18.png'),
    '19': require('./assets/04-bolas/png/bola-19.png'),
    '20': require('./assets/04-bolas/png/bola-20.png'),
    '21': require('./assets/04-bolas/png/bola-21.png'),
    '22': require('./assets/04-bolas/png/bola-22.png'),
    '23': require('./assets/04-bolas/png/bola-23.png'),
    '24': require('./assets/04-bolas/png/bola-24.png'),
    '25': require('./assets/04-bolas/png/bola-25.png'),
    '26': require('./assets/04-bolas/png/bola-26.png'),
    '27': require('./assets/04-bolas/png/bola-27.png'),
    '28': require('./assets/04-bolas/png/bola-28.png'),
    '29': require('./assets/04-bolas/png/bola-29.png'),
    '30': require('./assets/04-bolas/png/bola-30.png'),
    '31': require('./assets/04-bolas/png/bola-31.png'),
    '32': require('./assets/04-bolas/png/bola-32.png'),
    '33': require('./assets/04-bolas/png/bola-33.png'),
    '34': require('./assets/04-bolas/png/bola-34.png'),
    '35': require('./assets/04-bolas/png/bola-35.png'),
    '36': require('./assets/04-bolas/png/bola-36.png'),
    '37': require('./assets/04-bolas/png/bola-37.png'),
    '38': require('./assets/04-bolas/png/bola-38.png'),
    '39': require('./assets/04-bolas/png/bola-39.png'),
    '40': require('./assets/04-bolas/png/bola-40.png'),
    '41': require('./assets/04-bolas/png/bola-41.png'),
    '42': require('./assets/04-bolas/png/bola-42.png'),
    '43': require('./assets/04-bolas/png/bola-43.png'),
    '44': require('./assets/04-bolas/png/bola-44.png'),
    '45': require('./assets/04-bolas/png/bola-45.png'),
    '46': require('./assets/04-bolas/png/bola-46.png'),
    '47': require('./assets/04-bolas/png/bola-47.png'),
    '48': require('./assets/04-bolas/png/bola-48.png'),
    '49': require('./assets/04-bolas/png/bola-49.png'),
    '50': require('./assets/04-bolas/png/bola-50.png'),
    '51': require('./assets/04-bolas/png/bola-51.png'),
    '52': require('./assets/04-bolas/png/bola-52.png'),
    '53': require('./assets/04-bolas/png/bola-53.png'),
    '54': require('./assets/04-bolas/png/bola-54.png'),
    '55': require('./assets/04-bolas/png/bola-55.png'),
    '56': require('./assets/04-bolas/png/bola-56.png'),
    '57': require('./assets/04-bolas/png/bola-57.png'),
    '58': require('./assets/04-bolas/png/bola-58.png'),
    '59': require('./assets/04-bolas/png/bola-59.png'),
    '60': require('./assets/04-bolas/png/bola-60.png'),
    '61': require('./assets/04-bolas/png/bola-61.png'),
    '62': require('./assets/04-bolas/png/bola-62.png'),
    '63': require('./assets/04-bolas/png/bola-63.png'),
    '64': require('./assets/04-bolas/png/bola-64.png'),
    '65': require('./assets/04-bolas/png/bola-65.png'),
    '66': require('./assets/04-bolas/png/bola-66.png'),
    '67': require('./assets/04-bolas/png/bola-67.png'),
    '68': require('./assets/04-bolas/png/bola-68.png'),
    '69': require('./assets/04-bolas/png/bola-69.png'),
    '70': require('./assets/04-bolas/png/bola-70.png'),
    '71': require('./assets/04-bolas/png/bola-71.png'),
    '72': require('./assets/04-bolas/png/bola-72.png'),
    '73': require('./assets/04-bolas/png/bola-73.png'),
    '74': require('./assets/04-bolas/png/bola-74.png'),
    '75': require('./assets/04-bolas/png/bola-75.png'),
  };
  return BALLS[pad] ?? null;
};

// ─── Toast system ─────────────────────────────────────────────────────────────

const TOAST_TYPES = {
  success: { bg: '#D8F36A', text: '#1a3300', icon: '✓' },
  error:   { bg: '#F5DCDC', text: '#8B1A1A', icon: '✗' },
  info:    { bg: '#BAA5F5', text: '#221B3D', icon: 'ℹ' },
  warn:    { bg: '#FFF0CC', text: '#7A4500', icon: '⚠' },
};

function Toast({ toasts }) {
  return (
    <View style={ts.container} pointerEvents="none">
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} />
      ))}
    </View>
  );
}

function ToastItem({ toast }) {
  const opacity = useRef(new Animated.Value(0)).current;
  const translateY = useRef(new Animated.Value(-12)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.spring(opacity,     { toValue: 1, useNativeDriver: true }),
      Animated.spring(translateY,  { toValue: 0, useNativeDriver: true }),
    ]).start();
  }, []);

  const style = TOAST_TYPES[toast.type] || TOAST_TYPES.info;
  return (
    <Animated.View style={[ts.toast, { backgroundColor: style.bg, opacity, transform: [{ translateY }] }]}>
      <Text style={[ts.icon, { color: style.text }]}>{style.icon}</Text>
      <Text style={[ts.msg, { color: style.text }]}>{toast.message}</Text>
    </Animated.View>
  );
}

const ts = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 72,
    left: 0,
    right: 0,
    alignItems: 'center',
    zIndex: 9999,
    gap: 8,
    paddingHorizontal: 20,
  },
  toast: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    borderRadius: 14,
    paddingVertical: 12,
    paddingHorizontal: 16,
    maxWidth: 480,
    width: '100%',
    ...Platform.select({
      web: { boxShadow: '0 4px 20px rgba(34,27,61,0.18)' },
      default: { shadowColor: '#000', shadowOpacity: 0.15, shadowRadius: 8, elevation: 6 },
    }),
  },
  icon: { fontSize: 16, fontWeight: '800', marginRight: 10, marginTop: 1 },
  msg:  { flex: 1, fontSize: 14, fontWeight: '600', lineHeight: 20 },
});

// ─── Small reusable components ────────────────────────────────────────────────

/** Numbered bingo ball using the branded PNG assets */
function BingoBall({ number, size = 48 }) {
  const src = getBallImage(number);
  if (!src) return null;
  return (
    <Image
      source={src}
      style={{ width: size, height: size }}
      resizeMode="contain"
      accessibilityLabel={`Bola ${number}`}
    />
  );
}

/** Status badge with BoraBingo palette */
function StatusBadge({ status }) {
  const map = {
    CRIADA:       { bg: C.tile,           text: C.purple,  label: 'Aberta'      },
    EM_ANDAMENTO: { bg: C.lime,           text: C.ink,     label: 'Em Andamento'},
    FINALIZADA:   { bg: '#F5DCDC',        text: '#8B1A1A', label: 'Finalizada'  },
  };
  const s = map[status] ?? { bg: C.tile, text: C.muted, label: status };
  return (
    <View style={[ss.badge, { backgroundColor: s.bg }]}>
      <Text style={[ss.badgeText, { color: s.text }]}>{s.label}</Text>
    </View>
  );
}

// ─── Main App ────────────────────────────────────────────────────────────────
export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [username, setUsername]       = useState('');
  const [password, setPassword]       = useState('');
  const [nome, setNome]               = useState('');
  const [role, setRole]               = useState('ROLE_USER');
  const [isRegistering, setIsRegistering] = useState(false);
  const [loading, setLoading]         = useState(false);

  const [salas, setSalas]             = useState([]);
  const [selectedSala, setSelectedSala] = useState(null);
  const [cartelas, setCartelas]       = useState([]);

  const [novaSalaNome, setNovaSalaNome]             = useState('');
  const [novaSalaMaxCartelas, setNovaSalaMaxCartelas] = useState('20');
  const [novaSalaPreco, setNovaSalaPreco]           = useState('5.00');

  // ── Toast state ──────────────────────────────────────────────────────────────
  const [toasts, setToasts] = useState([]);
  const toastCounter = useRef(0);

  const showToast = useCallback((message, type = 'info', duration = 3500) => {
    const id = ++toastCounter.current;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), duration);
  }, []);

  // ── Real-time polling refs ────────────────────────────────────────────────────
  const pollingRef     = useRef(null);
  const selectedSalaRef = useRef(null);
  const loggedInRef     = useRef(false);

  // Keep ref in sync so the interval closure always has the latest selectedSala
  useEffect(() => { selectedSalaRef.current = selectedSala; }, [selectedSala]);

  // Start real-time polling when logged in, stop on logout
  useEffect(() => {
    if (!currentUser) {
      loggedInRef.current = false;
      if (pollingRef.current) clearInterval(pollingRef.current);
      return;
    }
    loggedInRef.current = true;
    pollingRef.current = setInterval(async () => {
      if (!loggedInRef.current) return;
      try {
        const [resSalas, resMe] = await Promise.all([
          api.get('/salas'),
          api.get('/auth/me'),
        ]);
        setSalas(resSalas.data);
        setCurrentUser(resMe.data); // mantém saldo sempre atualizado
        // If a room is open, silently refresh it too
        const current = selectedSalaRef.current;
        if (current) {
          const r2 = await api.get(`/salas/${current.id}`);
          setSelectedSala(r2.data);
        }
      } catch { /* silent — don't spam toasts on network blip */ }
    }, 3000);
    return () => { if (pollingRef.current) clearInterval(pollingRef.current); };
  }, [currentUser]);

  useEffect(() => { checkSession(); }, []);

  const checkSession = async () => {
    try {
      const res = await api.get('/auth/me');
      setCurrentUser(res.data);
      carregarSalas();
    } catch { setCurrentUser(null); }
  };

  const handleLogin = async () => {
    try {
      setLoading(true);
      const res = await api.post('/auth/login', { username, password });
      setCurrentUser(res.data.user);
      carregarSalas();
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao realizar login', 'error'); }
    finally     { setLoading(false); }
  };

  const handleRegister = async () => {
    try {
      setLoading(true);
      await api.post('/auth/register', { username, password, nome, role });
      showToast('Cadastro realizado com sucesso! Faça login.', 'success');
      setIsRegistering(false);
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao realizar cadastro', 'error'); }
    finally     { setLoading(false); }
  };

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
      setCurrentUser(null);
      setSelectedSala(null);
    } catch (e) { console.error(e); }
  };

  const carregarSalas = async () => {
    try {
      const res = await api.get('/salas');
      setSalas(res.data);
    } catch (e) { console.error('Erro ao carregar salas:', e); }
  };

  const limparSalas = async () => {
    try {
      const res = await api.delete('/salas/admin/limpar');
      showToast(`${res.data.total} salas removidas com sucesso!`, 'success');
      setSelectedSala(null);
      carregarSalas();
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao limpar salas', 'error'); }
  };

  const criarSalaAdmin = async () => {
    if (!novaSalaNome) return showToast('Digite o nome da sala', 'warn');
    try {
      await api.post('/salas/admin', {
        nome: novaSalaNome,
        descricao: 'Sala de Bingo Oficial',
        precoCartela: parseFloat(novaSalaPreco),
        maxCartelas: parseInt(novaSalaMaxCartelas),
      });
      showToast('Sala criada com sucesso!', 'success');
      setNovaSalaNome('');
      carregarSalas();
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao criar sala. Verifique se é Admin.', 'error'); }
  };

  const iniciarJogoAdmin = async (salaId) => {
    try {
      await api.post(`/salas/admin/${salaId}/iniciar`);
      showToast('Jogo iniciado! Os sorteios já podem começar.', 'success');
      carregarSalas();
      atualizarSalaSelecionada(salaId);
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao iniciar jogo', 'error'); }
  };

  const sortearNumeroAdmin = async (salaId) => {
    // Guard: silently block if already 75 numbers drawn (button will be hidden, but belt-and-suspenders)
    if ((selectedSalaRef.current?.numerosSorteados?.length ?? 0) >= 75) return;
    try {
      const res = await api.post(`/salas/admin/${salaId}/sortear`);
      setSelectedSala(res.data);
      carregarSalas();
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao sortear', 'error'); }
  };

  const entrarNaSala = async (sala) => {
    setSelectedSala(sala);
    carregarCartelas(sala.id);
  };

  const atualizarSalaSelecionada = async (salaId) => {
    try {
      const res = await api.get(`/salas/${salaId}`);
      setSelectedSala(res.data);
    } catch (e) { console.error(e); }
  };

  const carregarCartelas = async (salaId) => {
    try {
      const res = await api.get(`/cartelas/sala/${salaId}`);
      setCartelas(res.data);
    } catch (e) { console.error(e); }
  };

  const comprarCartelaAssincrona = async (salaId) => {
    try {
      setLoading(true);
      const res = await api.post('/cartelas/comprar', { salaId });
      const cartelaPendente = res.data;
      showToast('Pedido recebido! Aguardando processamento na fila...', 'info');

      let tentativas = 0;
      const interval = setInterval(async () => {
        tentativas++;
        try {
          const pollRes = await api.get(`/cartelas/${cartelaPendente.id}`);
          if (pollRes.data.status === 'CONFIRMADO' || pollRes.data.status === 'REJEITADO' || tentativas > 15) {
            clearInterval(interval);
            setLoading(false);
            carregarCartelas(salaId);
            atualizarSalaSelecionada(salaId);
            if (pollRes.data.status === 'CONFIRMADO') {
              showToast('Cartela confirmada com sucesso!', 'success', 5000);
              // Atualiza saldo imediatamente após débito confirmado
              try {
                const meRes = await api.get('/auth/me');
                setCurrentUser(meRes.data);
              } catch { /* ignorar — o polling vai pegar no próximo ciclo */ }
            } else {
              showToast(`Compra rejeitada: ${pollRes.data.motivoRejeicao}`, 'error', 6000);
            }
          }
        } catch {
          clearInterval(interval);
          setLoading(false);
        }
      }, 800);
    } catch (e) {
      setLoading(false);
      showToast(e.response?.data?.error || 'Erro ao solicitar cartela', 'error');
    }
  };

  const marcarNumeroCartela = async (cartelaId, numero) => {
    try {
      await api.post(`/cartelas/${cartelaId}/marcar`, { numero });
      carregarCartelas(selectedSala.id);
    } catch (e) { showToast(e.response?.data?.error || 'Erro ao marcar número', 'error'); }
  };

  const baterBingo = async (cartelaId) => {
    try {
      const res = await api.post(`/cartelas/${cartelaId}/validar-bingo`);
      showToast(res.data.mensagem, 'success', 6000);
      atualizarSalaSelecionada(selectedSala.id);
      carregarSalas();
    } catch (e) {
      showToast(e.response?.data?.mensagem || 'Bingo inválido! Continue jogando.', 'error');
    }
  };

  // ── Auth Screen ─────────────────────────────────────────────────────────────
  if (!currentUser) {
    return (
      <View style={ss.authBg}>
        <Toast toasts={toasts} />
        {/* Hero illustration */}
        <Image
          source={require('./assets/02-ilustracoes/bolas-hero.png')}
          style={ss.heroImage}
          resizeMode="contain"
        />

        <View style={ss.authCard}>
          {/* Logo */}
          <Image
            source={require('./assets/01-marca/borabingo-logo.png')}
            style={ss.authLogo}
            resizeMode="contain"
          />
          <Text style={ss.authCaption}>Microsserviços · MongoDB · RabbitMQ</Text>

          {/* Tab switcher */}
          <View style={ss.tabRow}>
            <TouchableOpacity
              style={[ss.tab, !isRegistering && ss.tabActive]}
              onPress={() => setIsRegistering(false)}>
              <Text style={[ss.tabLabel, !isRegistering && ss.tabLabelActive]}>Entrar</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[ss.tab, isRegistering && ss.tabActive]}
              onPress={() => setIsRegistering(true)}>
              <Text style={[ss.tabLabel, isRegistering && ss.tabLabelActive]}>Cadastrar</Text>
            </TouchableOpacity>
          </View>

          {isRegistering && (
            <TextInput
              style={ss.input}
              placeholder="Nome Completo"
              placeholderTextColor={C.muted}
              value={nome}
              onChangeText={setNome}
            />
          )}

          <TextInput
            style={ss.input}
            placeholder="Usuário"
            placeholderTextColor={C.muted}
            value={username}
            onChangeText={setUsername}
            autoCapitalize="none"
          />

          <TextInput
            style={ss.input}
            placeholder="Senha"
            placeholderTextColor={C.muted}
            secureTextEntry
            value={password}
            onChangeText={setPassword}
          />

          {/* Perfil fixado como ROLE_USER no cadastro público — admin criado apenas internamente */}

          <TouchableOpacity
            style={ss.btnPrimary}
            onPress={isRegistering ? handleRegister : handleLogin}
            disabled={loading}>
            {loading
              ? <ActivityIndicator color={C.ink} />
              : <Text style={ss.btnPrimaryText}>{isRegistering ? 'Criar Conta' : 'Entrar no Jogo'}</Text>}
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // ── Main Screen ──────────────────────────────────────────────────────────────
  return (
    <View style={ss.appBg}>
      <Toast toasts={toasts} />
      {/* Header */}
      <View style={ss.header}>
        <View style={ss.headerLeft}>
          <Image
            source={require('./assets/01-marca/borabingo-simbolo.png')}
            style={ss.headerSymbol}
            resizeMode="contain"
          />
          <View>
            <Text style={ss.headerTitle}>BoraBingo</Text>
            <Text style={ss.headerSub}>
              {currentUser.nome}
              {'  '}
              <Text style={ss.headerBalance}>R$ {currentUser.saldo?.toFixed(2)}</Text>
            </Text>
          </View>
        </View>
        <TouchableOpacity style={ss.btnLogout} onPress={handleLogout}>
          <Image
            source={require('./assets/05-icones/png/fechar.png')}
            style={{ width: 16, height: 16, tintColor: C.cream, marginRight: 6 }}
            resizeMode="contain"
          />
          <Text style={ss.btnLogoutText}>Sair</Text>
        </TouchableOpacity>
      </View>

      <ScrollView style={ss.scroll} contentContainerStyle={ss.scrollContent}>
        {/* Admin Panel */}
        {currentUser.role === 'ROLE_ADMIN' && (
          <View style={ss.adminCard}>
            <View style={ss.adminCardTitleRow}>
              <Image
                source={require('./assets/05-icones/png/configuracoes.png')}
                style={{ width: 20, height: 20, tintColor: C.ink, marginRight: 8 }}
                resizeMode="contain"
              />
              <Text style={ss.sectionTitle}>Painel Administrativo</Text>
            </View>
            <View style={ss.adminForm}>
              <TextInput
                style={[ss.input, { flex: 2, marginRight: 8 }]}
                placeholder="Nome da Sala"
                placeholderTextColor={C.muted}
                value={novaSalaNome}
                onChangeText={setNovaSalaNome}
              />
              <TextInput
                style={[ss.input, { flex: 1, marginRight: 8 }]}
                placeholder="Máx."
                placeholderTextColor={C.muted}
                keyboardType="numeric"
                value={novaSalaMaxCartelas}
                onChangeText={setNovaSalaMaxCartelas}
              />
              <TextInput
                style={[ss.input, { flex: 1, marginRight: 8 }]}
                placeholder="Preço"
                placeholderTextColor={C.muted}
                keyboardType="numeric"
                value={novaSalaPreco}
                onChangeText={setNovaSalaPreco}
              />
              <TouchableOpacity style={ss.btnLime} onPress={criarSalaAdmin}>
                <Text style={ss.btnLimeText}>+ Criar Sala</Text>
              </TouchableOpacity>
            </View>
            <TouchableOpacity
              style={[ss.btnLime, { backgroundColor: '#e53e3e', marginTop: 10, alignSelf: 'flex-start' }]}
              onPress={limparSalas}>
              <Text style={ss.btnLimeText}>🗑 Limpar Todas as Salas</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Room list / active room */}
        {!selectedSala ? (
          <View>
            <View style={ss.listHeader}>
              {/* SVG inline — grupo.png original estava desalinhado */}
              <View style={{ width: 22, height: 22, marginRight: 8, marginTop: -1 }}>
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="9" cy="7" r="3.5" stroke={C.purple} strokeWidth="1.8"/>
                  <path d="M2 20c0-3.314 3.134-6 7-6s7 2.686 7 6" stroke={C.purple} strokeWidth="1.8" strokeLinecap="round"/>
                  <circle cx="17.5" cy="7.5" r="2.5" stroke={C.purple} strokeWidth="1.6"/>
                  <path d="M21.5 19c0-2.485-1.82-4.5-4-4.5" stroke={C.purple} strokeWidth="1.6" strokeLinecap="round"/>
                </svg>
              </View>
              <Text style={ss.sectionTitle}>Salas Disponíveis</Text>
            </View>

            <View style={ss.roomGrid}>
              {salas.map((sala) => (
                <View key={sala.id} style={ss.roomCard}>
                  <View style={ss.roomCardTop}>
                    <Text style={ss.roomName}>{sala.nome}</Text>
                    <StatusBadge status={sala.status} />
                  </View>

                  <Text style={ss.roomMeta}>
                    <Text style={ss.roomMetaLabel}>Cartelas: </Text>
                    {sala.cartelasVendidas} / {sala.maxCartelas}
                  </Text>
                  <Text style={ss.roomMeta}>
                    <Text style={ss.roomMetaLabel}>Preço: </Text>
                    R$ {sala.precoCartela?.toFixed(2)}
                  </Text>

                  {sala.vencedorUsername && (
                    <View style={ss.winnerRow}>
                      <Image
                        source={require('./assets/05-icones/png/trofeu.png')}
                        style={{ width: 16, height: 16, tintColor: C.purple, marginRight: 4 }}
                        resizeMode="contain"
                      />
                      <Text style={ss.winnerText}>{sala.vencedorUsername}</Text>
                    </View>
                  )}

                  <View style={ss.roomActions}>
                    <TouchableOpacity style={ss.btnEnter} onPress={() => entrarNaSala(sala)}>
                      <Image
                        source={require('./assets/05-icones/png/jogar.png')}
                        style={{ width: 14, height: 14, tintColor: C.cream, marginRight: 6 }}
                        resizeMode="contain"
                      />
                      <Text style={ss.btnEnterText}>Entrar</Text>
                    </TouchableOpacity>

                    {currentUser.role === 'ROLE_ADMIN' && sala.status === 'CRIADA' && (
                      <TouchableOpacity style={ss.btnStart} onPress={() => iniciarJogoAdmin(sala.id)}>
                        <Text style={ss.btnStartText}>Iniciar</Text>
                      </TouchableOpacity>
                    )}
                  </View>
                </View>
              ))}
            </View>
          </View>
        ) : (
          /* ── Active Game Room ─────────────────────────────────────────────── */
          <View>
            <TouchableOpacity style={ss.btnBack} onPress={() => setSelectedSala(null)}>
              <Text style={ss.btnBackText}>← Voltar para salas</Text>
            </TouchableOpacity>

            <View style={ss.gameCard}>
              {/* Room header */}
              <View style={ss.gameRoomHeader}>
                <Text style={ss.gameRoomTitle}>{selectedSala.nome}</Text>
                <StatusBadge status={selectedSala.status} />
              </View>
              <Text style={ss.roomMeta}>
                Vagas: {selectedSala.cartelasVendidas} / {selectedSala.maxCartelas}
              </Text>

              {/* Admin draw control */}
              {currentUser.role === 'ROLE_ADMIN' && selectedSala.status === 'EM_ANDAMENTO' && (() => {
                const totalSorteados = selectedSala.numerosSorteados?.length ?? 0;
                const esgotado = totalSorteados >= 75;
                return (
                  <TouchableOpacity
                    style={[ss.btnDraw, esgotado && ss.btnDrawDisabled]}
                    onPress={() => !esgotado && sortearNumeroAdmin(selectedSala.id)}
                    activeOpacity={esgotado ? 1 : 0.75}>
                    <Image
                      source={require('./assets/05-icones/png/sorteio.png')}
                      style={{ width: 20, height: 20, tintColor: esgotado ? C.muted : C.ink, marginRight: 8 }}
                      resizeMode="contain"
                    />
                    <Text style={[ss.btnDrawText, esgotado && ss.btnDrawTextDisabled]}>
                      {esgotado ? 'Todos os 75 números sorteados' : 'Sortear Próximo Número'}
                    </Text>
                  </TouchableOpacity>
                );
              })()}

              {/* Drawn balls globe */}
              <View style={ss.globeBox}>
                <Text style={ss.globeTitle}>
                  Números Sorteados ({selectedSala.numerosSorteados?.length || 0}/75)
                </Text>
                <View style={ss.ballsWrap}>
                  {selectedSala.numerosSorteados?.map((num, idx) => (
                    <BingoBall key={idx} number={num} size={52} />
                  ))}
                  {(!selectedSala.numerosSorteados || selectedSala.numerosSorteados.length === 0) && (
                    <Text style={ss.emptyHint}>Nenhum número sorteado ainda.</Text>
                  )}
                </View>
              </View>

              {/* Buy card CTA */}
              {selectedSala.status === 'CRIADA' && (
                <TouchableOpacity
                  style={ss.btnBuyCard}
                  onPress={() => comprarCartelaAssincrona(selectedSala.id)}
                  disabled={loading}>
                  {loading
                    ? <ActivityIndicator color={C.ink} />
                    : (
                      <>
                        <Image
                          source={require('./assets/05-icones/png/cartela.png')}
                          style={{ width: 20, height: 20, tintColor: C.ink, marginRight: 8 }}
                          resizeMode="contain"
                        />
                        <Text style={ss.btnBuyCardText}>Comprar Cartela</Text>
                      </>
                    )}
                </TouchableOpacity>
              )}

              {/* Player cards */}
              <View style={ss.cardsHeader}>
                <Text style={[ss.sectionTitle, { marginBottom: 0 }]}>
                  Suas Cartelas ({cartelas.length})
                </Text>
              </View>

              <View style={ss.cardsGrid}>
                {cartelas.map((c) => (
                  <View key={c.id} style={ss.cartelaCard}>
                    {/* Card header */}
                    <View style={ss.cartelaCardTop}>
                      <Text style={ss.cartelaId}>#{c.id.substring(c.id.length - 6)}</Text>
                      <View style={[
                        ss.cartelaStatusBadge,
                        c.status === 'CONFIRMADO' ? ss.cartelaConfirmada : ss.cartelaPendente,
                      ]}>
                        <Text style={[
                          ss.cartelaStatusText,
                          c.status === 'CONFIRMADO' ? ss.cartelaConfirmadaText : ss.cartelaPendenteText,
                        ]}>
                          {c.status}
                        </Text>
                      </View>
                    </View>

                    {/* 5×5 grid */}
                    {c.matrizNumeros && c.matrizNumeros.length === 5 && (
                      <View style={ss.grid}>
                        {/* B I N G O header */}
                        <View style={ss.gridHeaderRow}>
                          {['B', 'I', 'N', 'G', 'O'].map((l, i) => (
                            <View key={i} style={ss.gridHeaderCell}>
                              <Text style={ss.gridHeaderText}>{l}</Text>
                            </View>
                          ))}
                        </View>

                        {[0, 1, 2, 3, 4].map((rowIdx) => (
                          <View key={rowIdx} style={ss.gridRow}>
                            {[0, 1, 2, 3, 4].map((colIdx) => {
                              const val      = c.matrizNumeros[colIdx][rowIdx];
                              const isFree   = val === 0;
                              const isMarcado = isFree || c.marcacoes?.includes(val);
                              return (
                                <TouchableOpacity
                                  key={colIdx}
                                  style={[ss.gridCell, isMarcado && ss.gridCellMarked]}
                                  onPress={() => !isFree && marcarNumeroCartela(c.id, val)}
                                  activeOpacity={isFree ? 1 : 0.7}>
                                  {isFree ? (
                                    <Image
                                      source={require('./assets/06-interface/png/centro-livre.png')}
                                      style={{ width: 32, height: 32 }}
                                      resizeMode="contain"
                                    />
                                  ) : isMarcado ? (
                                    <Image
                                      source={require('./assets/06-interface/png/marcador-roxo.png')}
                                      style={{ width: 26, height: 26 }}
                                      resizeMode="contain"
                                    />
                                  ) : (
                                    <Text style={ss.gridCellText}>{val}</Text>
                                  )}
                                </TouchableOpacity>
                              );
                            })}
                          </View>
                        ))}
                      </View>
                    )}

                    {/* BINGO button */}
                    {selectedSala.status === 'EM_ANDAMENTO' && c.status === 'CONFIRMADO' && (
                      <TouchableOpacity style={ss.btnBingo} onPress={() => baterBingo(c.id)}>
                        <Image
                          source={require('./assets/02-ilustracoes/selo-bingo.png')}
                          style={{ width: 28, height: 28, marginRight: 8 }}
                          resizeMode="contain"
                        />
                        <Text style={ss.btnBingoText}>BINGO!</Text>
                      </TouchableOpacity>
                    )}
                  </View>
                ))}
              </View>

            </View>
          </View>
        )}
      </ScrollView>
    </View>
  );
}

// ─── Styles ───────────────────────────────────────────────────────────────────
const ss = StyleSheet.create({
  // Auth
  authBg: {
    flex: 1,
    backgroundColor: C.ink,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  heroImage: {
    width: 260,
    height: 160,
    marginBottom: 8,
  },
  authCard: {
    backgroundColor: C.cream,
    borderRadius: 24,
    padding: 28,
    width: '100%',
    maxWidth: 440,
    ...Platform.select({
      web: { boxShadow: '0 12px 40px rgba(34,27,61,0.35)' },
      default: {
        shadowColor: C.ink,
        shadowOpacity: 0.3,
        shadowRadius: 20,
        elevation: 10,
      },
    }),
  },
  authLogo: {
    width: '100%',
    height: 56,
    marginBottom: 4,
  },
  authCaption: {
    fontSize: 11,
    color: C.muted,
    textAlign: 'center',
    marginBottom: 20,
    letterSpacing: 0.4,
  },
  tabRow: {
    flexDirection: 'row',
    backgroundColor: C.tile,
    borderRadius: 12,
    padding: 4,
    marginBottom: 18,
  },
  tab: {
    flex: 1,
    paddingVertical: 9,
    alignItems: 'center',
    borderRadius: 10,
  },
  tabActive: {
    backgroundColor: C.purple,
  },
  tabLabel: {
    fontWeight: '600',
    color: C.muted,
    fontSize: 14,
  },
  tabLabelActive: {
    color: C.cream,
  },
  input: {
    backgroundColor: '#fff',
    borderWidth: 1.5,
    borderColor: C.line,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 12,
    fontSize: 14,
    color: C.ink,
  },
  roleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
    gap: 8,
  },
  roleLabel: {
    fontWeight: '600',
    color: C.ink,
    marginRight: 4,
    fontSize: 13,
  },
  roleChip: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 20,
    borderWidth: 1.5,
    borderColor: C.line,
  },
  roleChipActive: {
    backgroundColor: C.purple,
    borderColor: C.purple,
  },
  roleChipText: { color: C.muted, fontSize: 13, fontWeight: '600' },
  roleChipTextActive: { color: C.cream, fontSize: 13, fontWeight: '700' },
  btnPrimary: {
    backgroundColor: C.lime,
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 4,
    flexDirection: 'row',
    justifyContent: 'center',
  },
  btnPrimaryText: {
    color: C.ink,
    fontWeight: '800',
    fontSize: 15,
    letterSpacing: 0.3,
  },

  // App chrome
  appBg: { flex: 1, backgroundColor: C.tile },
  header: {
    backgroundColor: C.ink,
    paddingHorizontal: 20,
    paddingVertical: 14,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerLeft: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  headerSymbol: { width: 36, height: 36 },
  headerTitle: { color: C.cream, fontSize: 18, fontWeight: '800' },
  headerSub: { color: C.lilac, fontSize: 12, marginTop: 2 },
  headerBalance: { color: C.lime, fontWeight: '700' },
  btnLogout: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#3A2E5C',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 10,
  },
  btnLogoutText: { color: C.cream, fontWeight: '600', fontSize: 13 },
  scroll: { flex: 1 },
  scrollContent: { padding: 20, paddingBottom: 40 },

  // Section header
  sectionTitle: { fontSize: 17, fontWeight: '700', color: C.ink, marginBottom: 12 },
  listHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },

  // Badge
  badge: { paddingHorizontal: 9, paddingVertical: 3, borderRadius: 20 },
  badgeText: { fontSize: 11, fontWeight: '700' },

  // Admin card
  adminCard: {
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 18,
    marginBottom: 20,
    borderLeftWidth: 4,
    borderLeftColor: C.lime,
  },
  adminCardTitleRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  adminForm: { flexDirection: 'row', alignItems: 'center', flexWrap: 'wrap' },
  btnLime: {
    backgroundColor: C.lime,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 10,
    marginBottom: 12,
  },
  btnLimeText: { color: C.ink, fontWeight: '800', fontSize: 13 },

  // Room cards grid
  roomGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 16 },
  roomCard: {
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 18,
    width: 300,
    ...Platform.select({
      web: { boxShadow: '0 2px 12px rgba(101,65,217,0.08)' },
      default: { shadowColor: C.purple, shadowOpacity: 0.08, shadowRadius: 8, elevation: 3 },
    }),
  },
  roomCardTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 },
  roomName: { fontSize: 15, fontWeight: '700', color: C.ink, flex: 1, marginRight: 8 },
  roomMeta: { color: C.muted, fontSize: 13, marginBottom: 3 },
  roomMetaLabel: { fontWeight: '600', color: C.ink },
  winnerRow: { flexDirection: 'row', alignItems: 'center', marginTop: 6 },
  winnerText: { color: C.purple, fontWeight: '700', fontSize: 13 },
  roomActions: { flexDirection: 'row', marginTop: 14, gap: 8 },
  btnEnter: {
    flex: 1,
    flexDirection: 'row',
    backgroundColor: C.purple,
    paddingVertical: 9,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnEnterText: { color: C.cream, fontWeight: '700', fontSize: 13 },
  btnStart: {
    backgroundColor: C.lime,
    paddingHorizontal: 14,
    paddingVertical: 9,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnStartText: { color: C.ink, fontWeight: '700', fontSize: 13 },

  // Back button
  btnBack: { marginBottom: 14 },
  btnBackText: { color: C.purple, fontWeight: '600', fontSize: 14 },

  // Game card
  gameCard: { backgroundColor: '#fff', borderRadius: 20, padding: 20 },
  gameRoomHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 },
  gameRoomTitle: { fontSize: 20, fontWeight: '800', color: C.ink, flex: 1 },

  // Draw button
  btnDraw: {
    backgroundColor: C.lime,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 13,
    borderRadius: 14,
    marginTop: 16,
    marginBottom: 4,
  },
  btnDrawText: { color: C.ink, fontWeight: '800', fontSize: 15 },
  btnDrawDisabled: { backgroundColor: C.tile, borderWidth: 1.5, borderColor: C.line },
  btnDrawTextDisabled: { color: C.muted },

  // Globe / ball display
  globeBox: {
    backgroundColor: C.tile,
    borderRadius: 16,
    padding: 16,
    marginTop: 16,
    marginBottom: 16,
  },
  globeTitle: { fontSize: 14, fontWeight: '700', color: C.ink, marginBottom: 12 },
  ballsWrap: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  emptyHint: { color: C.muted, fontStyle: 'italic', fontSize: 13 },

  // Buy card button
  btnBuyCard: {
    backgroundColor: C.lime,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 14,
    borderRadius: 14,
    marginBottom: 8,
  },
  btnBuyCardText: { color: C.ink, fontWeight: '800', fontSize: 15 },

  // Cards section
  cardsHeader: { flexDirection: 'row', alignItems: 'center', marginTop: 20, marginBottom: 12 },
  cardsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 20 },

  // Individual card
  cartelaCard: {
    backgroundColor: C.cream,
    borderRadius: 16,
    padding: 14,
    width: 290,
    borderWidth: 1.5,
    borderColor: C.line,
  },
  cartelaCardTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  cartelaId: { fontWeight: '700', color: C.ink, fontSize: 14 },
  cartelaStatusBadge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 20 },
  cartelaStatusText: { fontSize: 11, fontWeight: '700' },
  cartelaConfirmada: { backgroundColor: '#D8F36A33' },
  cartelaConfirmadaText: { color: '#3A6600' },
  cartelaPendente: { backgroundColor: '#BAA5F533' },
  cartelaPendenteText: { color: C.purple },

  // 5×5 grid
  grid: { borderRadius: 12, overflow: 'hidden', borderWidth: 1.5, borderColor: C.line },
  gridHeaderRow: { flexDirection: 'row', backgroundColor: C.purple },
  gridHeaderCell: { flex: 1, paddingVertical: 6, alignItems: 'center' },
  gridHeaderText: { color: C.cream, fontWeight: '900', fontSize: 15 },
  gridRow: { flexDirection: 'row' },
  gridCell: {
    flex: 1,
    height: 46,
    borderWidth: 0.5,
    borderColor: C.line,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  gridCellMarked: { backgroundColor: C.tile },
  gridCellText: { fontSize: 14, fontWeight: '600', color: C.ink },

  // BINGO claim button
  btnBingo: {
    backgroundColor: C.purple,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 14,
    paddingVertical: 12,
    borderRadius: 12,
  },
  btnBingoText: { color: C.lime, fontWeight: '900', fontSize: 17, letterSpacing: 1 },
});
