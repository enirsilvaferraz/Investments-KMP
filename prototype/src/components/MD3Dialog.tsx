/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, Calendar, AlertTriangle, Info, Plus, ChevronDown, Trash2 } from 'lucide-react';
import { Asset } from '../types';
import { CORRETORAS_LIST, MESES_LIST, SUBTYPES_MAP } from '../data';

interface MD3DialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (asset: Omit<Asset, 'id'> & { id?: string }) => void;
  onDeleteAsset?: (id: string) => void;
  editingAsset?: Asset | null;
}

export default function MD3Dialog({ isOpen, onClose, onSave, onDeleteAsset, editingAsset }: MD3DialogProps) {
  const [corretora, setCorretora] = useState('Banco BMG');
  const [customCorretora, setCustomCorretora] = useState('');
  const [isCustomCorretora, setIsCustomCorretora] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [observacao, setObservacao] = useState('');
  const [valorAnterior, setValorAnterior] = useState('');
  const [valorAtual, setValorAtual] = useState('');
  const [valorizacao, setValorizacao] = useState('');
  const [tipo, setTipo] = useState<'Renda Fixa' | 'Renda Variável' | 'Fundos'>('Renda Fixa');
  const [subtipo, setSubtipo] = useState('CDB');
  const [vencimento, setVencimento] = useState('-');
  const [mesReferencia, setMesReferencia] = useState('Maio');
  const [alerta, setAlerta] = useState(false);
  const [liquidez, setLiquidez] = useState('Diária');
  const [codigoB3, setCodigoB3] = useState('');

  // Set initial subtipo default depending on tipo
  useEffect(() => {
    if (!editingAsset) {
      const allowed = SUBTYPES_MAP[tipo] || [];
      if (allowed.length > 0 && !allowed.includes(subtipo)) {
        setSubtipo(allowed[0]);
      }
    }
  }, [tipo, editingAsset]);

  useEffect(() => {
    if (editingAsset) {
      if (CORRETORAS_LIST.includes(editingAsset.corretora)) {
        setCorretora(editingAsset.corretora);
        setIsCustomCorretora(false);
      } else {
        setCorretora('Outro');
        setCustomCorretora(editingAsset.corretora);
        setIsCustomCorretora(true);
      }
      setDisplayName(editingAsset.displayName);
      setObservacao(editingAsset.observacao);
      setValorAnterior(editingAsset.valorAnterior.toString());
      setValorAtual(editingAsset.valorAtual.toString());
      setValorizacao(editingAsset.valorizacao.toString());
      setTipo(editingAsset.tipo);
      setSubtipo(editingAsset.subtipo || 'CDB');
      setVencimento(editingAsset.vencimento || '-');
      setMesReferencia(editingAsset.mesReferencia || 'Maio');
      setAlerta(!!editingAsset.alerta);
      setLiquidez(editingAsset.liquidez || 'Diária');
      setCodigoB3(editingAsset.codigoB3 || '');
    } else {
      setCorretora('Banco BMG');
      setCustomCorretora('');
      setIsCustomCorretora(false);
      setDisplayName('');
      setObservacao('');
      setValorAnterior('');
      setValorAtual('');
      setValorizacao('');
      setTipo('Renda Fixa');
      setSubtipo('CDB');
      setVencimento('-');
      setMesReferencia('Maio');
      setAlerta(false);
      setLiquidez('Diária');
      setCodigoB3('');
    }
  }, [editingAsset, isOpen]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!displayName) return;

    const finalCorretora = isCustomCorretora ? customCorretora || 'Outro' : corretora;

    onSave({
      id: editingAsset?.id,
      corretora: finalCorretora,
      displayName,
      observacao,
      valorAnterior: parseFloat(valorAnterior) || 0,
      valorAtual: parseFloat(valorAtual) || 0,
      valorizacao: parseFloat(valorizacao) || 0,
      tipo,
      subtipo,
      vencimento,
      mesReferencia,
      alerta,
      liquidez,
      codigoB3,
    });
    onClose();
  };

  const handleDelete = () => {
    if (editingAsset && onDeleteAsset) {
      if (window.confirm('Excluir permanentemente este ativo de investimento?')) {
        onDeleteAsset(editingAsset.id);
        onClose();
      }
    }
  };

  const allowedSubtypes = SUBTYPES_MAP[tipo] || [];

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop screen layer */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.6 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-[#001d36]/55 backdrop-blur-xs"
            onClick={onClose}
          />

          {/* Dialog core layout */}
          <motion.div
            initial={{ scale: 0.9, opacity: 0, y: 30 }}
            animate={{ 
              scale: 1, 
              opacity: 1, 
              y: 0,
              transition: { type: 'spring', damping: 25, stiffness: 350 }
            }}
            exit={{ scale: 0.95, opacity: 0, y: 15 }}
            className="relative w-full max-w-2xl bg-white text-[#1a1c1e] rounded-[28px] shadow-2xl overflow-hidden z-10 border border-[#dfe2e7]"
          >
            {/* Upper cover name bar */}
            <div className="px-6 py-5 flex items-center justify-between border-b border-[#ebedf2] bg-[#f7f9fc]">
              <div className="flex items-center gap-3">
                <div className="p-2 ml-[2px] bg-[#d1e4ff] text-[#0061a4] rounded-xl">
                  {editingAsset ? <Info size={20} /> : <Plus size={20} />}
                </div>
                <div>
                  <h3 className="text-xl font-display font-semibold text-[#001d36]">
                    {editingAsset ? 'Editar Ativo de Investimento' : 'Novo Ativo de Investimento'}
                  </h3>
                  <p className="text-xs text-[#535f70] font-sans">
                    Insira as informações do ativo seguindo os padrões Material Design 3
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={onClose}
                className="p-2 text-[#535f70] hover:bg-[#e5e8ec] rounded-full transition-colors"
              >
                <X size={20} />
              </button>
            </div>

            {/* Scrollable form */}
            <form onSubmit={handleSubmit} className="p-6 max-h-[75vh] overflow-y-auto space-y-5">
              
              {/* Row: Corretora / Instituição */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Corretora ou Banco Distribuidor</label>
                  <div className="relative">
                    <select
                      value={corretora}
                      onChange={(e) => {
                        const val = e.target.value;
                        setCorretora(val);
                        setIsCustomCorretora(val === 'Outro');
                      }}
                      className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] hover:border-[#73777f] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none appearance-none font-sans text-sm transition-all text-[#1a1c1e]"
                    >
                      {CORRETORAS_LIST.map((c) => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                      <option value="Outro">Outro (Especificar)</option>
                    </select>
                    <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[#535f70]">
                      <ChevronDown size={16} />
                    </div>
                  </div>
                </div>

                {isCustomCorretora && (
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-[#43474e] block ml-1">Nome da Corretora / Banco</label>
                    <input
                      type="text"
                      required
                      placeholder="Exemplo: XP Investimentos, BTG Pactual"
                      value={customCorretora}
                      onChange={(e) => setCustomCorretora(e.target.value)}
                      className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-sans text-sm transition-all"
                    />
                  </div>
                )}
              </div>

              {/* Row: Asset Display Name & B3 CODE SIDE-BY-SIDE */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="md:col-span-2 space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Descrição Comercial do Ativo <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    required
                    placeholder="Exemplo: CDB de 110.0% do CDI"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-sans text-sm transition-all text-[#1a1c1e]"
                  />
                </div>
                
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Código B3 (Ticker)</label>
                  <input
                    type="text"
                    placeholder="Ex: PETR4, VALE3"
                    value={codigoB3}
                    onChange={(e) => setCodigoB3(e.target.value.toUpperCase())}
                    className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-sans text-sm transition-all text-[#1a1c1e] font-semibold"
                  />
                </div>
              </div>

              {/* Row: Observação */}
              <div className="space-y-1">
                <label className="text-xs font-semibold text-[#43474e] block ml-1">Marcador Secundário / Nota</label>
                <input
                  type="text"
                  placeholder="Exemplo: Reserva Emergencial, Aposentadoria, etc."
                  value={observacao}
                  onChange={(e) => setObservacao(e.target.value)}
                  className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-sans text-sm transition-all"
                />
              </div>

              {/* Grid: Values and Returns */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Valor Anterior (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="20000.00"
                    value={valorAnterior}
                    onChange={(e) => setValorAnterior(e.target.value)}
                    className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-mono text-sm transition-all"
                  />
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Valor Atualizado (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="25000.00"
                    value={valorAtual}
                    onChange={(e) => setValorAtual(e.target.value)}
                    className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-mono text-sm transition-all"
                  />
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Valorização Estimada (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="Exemplo: 500.00 ou -250.00"
                    value={valorizacao}
                    onChange={(e) => setValorizacao(e.target.value)}
                    className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-mono text-sm transition-all"
                  />
                </div>
              </div>

              {/* Grid: Type, Subtype and Vencimento */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Classe do Ativo</label>
                  <div className="flex bg-[#f1f3f8] p-1 rounded-xl border border-[#c3c7cf]">
                    {(['Renda Fixa', 'Renda Variável', 'Fundos'] as const).map((t) => (
                      <button
                        key={t}
                        type="button"
                        onClick={() => {
                          setTipo(t);
                        }}
                        className={`flex-1 py-1.5 px-2 rounded-lg text-[11px] font-bold font-sans transition-all duration-200 ${
                          tipo === t
                            ? 'bg-[#0061a4] text-white shadow-xs'
                            : 'text-[#535f70] hover:text-[#1a1c1e]'
                        }`}
                      >
                        {t}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Subtipo / Categoria</label>
                  <div className="relative">
                    <select
                      value={subtipo}
                      onChange={(e) => setSubtipo(e.target.value)}
                      className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] rounded-xl outline-none appearance-none font-sans text-sm transition-all text-[#1a1c1e]"
                    >
                      {allowedSubtypes.map((sub) => (
                        <option key={sub} value={sub}>{sub}</option>
                      ))}
                    </select>
                    <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[#535f70]">
                      <ChevronDown size={16} />
                    </div>
                  </div>
                </div>
              </div>

              {/* Grid: Vencimento, Mes Referencia, Liquidez */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Data de Vencimento/Maturidade</label>
                  <input
                    type="text"
                    placeholder="Exemplo: 11/05/2026 ou '-' para ilimitado"
                    value={vencimento}
                    onChange={(e) => setVencimento(e.target.value)}
                    className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-sans text-sm transition-all"
                  />
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Liquidez do Ativo</label>
                  <div className="relative">
                    <select
                      value={liquidez}
                      onChange={(e) => setLiquidez(e.target.value)}
                      className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] rounded-xl outline-none appearance-none font-sans text-sm transition-all text-[#1a1c1e]"
                    >
                      <option value="Diária">Diária (D+0)</option>
                      <option value="D+1">D+1</option>
                      <option value="D+2">D+2</option>
                      <option value="D+30">D+30</option>
                      <option value="No Vencimento">No Vencimento</option>
                      <option value="Imediata">Imediata</option>
                    </select>
                    <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[#535f70]">
                      <ChevronDown size={16} />
                    </div>
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-[#43474e] block ml-1">Mês de Referência (Painel)</label>
                  <div className="relative">
                    <select
                      value={mesReferencia}
                      onChange={(e) => setMesReferencia(e.target.value)}
                      className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] rounded-xl outline-none appearance-none font-sans text-sm transition-all text-[#1a1c1e]"
                    >
                      {MESES_LIST.map((m) => (
                        <option key={m} value={m}>{m}</option>
                      ))}
                    </select>
                    <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[#535f70]">
                      <Calendar size={16} />
                    </div>
                  </div>
                </div>
              </div>

              {/* Row: Alert Mode (Warning Checkbox) */}
              <div className="flex items-center gap-3 p-3 bg-[#fff9f0] border border-[#fbd38d]/60 rounded-xl">
                <div className="p-2 bg-[#ffdad6] text-[#ba1a1a] rounded-lg">
                  <AlertTriangle size={18} />
                </div>
                <div className="flex-1">
                  <label htmlFor="alerta-chk" className="text-xs font-bold text-[#1a1c1e] font-sans block cursor-pointer">
                    Ativar Alerta Visual no Painel
                  </label>
                  <p className="text-[10px] text-[#535f70] font-sans">
                    Marca o ativo com ícone laranja de atenção na listagem geral para sinalizar carência, alteração regulatória ou evento importante.
                  </p>
                </div>
                <input
                  id="alerta-chk"
                  type="checkbox"
                  checked={alerta}
                  onChange={(e) => setAlerta(e.target.checked)}
                  className="w-5 h-5 text-[#0061a4] focus:ring-[#0061a4] border-gray-300 rounded-md cursor-pointer"
                />
              </div>

              {/* Footer Buttons */}
              <div className="pt-4 flex items-center justify-between gap-3 border-t border-[#ebedf2]">
                <div>
                  {editingAsset && onDeleteAsset && (
                    <button
                      type="button"
                      onClick={handleDelete}
                      className="px-4 h-10 bg-[#ffdad6]/80 hover:bg-[#ffdad6] text-[#ba1a1a] font-sans text-xs font-bold rounded-full transition-all cursor-pointer flex items-center gap-2 border border-[#ffb4ab]"
                    >
                      <Trash2 size={14} />
                      Excluir Ativo
                    </button>
                  )}
                </div>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={onClose}
                    className="px-5 h-10 border border-[#c3c7cf] text-[#535f70] hover:bg-[#ebedf2] font-sans text-xs font-semibold rounded-full transition-colors cursor-pointer"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="px-6 h-10 bg-[#0061a4] hover:bg-[#00518d] text-white font-sans text-xs font-bold rounded-full shadow-md hover:shadow-lg transition-all cursor-pointer flex items-center justify-center animate-none"
                  >
                    {editingAsset ? 'Salvar Alterações' : 'Gravar Ativo'}
                  </button>
                </div>
              </div>
            </form>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}
