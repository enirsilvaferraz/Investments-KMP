/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { motion } from 'motion/react';
import { AlertTriangle, Info, TrendingUp, TrendingDown } from 'lucide-react';
import { Asset } from '../types';

interface AssetRowProps {
  key?: string;
  asset: Asset;
  onRowClick: (asset: Asset) => void;
  monthTransactionsBalance: number;
}

export default function AssetRow({ asset, onRowClick, monthTransactionsBalance }: AssetRowProps) {
  
  // Format currency dynamically to BR style
  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(val);
  };

  // Color matching for brokers
  const getCorretoraBadgeStyle = (corretora: string) => {
    switch (corretora) {
      case 'Banco BMG':
        return 'bg-amber-50 text-amber-800 border-amber-200/60';
      case 'Nubank':
        return 'bg-purple-50 text-purple-800 border-purple-200/60';
      case 'Banco Inter':
        return 'bg-orange-50 text-orange-800 border-orange-200/60';
      case 'XP Investimentos':
        return 'bg-blue-50 text-blue-800 border-blue-200/50';
      case 'BTG Pactual':
        return 'bg-emerald-50 text-emerald-800 border-emerald-200/50';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  // Color matching for asset classes (tipo)
  const getTipoBadgeStyle = (tipo: string) => {
    switch (tipo) {
      case 'Renda Fixa':
        return 'bg-blue-50 text-blue-800 border-[#0061a4]/20';
      case 'Renda Variável':
        return 'bg-emerald-50 text-emerald-800 border-emerald-200/50';
      case 'Fundos':
        return 'bg-purple-50 text-purple-800 border-purple-200/50';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const isPositiveYield = asset.valorizacao > 0;
  const isNegativeYield = asset.valorizacao < 0;
  const isInactive = asset.valorAnterior === 0 && asset.valorAtual === 0;

  return (
    <motion.tr
      initial={{ opacity: 0, y: 3 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -3 }}
      onClick={() => onRowClick(asset)}
      className={`border-b border-[#ebedf2] transition-colors cursor-pointer relative select-none whitespace-nowrap ${
        isInactive 
          ? 'bg-gray-50/60 opacity-45 grayscale-[20%] border-dashed hover:bg-gray-100 hover:opacity-80' 
          : asset.alerta 
            ? 'bg-[#fffdf9] hover:bg-[#0061a4]/5' 
            : 'hover:bg-[#0061a4]/5'
      }`}
      title={isInactive ? "Ativo Liquidado - Clique para editar ou ver transações" : "Clique para editar ou lançar transações neste ativo"}
    >
      {/* Column: Corretora badge */}
      <td className="py-2.5 px-2 font-display font-semibold text-xs text-[#1a1c1e] whitespace-nowrap">
        <span className={`px-2 py-1 rounded-md border text-[10px] font-sans font-medium hover:brightness-95 transition-all whitespace-nowrap ${
          isInactive ? 'bg-gray-100 text-gray-500 border-gray-200' : getCorretoraBadgeStyle(asset.corretora)
        }`}>
          {asset.corretora}
        </span>
      </td>

      {/* Column: Asset Title */}
      <td className="py-2.5 px-2 whitespace-nowrap">
        <div className={`text-xs font-semibold font-sans whitespace-nowrap flex items-center gap-1.5 ${
          isInactive ? 'text-gray-400 font-normal' : 'text-[#1a1c1e]'
        }`}>
          {asset.displayName}
        </div>
      </td>

      {/* Column: Class/Tipo */}
      <td className="py-2.5 px-2 text-xs font-sans whitespace-nowrap">
        <span className={`px-2 py-0.5 rounded-md border text-[10px] font-bold font-mono uppercase tracking-wider whitespace-nowrap ${
          isInactive ? 'bg-gray-100 text-gray-400 border-gray-200' : getTipoBadgeStyle(asset.tipo)
        }`}>
          {asset.tipo}
        </span>
      </td>

      {/* Column: Subtipo */}
      <td className="py-2.5 px-2 text-xs font-sans whitespace-nowrap">
        <span className={`px-1.5 py-0.5 rounded text-[10px] whitespace-nowrap border ${
          isInactive 
            ? 'bg-gray-100 border-gray-200 text-gray-400' 
            : 'font-semibold text-gray-800 bg-blue-50 border-blue-100'
        }`}>
          {asset.subtipo || '—'}
        </span>
      </td>

      {/* Column: Vencimento */}
      <td className={`py-2.5 px-2 text-xs font-mono text-center whitespace-nowrap ${
        isInactive ? 'text-gray-400' : 'text-gray-600'
      }`}>
        {asset.vencimento && asset.vencimento !== '-' ? asset.vencimento : 'Sem Vencimento'}
      </td>

      {/* Column: Observação */}
      <td className="py-2.5 px-2 text-xs font-sans text-gray-400 whitespace-nowrap">
        {asset.observacao || '—'}
      </td>

      {/* Column: Transações (Balanço Mensal) */}
      <td className="py-2.5 px-2 text-right font-mono text-xs whitespace-nowrap">
        {monthTransactionsBalance > 0 ? (
          <span className={isInactive ? "text-gray-400 font-bold" : "text-emerald-600 font-bold"}>+{formatCurrency(monthTransactionsBalance)}</span>
        ) : monthTransactionsBalance < 0 ? (
          <span className={isInactive ? "text-gray-400 font-bold" : "text-[#ba1a1a] font-bold"}>{formatCurrency(monthTransactionsBalance)}</span>
        ) : (
          <span className="text-gray-400 font-normal">—</span>
        )}
      </td>

      {/* Column: Valor Anterior */}
      <td className={`py-2.5 px-2 text-right font-mono text-xs whitespace-nowrap ${
        isInactive ? 'text-gray-400 font-normal' : 'font-medium text-gray-500'
      }`}>
        {formatCurrency(asset.valorAnterior)}
      </td>

      {/* Column: Valor Atual */}
      <td className={`py-2.5 px-2 text-right font-mono text-xs whitespace-nowrap ${
        isInactive ? 'text-gray-400 font-normal' : 'font-bold text-[#1a1c1e]'
      }`}>
        {formatCurrency(asset.valorAtual)}
      </td>

      {/* Column: Valorização (CHIPS RED/GREEN WITH ICON AND REAL VALUATION VALUES WITH SYMMETRIC SIZES) */}
      <td className="py-2.5 px-3 text-center whitespace-nowrap">
        <div className="flex items-center justify-center">
          {isInactive ? (
            <span className="w-[130px] inline-flex items-center justify-center gap-2 px-2 py-1 rounded-md text-[10px] font-bold font-mono bg-gray-100/60 text-gray-400 border border-gray-200/50 shrink-0 text-center">
              {formatCurrency(asset.valorizacao)}
            </span>
          ) : isNegativeYield ? (
            <span className="w-[130px] inline-flex items-center justify-center gap-2 px-2 py-1 rounded-md text-[10px] font-bold font-mono bg-[#ffdad6] text-[#ba1a1a] border border-[#ffb4ab] shrink-0">
              <TrendingDown size={11} className="stroke-[3] shrink-0" />
              <span className="truncate">{formatCurrency(asset.valorizacao)}</span>
            </span>
          ) : isPositiveYield ? (
            <span className="w-[130px] inline-flex items-center justify-center gap-2 px-2 py-1 rounded-md text-[10px] font-bold font-mono bg-emerald-50 text-emerald-700 border border-emerald-200 shrink-0">
              <TrendingUp size={11} className="stroke-[3] shrink-0" />
              <span className="truncate">+{formatCurrency(asset.valorizacao)}</span>
            </span>
          ) : (
            <span className="w-[130px] inline-flex items-center justify-center gap-2 px-2 py-1 rounded-md text-[10px] font-bold font-mono bg-gray-100 text-gray-600 border border-gray-200 shrink-0 text-center">
              —
            </span>
          )}
        </div>
      </td>

      {/* Column: B3 (Ticker Code) */}
      <td className="py-2.5 px-3 text-center whitespace-nowrap w-11">
        <div className="flex items-center justify-center">
          {asset.codigoB3 ? (
            <div className={`p-1 rounded-md transition-all hover:scale-105 ${
              isInactive ? 'bg-gray-100 text-gray-400' : 'bg-[#d1e4ff] text-[#0061a4]'
            }`} title={`Código B3: ${asset.codigoB3}`}>
              <Info size={14} />
            </div>
          ) : (
            <div className={`p-1 border rounded-md transition-all hover:scale-105 ${
              isInactive ? 'bg-gray-50 border-gray-200 text-gray-400' : 'bg-amber-50 border-amber-200 text-amber-500'
            }`} title="Código não informado">
              <AlertTriangle size={14} />
            </div>
          )}
        </div>
      </td>
    </motion.tr>
  );
}
