/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, ArrowUpRight, ArrowDownLeft, Receipt } from 'lucide-react';
import { Asset, Transaction } from '../types';

interface MD3TransactionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  asset: Asset | null;
  onSave: (transaction: Omit<Transaction, 'id'>) => void;
}

export default function MD3TransactionDialog({ isOpen, onClose, asset, onSave }: MD3TransactionDialogProps) {
  const [tipo, setTipo] = useState<'Compra' | 'Venda'>('Compra');
  const [valor, setValor] = useState('');
  const [data, setData] = useState('');

  useEffect(() => {
    // Current date format: YYYY.MM.DD
    const today = new Date();
    const formattedDate = `${today.getFullYear()}.${String(today.getMonth() + 1).padStart(2, '0')}.${String(today.getDate()).padStart(2, '0')}`;
    setData(formattedDate);
    setValor('');
    setTipo('Compra');
  }, [isOpen]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!valor || !asset) return;

    onSave({
      data,
      tipo,
      valor: parseFloat(valor) || 0,
      assetName: `${asset.displayName} (${asset.corretora})`
    });
    onClose();
  };

  if (!asset) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.6 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-[#001d36]/55 backdrop-blur-xs"
            onClick={onClose}
          />

          <motion.div
            initial={{ scale: 0.9, opacity: 0, y: 30 }}
            animate={{ 
              scale: 1, 
              opacity: 1, 
              y: 0,
              transition: { type: 'spring', damping: 25, stiffness: 350 }
            }}
            exit={{ scale: 0.95, opacity: 0, y: 15 }}
            className="relative w-full max-w-md bg-white text-[#1a1c1e] rounded-[28px] shadow-2xl overflow-hidden z-10 border border-[#dfe2e7]"
          >
            {/* Header */}
            <div className="px-6 py-5 flex items-center justify-between border-b border-[#ebedf2] bg-[#f7f9fc]">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-[#f2daff] text-[#6b5778] rounded-xl">
                  <Receipt size={20} />
                </div>
                <div>
                  <h3 className="text-xl font-display font-semibold text-[#001d36]">
                    Registrar Transação
                  </h3>
                  <p className="text-xs text-[#535f70] font-sans">
                    Lançar movimentação financeira
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

            {/* Form */}
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div className="p-3 bg-[#f1f3f8] rounded-xl border border-[#c3c7cf]">
                <p className="text-[10px] font-bold text-[#535f70] uppercase tracking-wider font-mono">Ativo Alvo</p>
                <p className="text-sm font-semibold text-[#1a1c1e] font-sans truncate">{asset.displayName}</p>
                <p className="text-xs text-[#535f70] font-sans">{asset.corretora} &bull; {asset.tipo}</p>
              </div>

              {/* Transaction Type Segmented Toggle */}
              <div className="space-y-1">
                <label className="text-xs font-semibold text-[#43474e] ml-1 block">Natureza</label>
                <div className="grid grid-cols-2 bg-[#f1f3f8] p-1 rounded-xl border border-[#c3c7cf]">
                  <button
                    type="button"
                    onClick={() => setTipo('Compra')}
                    className={`flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-xs font-bold font-sans transition-all duration-200 ${
                      tipo === 'Compra'
                        ? 'bg-[#0061a4] text-white shadow-xs'
                        : 'text-[#535f70] hover:text-[#1a1c1e]'
                    }`}
                  >
                    <ArrowUpRight size={14} />
                    Compra (Aporte)
                  </button>
                  <button
                    type="button"
                    onClick={() => setTipo('Venda')}
                    className={`flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-xs font-bold font-sans transition-all duration-200 ${
                      tipo === 'Venda'
                        ? 'bg-[#ba1a1a] text-white shadow-xs'
                        : 'text-[#535f70] hover:text-[#1a1c1e]'
                    }`}
                  >
                    <ArrowDownLeft size={14} />
                    Venda (Resgate)
                  </button>
                </div>
              </div>

              {/* Value Input */}
              <div className="space-y-1">
                <label className="text-xs font-semibold text-[#43474e] ml-1 block">Valor da Operação (R$)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  placeholder="Exemplo: 5000.00"
                  value={valor}
                  onChange={(e) => setValor(e.target.value)}
                  className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-mono text-sm transition-all"
                />
              </div>

              {/* Date Input */}
              <div className="space-y-1">
                <label className="text-xs font-semibold text-[#43474e] ml-1 block">Data do Lançamento</label>
                <input
                  type="text"
                  required
                  placeholder="Exemplo: 2026.05.28"
                  value={data}
                  onChange={(e) => setData(e.target.value)}
                  className="w-full h-11 px-4 bg-[#f1f3f8] border border-[#c3c7cf] focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4] rounded-xl outline-none font-mono text-sm transition-all"
                />
              </div>

              <div className="text-[11px] text-[#535f70] font-sans italic bg-[#ebedf2]/50 p-2.5 rounded-lg border border-[#c3c7cf]/50">
                💡 Ao salvar, esta transação atualizará o balanço geral e o histórico de compras/vendas em tempo real.
              </div>

              {/* Actions Footer */}
              <div className="pt-3 flex items-center justify-end gap-3 border-t border-[#ebedf2]">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 h-10 border border-[#c3c7cf] text-[#535f70] hover:bg-[#ebedf2] font-sans text-xs font-semibold rounded-full transition-colors cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className={`px-5 h-10 text-white font-sans text-xs font-semibold rounded-full shadow-md hover:shadow-lg transition-all cursor-pointer ${
                    tipo === 'Compra' ? 'bg-[#0061a4] hover:bg-[#00518d]' : 'bg-[#ba1a1a] hover:bg-[#961212]'
                  }`}
                >
                  Registrar
                </button>
              </div>
            </form>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}
