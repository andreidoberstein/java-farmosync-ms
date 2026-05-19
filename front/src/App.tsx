import { useState } from 'react'
import { 
  FileText, 
  Package, 
  Activity, 
  RefreshCw, 
  ShieldCheck,
  CheckCircle2,
  XCircle,
  AlertTriangle
} from 'lucide-react'
import { Header } from './components/Header'
import { VendaList } from './components/VendaList'
import { VendaForm } from './components/VendaForm'
import { ReceitaModal } from './components/ReceitaModal'

// --- DADOS DE EXEMPLO (MOCK DATA) ---
const PRODUTOS_BASE = [
  { id: '1', nome: 'Dipirona Monoidratada 500mg', preco: 12.50, controlado: false, tarja: 'Livre', estoque: 150 },
  { id: '2', nome: 'Paracetamol 750mg (Blister)', preco: 8.90, controlado: false, tarja: 'Livre', estoque: 220 },
  { id: '3', nome: 'Clonazepam 2mg (Rivotril)', preco: 24.99, controlado: true, tarja: 'Preta', estoque: 45 },
  { id: '4', nome: 'Amoxicilina + Clavulanato 500mg', preco: 48.50, controlado: true, tarja: 'Vermelha (Retida)', estoque: 32 },
  { id: '5', nome: 'Ibuprofeno 600mg', preco: 18.20, controlado: false, tarja: 'Livre', estoque: 98 },
  { id: '6', nome: 'Dramin B6 (Gotas)', preco: 15.40, controlado: false, tarja: 'Livre', estoque: 60 }
]

const LOTES_BASE = [
  { id: 'L1', produto: 'Clonazepam 2mg', codigo: 'CLO-2026A', quantidade: 45, validade: '12/08/2026', status: 'OK' },
  { id: 'L2', produto: 'Amoxicilina + Clavulanato', codigo: 'AMX-2026B', quantidade: 32, validade: '30/09/2026', status: 'OK' },
  { id: 'L3', produto: 'Dipirona 500mg', codigo: 'DIP-2026F', quantidade: 150, validade: '15/02/2026', status: 'ALERTA (Próximo)' },
  { id: 'L4', produto: 'Paracetamol 750mg', codigo: 'PAR-2026K', quantidade: 220, validade: '01/01/2027', status: 'OK' }
]

const EVENTOS_OUTBOX_BASE = [
  { id: 'evt_8f172a8c', aggregateType: 'Venda', eventType: 'VendaEmitidaEvent', payload: '{"vendaId":"vnd_8f12","total":61.00}', status: 'PROCESSED', timestamp: 'Agora mesmo' },
  { id: 'evt_3c29da1b', aggregateType: 'Prescription', eventType: 'ReceitaValidadaEvent', payload: '{"receitaId":"rc_1234","status":"APPROVED"}', status: 'PROCESSED', timestamp: '1 min atrás' },
  { id: 'evt_9b18c642', aggregateType: 'Inventory', eventType: 'EstoqueBaixadoEvent', payload: '{"lote":"CLO-2026A","qtd":1}', status: 'PROCESSED', timestamp: '1 min atrás' },
  { id: 'evt_failed_01', aggregateType: 'Venda', eventType: 'VendaEmitidaEvent', payload: '{"vendaId":"vnd_9999","total":48.50}', status: 'FAILED', timestamp: '5 min atrás' }
]

