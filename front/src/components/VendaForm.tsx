import { ShoppingCart, Trash2, CheckCircle2, RefreshCw, AlertTriangle, FileText, Check } from 'lucide-react'

interface Produto {
  id: string;
  nome: string;
  preco: number;
  controlado: boolean;
  tarja: string;
  estoque: number;
}

interface CartItem {
  produto: Produto;
  quantidade: number;
}

interface VendaFormProps {
  cart: CartItem[];
  removeFromCart: (id: string) => void;
  possuiControladoNoCarrinho: boolean;
  receitaStatus: 'NENHUMA' | 'PENDING' | 'APPROVED' | 'REJECTED';
  cpf: string;
  setCpf: (cpf: string) => void;
  processarVenda: () => void;
  setShowReceitaModal: (show: boolean) => void;
}

export function VendaForm({
  cart,
  removeFromCart,
  possuiControladoNoCarrinho,
  receitaStatus,
  cpf,
  setCpf,
  processarVenda,
  setShowReceitaModal
}: VendaFormProps) {
  const total = cart.reduce((acc, item) => acc + (item.produto.preco * item.quantidade), 0)

  const formatarCpf = (value: string): string => {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    return digits
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
  }

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-white flex items-center gap-2">
        <ShoppingCart className="text-emerald-400 h-5 w-5" /> Cupom Fiscal
      </h2>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-6 flex flex-col justify-between shadow-xl">
        <div className="space-y-4 min-h-[220px]">
          {cart.length === 0 ? (
            <div className="text-center py-12 space-y-3">
              <ShoppingCart className="h-10 w-10 text-slate-700 mx-auto stroke-[1.5]" />
              <p className="text-sm text-slate-500 font-medium">Carrinho de compras vazio</p>
            </div>
          ) : (
            cart.map((item) => (
              <div key={item.produto.id} className="flex items-center justify-between bg-slate-950/60 border border-slate-850 p-3 rounded-lg">
                <div className="space-y-1">
                  <h4 className="text-xs font-bold text-slate-200 line-clamp-1">{item.produto.nome}</h4>
                  <div className="flex items-center gap-2 text-[10px] text-slate-500 font-semibold font-mono">
                    <span>{item.quantidade}x R$ {item.produto.preco.toFixed(2)}</span>
                    {item.produto.controlado && (
                      <span className="text-rose-400 font-bold uppercase text-[9px] bg-rose-500/10 border border-rose-500/20 px-1 rounded">Preta</span>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-xs font-bold text-white font-mono">
                    R$ {(item.produto.preco * item.quantidade).toFixed(2)}
                  </span>
                  <button 
                    onClick={() => removeFromCart(item.produto.id)}
                    className="text-slate-600 hover:text-rose-400 transition-colors p-1"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {possuiControladoNoCarrinho && (
          <div className={`p-4 rounded-xl border flex flex-col gap-3 transition-all duration-300 ${
            receitaStatus === 'APPROVED'
              ? 'bg-emerald-500/5 border-emerald-500/20 text-emerald-400'
              : receitaStatus === 'PENDING'
              ? 'bg-amber-500/5 border-amber-500/20 text-amber-400 animate-pulse'
              : 'bg-rose-500/5 border-rose-500/20 text-rose-400'
          }`}>
            <div className="flex items-start gap-2.5">
              {receitaStatus === 'APPROVED' ? (
                <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-500 mt-0.5" />
              ) : receitaStatus === 'PENDING' ? (
                <RefreshCw className="h-5 w-5 shrink-0 text-amber-500 mt-0.5 animate-spin" />
              ) : (
                <AlertTriangle className="h-5 w-5 shrink-0 text-rose-500 mt-0.5" />
              )}
              
              <div className="space-y-1">
                <p className="text-xs font-bold text-white">Requer Receita Médica</p>
                <p className="text-[11px] leading-relaxed text-slate-400">
                  {receitaStatus === 'APPROVED' 
                    ? 'Receita validada e vinculada à venda com sucesso via prescription-service.'
                    : receitaStatus === 'PENDING'
                    ? 'Validando assinatura digital e CRM no barramento do Kafka...'
                    : 'Esta venda possui medicamentos restritos. Você deve anexar os dados da receita.'}
                </p>
              </div>
            </div>

            {receitaStatus !== 'APPROVED' && receitaStatus !== 'PENDING' && (
              <button
                onClick={() => setShowReceitaModal(true)}
                className="w-full bg-rose-500 hover:bg-rose-400 text-white text-xs font-bold py-2 rounded-lg transition-colors flex items-center justify-center gap-1.5 shadow-md shadow-rose-500/10"
              >
                <FileText className="h-3.5 w-3.5" />
                Vincular Receita Médica
              </button>
            )}
          </div>
        )}

        <div className="border-t border-slate-800/80 pt-4 space-y-2 text-xs font-medium text-slate-400">
          <div className="flex justify-between">
            <span>Subtotal</span>
            <span className="font-mono">R$ {total.toFixed(2)}</span>
          </div>
          <div className="flex justify-between">
            <span>Taxas Sanitárias (LGPD)</span>
            <span className="font-mono text-emerald-400">R$ 0,00</span>
          </div>
          
          <div className="pt-2">
            <label className="text-[10px] font-bold uppercase tracking-wider block text-slate-500 mb-1">CPF do Cliente</label>
            <input
              type="text"
              placeholder="000.000.000-00"
              value={cpf}
              onChange={(e) => setCpf(formatarCpf(e.target.value))}
              maxLength={14}
              className="w-full bg-slate-950 border border-slate-850 rounded-lg px-3 py-2 text-xs text-slate-200 placeholder-slate-650 focus:outline-none focus:border-emerald-500 transition-colors"
            />
          </div>

          <div className="border-t border-slate-800 pt-4 flex justify-between items-baseline">
            <span className="text-sm font-bold text-white">Valor Total</span>
            <span className="text-2xl font-extrabold text-emerald-400 font-mono">
              R$ {total.toFixed(2)}
            </span>
          </div>
        </div>

        <button
          onClick={processarVenda}
          disabled={cart.length === 0 || (possuiControladoNoCarrinho && receitaStatus !== 'APPROVED')}
          className={`w-full text-sm font-bold py-3.5 rounded-xl transition-all duration-300 mt-6 flex items-center justify-center gap-2 shadow-lg ${
            cart.length === 0 || (possuiControladoNoCarrinho && receitaStatus !== 'APPROVED')
              ? 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-750/30'
              : 'bg-emerald-500 text-slate-950 hover:bg-emerald-400 hover:shadow-emerald-500/20 active:scale-[0.98]'
          }`}
        >
          <Check className="h-4 w-4 stroke-[2.5]" />
          Finalizar Venda (F3)
        </button>
      </div>
    </div>
  )
}
