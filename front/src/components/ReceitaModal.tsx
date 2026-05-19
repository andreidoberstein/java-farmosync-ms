import React from 'react'
import { FileText, ShieldCheck } from 'lucide-react'

interface ReceitaModalProps {
  showReceitaModal: boolean;
  setShowReceitaModal: (show: boolean) => void;
  medicoNome: string;
  setMedicoNome: (nome: string) => void;
  crm: string;
  setCrm: (crm: string) => void;
  crmUf: string;
  setCrmUf: (uf: string) => void;
  submeterReceitaParaAuditoria: (e: React.FormEvent) => void;
}

export function ReceitaModal({
  showReceitaModal,
  setShowReceitaModal,
  medicoNome,
  setMedicoNome,
  crm,
  setCrm,
  crmUf,
  setCrmUf,
  submeterReceitaParaAuditoria
}: ReceitaModalProps) {
  if (!showReceitaModal) return null;

  const formatarCrm = (value: string): string => {
    return value.replace(/\D/g, '').slice(0, 8)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-6 shadow-2xl space-y-6">
        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <FileText className="text-emerald-400 h-5 w-5" /> Vincular Receita Sanitária
          </h3>
          <button 
            onClick={() => setShowReceitaModal(false)}
            className="text-slate-400 hover:text-white transition-colors"
          >
            fechar
          </button>
        </div>

        <form onSubmit={submeterReceitaParaAuditoria} className="space-y-4">
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Médico Prescritor</label>
            <input
              type="text"
              placeholder="Dr. Alexandre de Souza"
              value={medicoNome}
              onChange={(e) => setMedicoNome(e.target.value)}
              className="w-full bg-slate-950 border border-slate-850 rounded-xl px-3.5 py-2.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
              required
            />
          </div>

          <div className="grid grid-cols-3 gap-2">
            <div className="col-span-2 space-y-1">
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">CRM</label>
              <input
                type="text"
                placeholder="12345 (Digite 99999 p/ falha)"
                value={crm}
                onChange={(e) => setCrm(formatarCrm(e.target.value))}
                maxLength={8}
                className="w-full bg-slate-950 border border-slate-850 rounded-xl px-3.5 py-2.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
                required
              />
            </div>
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">UF</label>
              <select
                value={crmUf}
                onChange={(e) => setCrmUf(e.target.value)}
                className="w-full bg-slate-950 border border-slate-850 rounded-xl px-2 py-2.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
              >
                <option value="SP">SP</option>
                <option value="RJ">RJ</option>
                <option value="MG">MG</option>
                <option value="PR">PR</option>
              </select>
            </div>
          </div>

          <div className="text-[11px] leading-relaxed text-slate-500 p-3 bg-slate-950 border border-slate-850 rounded-lg">
            <strong>Simulador Sênior:</strong> Ao clicar em "Auditar Assinatura", os dados serão empacotados em um payload e enviados de forma simulada ao microsserviço de regulação. Digite <code className="text-rose-400 bg-slate-900 border border-slate-850 px-1.5 py-0.5 rounded">99999</code> no CRM para forçar a rejeição.
          </div>

          <div className="flex gap-3 pt-3 border-t border-slate-800">
            <button
              type="button"
              onClick={() => setShowReceitaModal(false)}
              className="flex-1 bg-slate-800 hover:bg-slate-750 text-slate-300 text-xs font-bold py-2.5 rounded-xl transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="flex-1 bg-rose-500 hover:bg-rose-400 text-white text-xs font-bold py-2.5 rounded-xl transition-colors flex items-center justify-center gap-1.5 shadow-md shadow-rose-500/15"
            >
              <ShieldCheck className="h-3.5 w-3.5" />
              Auditar Assinatura
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