function App() {
  const [activeTab, setActiveTab] = useState<'pdv' | 'receitas' | 'estoque' | 'auditoria'>('pdv')
  const [searchTerm, setSearchTerm] = useState('')
  const [cart, setCart] = useState<{ produto: typeof PRODUTOS_BASE[0]; quantidade: number }[]>([])
  const [cpf, setCpf] = useState('')
  
  // Estados para simulação de Receita Médica
  const [medicoNome, setMedicoNome] = useState('')
  const [crm, setCrm] = useState('')
  const [crmUf, setCrmUf] = useState('SP')
  const [receitaStatus, setReceitaStatus] = useState<'NENHUMA' | 'PENDING' | 'APPROVED' | 'REJECTED'>('NENHUMA')
  const [receitaMotivo, setReceitaMotivo] = useState('')
  const [showReceitaModal, setShowReceitaModal] = useState(false)

  // Estados de estoque e auditoria dinâmicos
  const [lotes, setLotes] = useState(LOTES_BASE)
  const [eventosOutbox, setEventosOutbox] = useState(EVENTOS_OUTBOX_BASE)
  const [dlqCount, setDlqCount] = useState(1)

  // Filtro de produtos
  const produtosFiltrados = PRODUTOS_BASE.filter(p => 
    p.nome.toLowerCase().includes(searchTerm.toLowerCase())
  )

  // Verifica se o carrinho possui algum medicamento controlado
  const possuiControladoNoCarrinho = cart.some(item => item.produto.controlado)

  // Adicionar ao carrinho
  const addToCart = (produto: typeof PRODUTOS_BASE[0]) => {
    const existing = cart.find(item => item.produto.id === produto.id)
    if (existing) {
      setCart(cart.map(item => 
        item.produto.id === produto.id 
          ? { ...item, quantidade: item.quantidade + 1 }
          : item
      ))
    } else {
      setCart([...cart, { produto, quantidade: 1 }])
    }
  }

  // Remover do carrinho
  const removeFromCart = (id: string) => {
    setCart(cart.filter(item => item.produto.id !== id))
  }

  // Simular a auditoria assíncrona do Kafka + prescription-service
  const submeterReceitaParaAuditoria = (e: React.FormEvent) => {
    e.preventDefault()
    if (!medicoNome || !crm) {
      alert('Por favor, preencha todos os campos da receita!')
      return
    }

    setReceitaStatus('PENDING')
    setShowReceitaModal(false)

    // Simula a latência de processamento de filas assíncronas do Kafka (2 segundos)
    setTimeout(() => {
      if (crm === '99999') {
        // Simulação de CRM inválido ou receita rejeitada
        setReceitaStatus('REJECTED')
        setReceitaMotivo('CRM não localizado no Conselho Regional de Medicina Federal.')
        
        // Adiciona evento de falha na auditoria
        const newEvent = {
          id: `evt_${Math.random().toString(36).substr(2, 8)}`,
          aggregateType: 'Prescription',
          eventType: 'ReceitaValidadaEvent',
          payload: `{"crm":"${crm}","status":"REJECTED","motivo":"CRM inválido"}`,
          status: 'FAILED',
          timestamp: 'Agora mesmo'
        }
        setEventosOutbox(prev => [newEvent, ...prev])
        setDlqCount(prev => prev + 1)
      } else {
        // Sucesso de validação
        setReceitaStatus('APPROVED')
        
        // Adiciona evento de sucesso na auditoria
        const newEvent = {
          id: `evt_${Math.random().toString(36).substr(2, 8)}`,
          aggregateType: 'Prescription',
          eventType: 'ReceitaValidadaEvent',
          payload: `{"crm":"${crm}","status":"APPROVED"}`,
          status: 'PROCESSED',
          timestamp: 'Agora mesmo'
        }
        setEventosOutbox(prev => [newEvent, ...prev])
      }
    }, 2000)
  }

  // Simular processamento/finalização de Venda
  const processarVenda = () => {
    if (cart.length === 0) {
      alert('O carrinho está vazio!')
      return
    }
    if (possuiControladoNoCarrinho && receitaStatus !== 'APPROVED') {
      alert('Esta venda contém medicamentos CONTROLADOS. Você precisa ter uma Receita Médica APROVADA antes de prosseguir!')
      return
    }

    const total = cart.reduce((acc, item) => acc + (item.produto.preco * item.quantidade), 0)
    
    // Adiciona evento de venda processada
    const newVendaEvent = {
      id: `evt_${Math.random().toString(36).substr(2, 8)}`,
      aggregateType: 'Venda',
      eventType: 'VendaEmitidaEvent',
      payload: `{"vendaId":"vnd_${Math.random().toString(36).substr(2, 4)}","total":${total.toFixed(2)},"cpf":"${cpf}"}`,
      status: 'PROCESSED',
      timestamp: 'Agora mesmo'
    }

    // Baixa física simulada de estoque nos lotes
    cart.forEach(item => {
      setLotes(prev => prev.map(l => {
        if (l.produto.includes(item.produto.nome.split(' ')[0])) {
          return { ...l, quantidade: Math.max(0, l.quantidade - item.quantidade) }
        }
        return l
      }))
    })

    setEventosOutbox(prev => [newVendaEvent, ...prev])
    setCart([])
    setCpf('')
    setReceitaStatus('NENHUMA')
    setMedicoNome('')
    setCrm('')
    alert('Venda finalizada com sucesso! A transação foi registrada no Outbox e o estoque foi decrementado via Kafka.')
  }

  // Reprocessar evento de DLQ
  const reprocessarEvento = (id: string) => {
    setEventosOutbox(prev => prev.map(evt => {
      if (evt.id === id) {
        return { ...evt, status: 'PROCESSED' }
      }
      return evt
    }))
    if (id === 'evt_failed_01') {
      setDlqCount(prev => Math.max(0, prev - 1))
    }
    alert('Evento reenviado da DLQ com sucesso para processamento definitivo!')
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans flex flex-col antialiased">
      <Header 
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        possuiControladoNoCarrinho={possuiControladoNoCarrinho}
        receitaStatus={receitaStatus}
        dlqCount={dlqCount}
      />

      {/* --- MAIN CONTENT --- */}
      <main className="flex-grow max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* TAB 1: CAIXA / PDV */}
        {activeTab === 'pdv' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <VendaList 
              produtosFiltrados={produtosFiltrados}
              searchTerm={searchTerm}
              setSearchTerm={setSearchTerm}
              addToCart={addToCart}
            />

            <VendaForm 
              cart={cart}
              removeFromCart={removeFromCart}
              possuiControladoNoCarrinho={possuiControladoNoCarrinho}
              receitaStatus={receitaStatus}
              cpf={cpf}
              setCpf={setCpf}
              processarVenda={processarVenda}
              setShowReceitaModal={setShowReceitaModal}
            />
          </div>
        )}

        {/* TAB 2: RECEITAS CONTROLADAS */}
        {activeTab === 'receitas' && (
          <div className="max-w-3xl mx-auto space-y-8">
            <div className="flex flex-col gap-2">
              <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
                <FileText className="text-emerald-400 h-6 w-6" /> Auditoria da Receita Médica
              </h1>
              <p className="text-xs text-slate-400">
                Os dados inseridos aqui são assinados digitalmente e auditados de forma assíncrona pelo microsserviço de regulação.
              </p>
            </div>

            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-xl space-y-6">
              <form onSubmit={submeterReceitaParaAuditoria} className="space-y-5">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Nome do Médico Prescritor</label>
                    <input
                      type="text"
                      placeholder="Dr. Alexandre de Souza"
                      value={medicoNome}
                      onChange={(e) => setMedicoNome(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-850 rounded-xl px-4 py-3 text-sm text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
                      required
                    />
                  </div>

                  <div className="grid grid-cols-3 gap-2">
                    <div className="col-span-2 space-y-1">
                      <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">CRM</label>
                      <input
                        type="text"
                        placeholder="12345"
                        value={crm}
                        onChange={(e) => setCrm(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-850 rounded-xl px-4 py-3 text-sm text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
                        required
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">UF</label>
                      <select
                        value={crmUf}
                        onChange={(e) => setCrmUf(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-850 rounded-xl px-2 py-3 text-sm text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
                      >
                        <option value="SP">SP</option>
                        <option value="RJ">RJ</option>
                        <option value="MG">MG</option>
                        <option value="PR">PR</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div className="p-4 bg-slate-950 border border-slate-850 rounded-xl space-y-2">
                  <p className="text-xs font-bold text-slate-300">ℹ️ Simule Cenários de Testes</p>
                  <p className="text-[11px] leading-relaxed text-slate-500">
                    Digite o CRM <code className="text-amber-400 bg-slate-900 border border-slate-800 px-1 py-0.5 rounded">99999</code> para simular uma **receita rejeitada** por inconsistência cadastral. Qualquer outro CRM simulará uma receita **aprovada** pela ANVISA via fila do Kafka.
                  </p>
                </div>

                <button
                  type="submit"
                  disabled={receitaStatus === 'PENDING'}
                  className={`w-full font-bold py-3.5 rounded-xl transition-all duration-300 flex items-center justify-center gap-2 ${
                    receitaStatus === 'PENDING'
                      ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                      : 'bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-md shadow-emerald-500/15'
                  }`}
                >
                  {receitaStatus === 'PENDING' ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin text-slate-500" />
                      Processando no barramento Kafka...
                    </>
                  ) : (
                    <>
                      <ShieldCheck className="h-4 w-4 stroke-[2.5]" />
                      Submeter para Validação na ANVISA
                    </>
                  )}
                </button>
              </form>

              {receitaStatus !== 'NENHUMA' && (
                <div className={`mt-8 p-6 rounded-2xl border flex items-start gap-4 transition-all duration-300 ${
                  receitaStatus === 'APPROVED'
                    ? 'bg-emerald-500/5 border-emerald-500/25 text-emerald-400'
                    : receitaStatus === 'PENDING'
                    ? 'bg-amber-500/5 border-amber-500/25 text-amber-400'
                    : 'bg-rose-500/5 border-rose-500/25 text-rose-400'
                }`}>
                  {receitaStatus === 'APPROVED' && <CheckCircle2 className="h-7 w-7 text-emerald-500 shrink-0 mt-0.5" />}
                  {receitaStatus === 'PENDING' && <RefreshCw className="h-7 w-7 text-amber-500 shrink-0 mt-0.5 animate-spin" />}
                  {receitaStatus === 'REJECTED' && <XCircle className="h-7 w-7 text-rose-500 shrink-0 mt-0.5" />}

                  <div className="space-y-2">
                    <h3 className="font-extrabold text-white text-base">
                      {receitaStatus === 'APPROVED' && 'Receita Aprovada Digitalmente'}
                      {receitaStatus === 'PENDING' && 'Auditoria de Assinatura Sanitária em Progresso'}
                      {receitaStatus === 'REJECTED' && 'Receita Rejeitada Sanitariamente'}
                    </h3>
                    
                    <div className="text-xs text-slate-400 space-y-1">
                      <p><strong>Médico:</strong> {medicoNome} (CRM-{crmUf}: {crm})</p>
                      <p><strong>Método de Auditoria:</strong> Assinatura Digital ICP-Brasil (Assíncrona)</p>
                      {receitaStatus === 'REJECTED' && (
                        <p className="mt-2 p-3 bg-rose-500/10 border border-rose-500/20 text-rose-400 rounded-lg text-xs leading-relaxed">
                          <strong>Erro Sanitário:</strong> {receitaMotivo}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 3: CONTROLE DE LOTES */}
        {activeTab === 'estoque' && (
          <div className="space-y-6">
            <div className="flex flex-col gap-2">
              <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
                <Package className="text-emerald-400 h-6 w-6" /> Controle de Lotes Físicos (Inventory)
              </h1>
              <p className="text-xs text-slate-400">
                Gestão sanitária de validade e baixa física atômica de lotes integrados.
              </p>
            </div>

            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-950 border-b border-slate-850 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                    <th className="px-6 py-4">ID</th>
                    <th className="px-6 py-4">Medicamento</th>
                    <th className="px-6 py-4">Código Lote</th>
                    <th className="px-6 py-4">Estoque Atual</th>
                    <th className="px-6 py-4">Vencimento</th>
                    <th className="px-6 py-4">Status de Alerta</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-xs font-semibold text-slate-300">
                  {lotes.map((lote) => (
                    <tr key={lote.id} className="hover:bg-slate-900/60 transition-colors">
                      <td className="px-6 py-4 text-slate-500 font-mono">#{lote.id}</td>
                      <td className="px-6 py-4 text-white font-bold">{lote.produto}</td>
                      <td className="px-6 py-4"><span className="bg-slate-950 border border-slate-800 rounded px-2 py-0.5 font-mono text-emerald-400">{lote.codigo}</span></td>
                      <td className="px-6 py-4 font-mono text-sm">{lote.quantidade} un</td>
                      <td className="px-6 py-4 font-mono">{lote.validade}</td>
                      <td className="px-6 py-4">
                        <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                          lote.status === 'OK'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : 'bg-amber-500/10 text-amber-400 border border-amber-500/30'
                        }`}>
                          {lote.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 4: AUDITORIA DO OUTBOX / KAFKA */}
        {activeTab === 'auditoria' && (
          <div className="space-y-8">
            <div className="flex flex-col gap-2">
              <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
                <Activity className="text-emerald-400 h-6 w-6" /> Observabilidade do Outbox & Eventos
              </h1>
              <p className="text-xs text-slate-400">
                Lista de transações no banco de dados MongoDB integradas com entrega garantida para o Kafka (At-Least-Once).
              </p>
            </div>

            {/* KPI Cards de Resiliência */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-slate-900 border border-slate-850 p-6 rounded-2xl flex items-center justify-between shadow-lg">
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Total de Eventos</p>
                  <p className="text-2xl font-extrabold text-white">{eventosOutbox.length}</p>
                </div>
                <div className="h-10 w-10 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-center">
                  <ShieldCheck className="h-5 w-5 text-emerald-400" />
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-850 p-6 rounded-2xl flex items-center justify-between shadow-lg">
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Eventos DLQ FAILED</p>
                  <p className={`text-2xl font-extrabold ${dlqCount > 0 ? 'text-rose-500 animate-pulse' : 'text-slate-500'}`}>
                    {dlqCount}
                  </p>
                </div>
                <div className="h-10 w-10 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-center">
                  <AlertTriangle className={`h-5 w-5 ${dlqCount > 0 ? 'text-rose-500' : 'text-slate-600'}`} />
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-850 p-6 rounded-2xl flex items-center justify-between shadow-lg">
                <div className="space-y-1">
                  <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Consistência Transacional</p>
                  <p className="text-2xl font-extrabold text-emerald-400 font-mono">100%</p>
                </div>
                <div className="h-10 w-10 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-center">
                  <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                </div>
              </div>
            </div>

            {/* Tabela do Outbox */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-950 border-b border-slate-850 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                    <th className="px-6 py-4">ID Evento</th>
                    <th className="px-6 py-4">Agregado</th>
                    <th className="px-6 py-4">Tipo do Evento</th>
                    <th className="px-6 py-4">Payload</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4">Reprocessar</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-xs font-semibold text-slate-300">
                  {eventosOutbox.map((evt) => (
                    <tr key={evt.id} className="hover:bg-slate-900/60 transition-colors">
                      <td className="px-6 py-4 text-slate-500 font-mono text-[10px]">{evt.id}</td>
                      <td className="px-6 py-4">{evt.aggregateType}</td>
                      <td className="px-6 py-4 text-slate-200">{evt.eventType}</td>
                      <td className="px-6 py-4"><code className="bg-slate-950 text-slate-400 border border-slate-800 rounded px-1.5 py-1 text-[10px] font-mono leading-none block overflow-x-auto max-w-[220px]">{evt.payload}</code></td>
                      <td className="px-6 py-4">
                        <span className={`px-2 py-0.5 rounded-full text-[9px] font-bold uppercase tracking-wider ${
                          evt.status === 'PROCESSED'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/25'
                            : 'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                        }`}>
                          {evt.status}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        {evt.status === 'FAILED' ? (
                          <button
                            onClick={() => reprocessarEvento(evt.id)}
                            className="flex items-center gap-1 bg-amber-500 hover:bg-amber-400 text-slate-950 text-[10px] font-bold px-2.5 py-1.5 rounded transition-colors"
                          >
                            <RefreshCw className="h-3 w-3" />
                            Retry
                          </button>
                        ) : (
                          <span className="text-[10px] text-slate-600 font-normal">Nenhuma ação</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      <ReceitaModal 
        showReceitaModal={showReceitaModal}
        setShowReceitaModal={setShowReceitaModal}
        medicoNome={medicoNome}
        setMedicoNome={setMedicoNome}
        crm={crm}
        setCrm={setCrm}
        crmUf={crmUf}
        setCrmUf={setCrmUf}
        submeterReceitaParaAuditoria={submeterReceitaParaAuditoria}
      />

      {/* --- FOOTER --- */}
      <footer className="bg-slate-950 py-8 border-t border-slate-900/60 mt-12 text-center text-xs text-slate-650 font-medium">
        <div className="max-w-7xl mx-auto px-4 space-y-2">
          <p>© 2026 FarmoSync Inc. Todos os direitos reservados.</p>
          <p className="text-[10px] text-slate-700 font-mono">
            Arquitetura Monorepo baseada em DDD & Clean Architecture com Event Sourcing no Kafka.
          </p>
        </div>
      </footer>
    </div>
  )
}

export default App
