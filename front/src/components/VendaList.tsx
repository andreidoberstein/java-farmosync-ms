import { ShoppingCart, Search, Plus } from 'lucide-react'

interface Produto {
  id: string;
  nome: string;
  preco: number;
  controlado: boolean;
  tarja: string;
  estoque: number;
}

interface VendaListProps {
  produtosFiltrados: Produto[];
  searchTerm: string;
  setSearchTerm: (term: string) => void;
  addToCart: (produto: Produto) => void;
}

export function VendaList({
  produtosFiltrados,
  searchTerm,
  setSearchTerm,
  addToCart
}: VendaListProps) {
  return (
    <div className="lg:col-span-2 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <ShoppingCart className="text-emerald-400 h-6 w-6" /> Registrador de Venda
        </h1>
        <p className="text-xs text-slate-500">Selecione os medicamentos para o carrinho</p>
      </div>

      <div className="relative">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 h-4.5 w-4.5" />
        <input
          type="text"
          placeholder="Pesquisar por medicamento (ex: Rivotril, Amoxicilina, Dipirona)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full bg-slate-900 border border-slate-800 rounded-xl py-3 pl-11 pr-4 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition-all duration-300"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {produtosFiltrados.map((produto) => (
          <div 
            key={produto.id} 
            className={`bg-slate-900/60 border rounded-xl p-5 flex flex-col justify-between hover:border-slate-700 hover:bg-slate-900 transition-all duration-300 relative overflow-hidden group ${
              produto.controlado ? 'border-rose-500/10' : 'border-slate-850'
            }`}
          >
            {produto.controlado && (
              <div className="absolute top-0 right-0 bg-rose-500 text-[10px] text-white font-bold px-3 py-1 rounded-bl-lg uppercase tracking-wider">
                Tarja {produto.tarja}
              </div>
            )}
            
            <div className="space-y-2">
              <p className="text-xs font-semibold font-mono text-slate-500">ID: #{produto.id}</p>
              <h3 className="font-bold text-white text-base leading-snug group-hover:text-emerald-400 transition-colors">
                {produto.nome}
              </h3>
              <div className="flex gap-2">
                <span className={`text-[10px] px-2 py-0.5 rounded-full font-semibold uppercase ${
                  produto.controlado 
                    ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' 
                    : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                }`}>
                  {produto.controlado ? 'Controlado Retido' : 'Venda Livre'}
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                  Qtd: {produto.estoque} un
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between mt-6 pt-3 border-t border-slate-800/60">
              <span className="text-xl font-extrabold text-white">
                R$ {produto.preco.toFixed(2)}
              </span>
              <button
                onClick={() => addToCart(produto)}
                className="flex items-center gap-1.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold px-3.5 py-2 rounded-lg transition-colors shadow-md shadow-emerald-500/15"
              >
                <Plus className="h-3.5 w-3.5 stroke-[2.5]" />
                Adicionar
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
