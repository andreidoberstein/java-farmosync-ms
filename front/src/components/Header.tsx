import { Activity, User, ShoppingCart, FileText, Package, ShieldCheck } from 'lucide-react'

interface HeaderProps {
  activeTab: 'pdv' | 'receitas' | 'estoque' | 'auditoria';
  setActiveTab: (tab: 'pdv' | 'receitas' | 'estoque' | 'auditoria') => void;
  possuiControladoNoCarrinho: boolean;
  receitaStatus: 'NENHUMA' | 'PENDING' | 'APPROVED' | 'REJECTED';
  dlqCount: number;
}

export function Header({
  activeTab,
  setActiveTab,
  possuiControladoNoCarrinho,
  receitaStatus,
  dlqCount
}: HeaderProps) {
  return (
    <>
      <header className="border-b border-slate-900 bg-slate-900/40 backdrop-blur-md sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-500 flex items-center justify-center shadow-lg shadow-emerald-500/20">
              <Activity className="h-6 w-6 text-slate-950 stroke-[2.5]" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-emerald-400 to-teal-200 bg-clip-text text-transparent">
                FarmoSync
              </span>
              <span className="text-[10px] block font-mono text-emerald-500 font-bold uppercase tracking-wider">
                PDV & Regulação Integrada
              </span>
            </div>
          </div>

          <div className="flex items-center gap-6">
            <div className="hidden sm:flex items-center gap-2 bg-slate-900 border border-slate-800 rounded-full px-3 py-1 text-xs">
              <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span className="text-slate-400 font-medium">Gateway de Microsserviços Online</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-8 w-8 rounded-full bg-slate-800 flex items-center justify-center border border-slate-700">
                <User className="h-4 w-4 text-slate-300" />
              </div>
              <div className="hidden md:block text-left">
                <p className="text-xs font-semibold text-slate-300 leading-tight">André Santos</p>
                <p className="text-[10px] text-slate-500">Operador / Farmacêutico</p>
              </div>
            </div>
          </div>
        </div>
      </header>

      <nav className="bg-slate-950 py-4 border-b border-slate-900/60 sticky top-16 z-30 backdrop-blur-md">
        <div className="max-w-7xl mx-auto px-4 flex gap-2 overflow-x-auto">
          <button
            onClick={() => setActiveTab('pdv')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 whitespace-nowrap ${
              activeTab === 'pdv'
                ? 'bg-emerald-500 text-slate-950 shadow-md shadow-emerald-500/25'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <ShoppingCart className="h-4 w-4" />
            Caixa / PDV
          </button>
          
          <button
            onClick={() => setActiveTab('receitas')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 relative whitespace-nowrap ${
              activeTab === 'receitas'
                ? 'bg-emerald-500 text-slate-950 shadow-md shadow-emerald-500/25'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <FileText className="h-4 w-4" />
            Receita Controlada
            {possuiControladoNoCarrinho && receitaStatus !== 'APPROVED' && (
              <span className="absolute -top-1 -right-1 h-3.5 w-3.5 rounded-full bg-rose-500 text-[9px] text-white flex items-center justify-center font-bold animate-bounce">
                !
              </span>
            )}
          </button>

          <button
            onClick={() => setActiveTab('estoque')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 whitespace-nowrap ${
              activeTab === 'estoque'
                ? 'bg-emerald-500 text-slate-950 shadow-md shadow-emerald-500/25'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <Package className="h-4 w-4" />
            Controle de Lotes
          </button>

          <button
            onClick={() => setActiveTab('auditoria')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all duration-300 relative whitespace-nowrap ${
              activeTab === 'auditoria'
                ? 'bg-emerald-500 text-slate-950 shadow-md shadow-emerald-500/25'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <ShieldCheck className="h-4 w-4" />
            Auditoria Outbox / Kafka
            {dlqCount > 0 && (
              <span className="h-5 px-1.5 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/30 text-[10px] flex items-center justify-center font-mono font-bold ml-1">
                {dlqCount} FAI
              </span>
            )}
          </button>
        </div>
      </nav>
    </>
  )
}
