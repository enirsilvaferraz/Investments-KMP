/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface Asset {
  id: string;
  corretora: string;
  displayName: string;
  observacao: string;
  valorAnterior: number;
  valorAtual: number;
  valorizacao: number; // calculated as a BRL relative change e.g. 25520.00 or -5045.00
  tipo: 'Renda Fixa' | 'Renda Variável' | 'Fundos';
  subtipo: string; // Ex: CDB, LCI, LCA, FII, Ação Nacional, Ação Internacional, ETF
  vencimento: string; // "11/05/2026" or "-"
  mesReferencia: string; // "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
  alerta?: boolean; // Show alert context
  codigoB3?: string; // Ticker B3 code (e.g., "PETR4")
  hasCustomTransaction?: boolean;
  transacoesManual?: number;
  liquidez?: string; // e.g., "Diária", "D+0", "D+1", "D+30", "No Vencimento"
}

export interface Transaction {
  id: string;
  data: string; // "YYYY.MM.DD"
  tipo: 'Compra' | 'Venda';
  valor: number;
  assetName?: string;
}
