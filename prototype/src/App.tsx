/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Search, 
  Download, 
  Upload,
  RotateCw, 
  Plus, 
  X, 
  TrendingUp, 
  Briefcase, 
  Calendar, 
  Layers, 
  Info,
  TrendingDown,
  Wallet,
  BarChart2,
  ChevronRight,
  ChevronLeft,
  Filter,
  Check,
  History
} from 'lucide-react';

import { Asset, Transaction } from './types';
import { INITIAL_ASSETS, INITIAL_TRANSACTIONS, CORRETORAS_LIST, MESES_LIST, SUBTYPES_MAP } from './data';
import AssetRow from './components/AssetRow';
import MD3Dialog from './components/MD3Dialog';
import MD3TransactionDialog from './components/MD3TransactionDialog';

export default function App() {
  // --- Persistent Local State ---
  const [assets, setAssets] = useState<Asset[]>(() => {
    const saved = localStorage.getItem('md3_potfolio_assets');
    return saved ? JSON.parse(saved) : INITIAL_ASSETS;
  });

  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    const saved = localStorage.getItem('md3_portfolio_transactions');
    return saved ? JSON.parse(saved) : INITIAL_TRANSACTIONS;
  });

  // Save on updates
  useEffect(() => {
    localStorage.setItem('md3_potfolio_assets', JSON.stringify(assets));
  }, [assets]);

  useEffect(() => {
    localStorage.setItem('md3_portfolio_transactions', JSON.stringify(transactions));
  }, [transactions]);

  // --- Filtering & Navigation states ---
  const [sidebarCollapsed, setSidebarCollapsed] = useState(true); // Collapsed by default (Mostre o nav rail recolhido)
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCorretora, setSelectedCorretora] = useState<string>('Todos'); // Single selection ('Todos' by default)
  const [selectedClasses, setSelectedClasses] = useState<string[]>([]); // Filters classes: Starts completely unchecked/empty (shows all)
  const [selectedSubtypes, setSelectedSubtypes] = useState<string[]>([]); // Filters subtipos: Starts completely unchecked/empty (shows all)
  const [selectedLiquidez, setSelectedLiquidez] = useState<string[]>([]); // Filters liquidez: Starts completely unchecked/empty (shows all)
  const [selectedB3Status, setSelectedB3Status] = useState<string[]>([]); // Filters B3 info status ("Sim", "Não"): Starts completely unchecked/empty (shows all)
  const [selectedLiquidated, setSelectedLiquidated] = useState<string[]>([]); // Filters Ativos Liquidados status ("Sim", "Não"): Starts completely unchecked/empty (shows all)
  const [selectedVenceAte, setSelectedVenceAte] = useState<string>(''); // Filter "Vence até" (YYYY-MM string format, e.g. "2028-12"): Starts empty/no filter
  const [selectedMonth, setSelectedMonth] = useState<string>('Maio'); // Monthly filter dropdown (O filtro de ano virou mensal)
  const [activeTab, setActiveTab] = useState<'carteira' | 'historico'>('carteira');
  
  // --- Sorting Configuration state ---
  const [sortConfig, setSortConfig] = useState<Record<string, 'asc' | 'desc' | null>>({});

  const toggleSort = (key: string) => {
    setSortConfig(prev => {
      const current = prev[key];
      const next = current === 'asc' ? 'desc' : current === 'desc' ? null : 'asc';
      return { ...prev, [key]: next };
    });
  };
  
  // --- Dynamic Loader Status ---
  const [isExporting, setIsExporting] = useState(false); // Exportar loading state
  const [isImporting, setIsImporting] = useState(false); // Importar loading state (Teremos um botao importar "carregando")

  // --- Modal Dialog States ---
  const [isAddAssetOpen, setIsAddAssetOpen] = useState(false);
  const [isTransactionOpen, setIsTransactionOpen] = useState(false);
  const [targetAssetForTx, setTargetAssetForTx] = useState<Asset | null>(null);
  const [editingAsset, setEditingAsset] = useState<Asset | null>(null);

  // Liquidity helper fallback retriever
  const getAssetLiquidez = (asset: Asset): string => {
    if (asset.liquidez) return asset.liquidez;
    const nameLower = asset.displayName.toLowerCase();
    const obsLower = asset.observacao.toLowerCase();
    if (nameLower.includes('liquidez') || nameLower.includes('reserva') || nameLower.includes('saldo') || obsLower.includes('liquidez') || obsLower.includes('saldo')) {
      return 'Diária';
    }
    if (asset.tipo === 'Renda Variável') {
       return 'D+2';
    }
    if (asset.vencimento && asset.vencimento !== '-') {
      return 'No Vencimento';
    }
    if (asset.tipo === 'Fundos') {
      return 'D+30';
    }
    return 'Diária';
  };

  const parseVencDate = (v: string): Date | null => {
    if (!v || v === '-' || v.toLowerCase().includes('vencimento') || v.toLowerCase().includes('sem')) return null;
    const pts = v.split('/');
    if (pts.length === 3) {
      const d = new Date(Number(pts[2]), Number(pts[1]) - 1, Number(pts[0]));
      if (!isNaN(d.getTime())) {
        return d;
      }
    }
    return null;
  };

  const getMaturityOptions = () => {
    const optionsMap = new Map<string, { label: string; year: number; month: number; lastDayTime: number }>();
    const MESES_NOMES = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    
    assets.forEach(asset => {
      const date = parseVencDate(asset.vencimento);
      if (date) {
        const year = date.getFullYear();
        const month = date.getMonth();
        const key = `${year}-${String(month + 1).padStart(2, '0')}`;
        if (!optionsMap.has(key)) {
          const lastDayOfMeso = new Date(year, month + 1, 0, 23, 59, 59, 999);
          optionsMap.set(key, {
            label: `${MESES_NOMES[month]}/${year}`,
            year,
            month,
            lastDayTime: lastDayOfMeso.getTime()
          });
        }
      }
    });

    return Array.from(optionsMap.values()).sort((a, b) => {
      if (a.year !== b.year) return a.year - b.year;
      return a.month - b.month;
    });
  };

  // --- Advanced Filter Logic ---
  const filteredAssets = assets.filter((asset) => {
    // 1. Text Search query (fields: broker, display name, observacao)
    const matchesQuery = 
      searchQuery.trim() === '' ||
      asset.corretora.toLowerCase().includes(searchQuery.toLowerCase()) ||
      asset.displayName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      asset.observacao.toLowerCase().includes(searchQuery.toLowerCase()) ||
      asset.subtipo.toLowerCase().includes(searchQuery.toLowerCase());

    // 2. Corretora filter state
    const matchesCorretora = 
      selectedCorretora === 'Todos' || asset.corretora === selectedCorretora;

    // 3. Asset Categories selection filters: "Renda Fixa", "Renda Variável", "Fundos"
    const matchesClass = 
      selectedClasses.length === 0 || selectedClasses.includes(asset.tipo);

    // 4. Reference month filter dropdown selection
    const matchesMonth = 
      selectedMonth === 'Todos' || 
      asset.mesReferencia === selectedMonth;

    // 5. Asset Subtype selection filters
    const matchesSubtype = 
      selectedSubtypes.length === 0 || selectedSubtypes.includes(asset.subtipo);

    // 6. Liquidity selection filters
    const matchesLiquidez = 
      selectedLiquidez.length === 0 || selectedLiquidez.includes(asset.liquidez || getAssetLiquidez(asset));

    // 7. B3 Informado selection filters
    const matchesB3Status = 
      selectedB3Status.length === 0 || 
      (selectedB3Status.includes('Sim') && !!asset.codigoB3) ||
      (selectedB3Status.includes('Não') && !asset.codigoB3);

    // 8. "Vence até" selection filter
    let matchesVenceAte = true;
    if (selectedVenceAte) {
      const assetDate = parseVencDate(asset.vencimento);
      if (assetDate) {
        const options = getMaturityOptions();
        const option = options.find(opt => `${opt.year}-${String(opt.month + 1).padStart(2, '0')}` === selectedVenceAte);
        if (option) {
          matchesVenceAte = assetDate.getTime() <= option.lastDayTime;
        } else {
          matchesVenceAte = false;
        }
      } else {
        matchesVenceAte = false;
      }
    }

    // 9. Ativos Liquidados (Liquidated assets where both previous and updated value are 0)
    const isAssetLiquidated = asset.valorAnterior === 0 && asset.valorAtual === 0;
    const matchesLiquidated = 
      selectedLiquidated.length === 0 || 
      (selectedLiquidated.includes('Sim') && isAssetLiquidated) ||
      (selectedLiquidated.includes('Não') && !isAssetLiquidated);

    return matchesQuery && matchesCorretora && matchesClass && matchesMonth && matchesSubtype && matchesLiquidez && matchesB3Status && matchesVenceAte && matchesLiquidated;
  });

  const getAssetTransactionsBalanceForMonth = (asset: Asset, monthName: string): number => {
    const monthMap: Record<string, string> = {
      'Janeiro': '01',
      'Fevereiro': '02',
      'Março': '03',
      'Abril': '04',
      'Maio': '05',
      'Junho': '06',
      'Julho': '07',
      'Agosto': '08',
      'Setembro': '09',
      'Outubro': '10',
      'Novembro': '11',
      'Dezembro': '12'
    };

    return transactions.reduce((sum, tx) => {
      // Check if tx matches asset
      let matchesAsset = false;
      if (tx.assetId === asset.id) {
        matchesAsset = true;
      } else if (tx.assetName) {
        const cleanTxName = tx.assetName.toLowerCase().replace(/[\(\)]/g, '').trim();
        const cleanDisplayName = asset.displayName.toLowerCase().trim();
        const cleanCorretoraName = asset.corretora.toLowerCase().trim();
        if (cleanTxName.includes(cleanDisplayName) || 
            cleanDisplayName.includes(cleanTxName) ||
            (cleanTxName.includes(cleanCorretoraName) && cleanTxName.includes(cleanDisplayName.split(' ')[0]))) {
          matchesAsset = true;
        }
      }

      if (!matchesAsset) return sum;

      // Check month
      if (monthName !== 'Todos') {
        const targetCode = monthMap[monthName];
        if (targetCode) {
          const pts = tx.data.split('.');
          if (pts.length >= 2 && pts[1] !== targetCode) {
            return sum;
          }
        }
      }

      const val = tx.tipo === 'Venda' ? -tx.valor : tx.valor;
      return sum + val;
    }, 0);
  };

  const COLUMN_PRIORITY_ORDER = [
    'corretora',
    'displayName',
    'tipo',
    'subtipo',
    'vencimento',
    'observacao',
    'transacoes',
    'valorAnterior',
    'valorAtual',
    'valorizacao',
    'codigoB3'
  ];

  const sortedFilteredAssets = [...filteredAssets].sort((a, b) => {
    for (const key of COLUMN_PRIORITY_ORDER) {
      const direction = sortConfig[key];
      if (!direction) continue;

      if (key === 'transacoes') {
        const balA = getAssetTransactionsBalanceForMonth(a, selectedMonth);
        const balB = getAssetTransactionsBalanceForMonth(b, selectedMonth);
        if (balA !== balB) {
          return direction === 'asc' ? balA - balB : balB - balA;
        }
        continue;
      }

      const valA = a[key as keyof Asset];
      const valB = b[key as keyof Asset];

      if (typeof valA === 'boolean' && typeof valB === 'boolean') {
        if (valA !== valB) {
          return direction === 'asc' 
            ? (valA ? 1 : -1) 
            : (valA ? -1 : 1);
        }
      } else if (typeof valA === 'number' && typeof valB === 'number') {
        if (valA !== valB) {
          return direction === 'asc' ? valA - valB : valB - valA;
        }
      } else {
        const strA = String(valA || '').trim();
        const strB = String(valB || '').trim();

        if (key === 'vencimento') {
          const parseVenc = (v: string) => {
            if (!v || v === '-' || v.toLowerCase().includes('vencimento')) return 10000000000000;
            const pts = v.split('/');
            if (pts.length === 3) {
              return new Date(Number(pts[2]), Number(pts[1]) - 1, Number(pts[0])).getTime();
            }
            return 0;
          };
          const tA = parseVenc(strA);
          const tB = parseVenc(strB);
          if (tA !== tB) {
            return direction === 'asc' ? tA - tB : tB - tA;
          }
        } else {
          const cmp = strA.localeCompare(strB, 'pt-BR', { numeric: true, sensitivity: 'base' });
          if (cmp !== 0) {
            return direction === 'asc' ? cmp : -cmp;
          }
        }
      }
    }
    return 0;
  });

  // --- Real-time math aggregates based on filters ---
  const valorAnteriorTotal = filteredAssets.reduce((sum, item) => sum + item.valorAnterior, 0);
  const valorAtualTotal = filteredAssets.reduce((sum, item) => sum + item.valorAtual, 0);
  const totalValuationChange = filteredAssets.reduce((sum, item) => sum + item.valorizacao, 0);

  // Helper to compute deposits/withdrawals for filtered assets in the current month
  const getTransactionsSums = () => {
    let aportes = 0;
    let retiradas = 0;
    
    const monthMap: Record<string, string> = {
      'Janeiro': '01',
      'Fevereiro': '02',
      'Março': '03',
      'Abril': '04',
      'Maio': '05',
      'Junho': '06',
      'Julho': '07',
      'Agosto': '08',
      'Setembro': '09',
      'Outubro': '10',
      'Novembro': '11',
      'Dezembro': '12'
    };

    transactions.forEach(tx => {
      const matchesAsset = filteredAssets.some(asset => {
        if (tx.assetId === asset.id) return true;
        if (tx.assetName) {
          const cleanTxName = tx.assetName.toLowerCase().replace(/[\(\)]/g, '').trim();
          const cleanDisplayName = asset.displayName.toLowerCase().trim();
          const cleanCorretoraName = asset.corretora.toLowerCase().trim();
          return cleanTxName.includes(cleanDisplayName) || 
                 cleanDisplayName.includes(cleanTxName) ||
                 (cleanTxName.includes(cleanCorretoraName) && cleanTxName.includes(cleanDisplayName.split(' ')[0]));
        }
        return false;
      });

      if (!matchesAsset) return;

      if (selectedMonth !== 'Todos') {
        const targetCode = monthMap[selectedMonth];
        if (targetCode) {
          const pts = tx.data.split('.');
          if (pts.length >= 2 && pts[1] !== targetCode) {
            return;
          }
        }
      }

      if (tx.tipo === 'Compra') {
        aportes += tx.valor;
      } else if (tx.tipo === 'Venda') {
        retiradas += tx.valor;
      }
    });

    return { aportes, retiradas };
  };

  const { aportes: aportesTotal, retiradas: retiradasTotal } = getTransactionsSums();

  // Crescimento: Soma dos aportes menos as retiradas mais valorização (lucro)
  const crescimentoTotal = aportesTotal - retiradasTotal + totalValuationChange;

  // Percentual de crescimento: % em relação ao valor anterior
  const percentualCrescimento = valorAnteriorTotal > 0 ? (crescimentoTotal / valorAnteriorTotal) : 0;

  // Lucro: quanto rendeu os investimentos (this is absolute total valuation change)
  const lucroTotal = totalValuationChange;

  // Valorização: percentual em relação ao valor final
  const valorizacaoPercentual = valorAtualTotal > 0 ? (totalValuationChange / valorAtualTotal) : 0;

  // Formatting helpers 
  const formatBRL = (val: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      minimumFractionDigits: 2
    }).format(val);
  };

  const formatPercentReal = (val: number) => {
    return (val * 100).toFixed(2).replace('.', ',') + '%';
  };

  const renderSortableHeader = (
    key: string,
    label: string,
    widthClass?: string,
    align: 'left' | 'center' | 'right' = 'left'
  ) => {
    const direction = sortConfig[key];
    const isActive = direction !== undefined && direction !== null;
    
    // Determine layout flex classes based on alignment
    const justifyClass = 
      align === 'right' ? 'justify-end text-right' : align === 'center' ? 'justify-center text-center' : 'justify-start text-left';
    
    return (
      <th 
        onClick={() => toggleSort(key)}
        className={`py-2 px-2 hover:bg-[#ebedf2] cursor-pointer select-none transition-colors border-b border-[#ebedf2] group relative h-11 whitespace-nowrap ${widthClass || ''} ${align === 'right' ? 'text-right' : align === 'center' ? 'text-center' : 'text-left'}`}
        title={`Clique para ordenar por ${label}`}
      >
        <div className={`flex items-center gap-1.5 font-display font-semibold text-xs tracking-wide text-[#43474e] select-none ${justifyClass}`}>
          <span>{label}</span>
          <div className="flex flex-col items-center justify-center shrink-0 w-3 h-3 text-[10px] select-none">
            {direction === 'asc' && (
              <span className="text-[#0061a4] font-black leading-none">▲</span>
            )}
            {direction === 'desc' && (
              <span className="text-[#0061a4] font-black leading-none">▼</span>
            )}
            {!direction && (
              <span className="text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity leading-none text-[9px]">⇅</span>
            )}
          </div>
        </div>
      </th>
    );
  };

  // --- Brokerage Filters toggling ---
  const handleSelectCorretora = (corretora: string) => {
    setSelectedCorretora(corretora);
  };

  const handleToggleClass = (cls: string) => {
    if (selectedClasses.includes(cls)) {
      setSelectedClasses(selectedClasses.filter(c => c !== cls));
      const subtypesToRemove = SUBTYPES_MAP[cls] || [];
      setSelectedSubtypes(prev => prev.filter(s => !subtypesToRemove.includes(s)));
    } else {
      setSelectedClasses([...selectedClasses, cls]);
    }
  };

  const handleToggleSubtype = (sub: string) => {
    if (selectedSubtypes.includes(sub)) {
      setSelectedSubtypes(selectedSubtypes.filter(s => s !== sub));
    } else {
      setSelectedSubtypes([...selectedSubtypes, sub]);
    }
  };

  const handleToggleLiquidez = (liq: string) => {
    if (selectedLiquidez.includes(liq)) {
      setSelectedLiquidez(selectedLiquidez.filter(l => l !== liq));
    } else {
      setSelectedLiquidez([...selectedLiquidez, liq]);
    }
  };

  const handleToggleB3Status = (status: string) => {
    if (selectedB3Status.includes(status)) {
      setSelectedB3Status(selectedB3Status.filter(s => s !== status));
    } else {
      setSelectedB3Status([...selectedB3Status, status]);
    }
  };

  const handleToggleLiquidated = (status: string) => {
    if (selectedLiquidated.includes(status)) {
      setSelectedLiquidated(selectedLiquidated.filter(s => s !== status));
    } else {
      setSelectedLiquidated([...selectedLiquidated, status]);
    }
  };

  const handleClearFilters = () => {
    setSelectedCorretora('Todos');
    setSelectedClasses([]);
    setSelectedSubtypes([]);
    setSelectedLiquidez([]);
    setSelectedB3Status([]);
    setSelectedLiquidated([]);
    setSelectedVenceAte('');
    setSelectedMonth('Maio');
    setSearchQuery('');
  };

  const isFilterModified = 
    selectedCorretora !== 'Todos' || 
    selectedClasses.length !== 0 || 
    selectedSubtypes.length !== 0 || 
    selectedLiquidez.length !== 0 || 
    selectedB3Status.length !== 0 || 
    selectedLiquidated.length !== 0 || 
    selectedVenceAte !== '' ||
    selectedMonth !== 'Maio' || 
    searchQuery !== '';

  // --- Handlers & Simulators ---
  const handleExportData = () => {
    setIsExporting(true);
    setTimeout(() => {
      setIsExporting(false);

      // Generate CSV string representing current filtered assets
      const headers = ['Corretora', 'Descrição Comercial', 'Classe', 'Subtipo', 'Vencimento', 'Valor Anterior', 'Valor Atual', 'Valorização', 'Nota / Objetivo'];
      const rows = filteredAssets.map(asset => [
        `"${asset.corretora.replace(/"/g, '""')}"`,
        `"${asset.displayName.replace(/"/g, '""')}"`,
        `"${asset.tipo.replace(/"/g, '""')}"`,
        `"${asset.subtipo.replace(/"/g, '""')}"`,
        `"${asset.vencimento.replace(/"/g, '""')}"`,
        asset.valorAnterior.toFixed(2),
        asset.valorAtual.toFixed(2),
        asset.valorizacao.toFixed(2),
        `"${(asset.observacao || '').replace(/"/g, '""')}"`
      ]);

      const csvContent = [
        '\uFEFF' + headers.join(';'), // Semicolon is excellent for Microsoft Excel compatibility
        ...rows.map(row => row.join(';'))
      ].join('\n');

      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', 'carteira-investimentos.csv');
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }, 1000);
  };

  // Import mock Excel/Sheet layout simulator
  const handleImportSpreadsheet = () => {
    setIsImporting(true);
    setTimeout(() => {
      setIsImporting(false);
      // Append a beautiful newly-imported asset
      const importedAsset: Asset = {
        id: `imported-${Date.now()}`,
        corretora: 'XP Investimentos',
        displayName: 'CDB Bradesco Auto 115% CDI',
        observacao: 'Importação automática',
        valorAnterior: 15400.00,
        valorAtual: 16120.00,
        valorizacao: 720.00,
        tipo: 'Renda Fixa',
        subtipo: 'CDB',
        vencimento: '14/11/2027',
        mesReferencia: 'Maio',
        alerta: false
      };
      setAssets(prev => [importedAsset, ...prev]);
    }, 1300);
  };

  // Asset Dialog callbacks
  const handleOpenAddAsset = () => {
    setEditingAsset(null);
    setIsAddAssetOpen(true);
  };

  const handleOpenEditAsset = (asset: Asset) => {
    setEditingAsset(asset);
    setIsAddAssetOpen(true);
  };

  const handleDeleteAsset = (id: string) => {
    setAssets(assets.filter(a => a.id !== id));
  };

  const handleSaveAsset = (assetData: Omit<Asset, 'id'> & { id?: string }) => {
    if (assetData.id) {
      setAssets(assets.map(a => a.id === assetData.id ? { ...a, ...assetData as Asset } : a));
    } else {
      const newAsset: Asset = {
        ...assetData,
        id: `asset-${Date.now()}`
      } as Asset;
      setAssets([newAsset, ...assets]);
    }
  };

  // Quick transaction register triggers (if needed)
  const handleTriggerQuickTransaction = (asset: Asset) => {
    setTargetAssetForTx(asset);
    setIsTransactionOpen(true);
  };

  const handleSaveTransaction = (txData: Omit<Transaction, 'id'>) => {
    const newTx: Transaction = {
      ...txData,
      id: `tx-${Date.now()}`
    };
    setTransactions([newTx, ...transactions]);

    if (targetAssetForTx) {
      setAssets(assets.map(a => {
        if (a.id === targetAssetForTx.id) {
          const transVal = txData.tipo === 'Venda' ? -txData.valor : txData.valor;
          let updatedValAtual = a.valorAtual;
          if (txData.tipo === 'Compra') {
            updatedValAtual += txData.valor;
          } else if (txData.tipo === 'Venda') {
            updatedValAtual = Math.max(0, updatedValAtual - txData.valor);
          }
          return {
            ...a,
            valorAtual: updatedValAtual,
            valorizacao: a.valorizacao + transVal,
            hasCustomTransaction: true
          };
        }
        return a;
      }));
    }
  };

  const isAnyFilterActive = 
    selectedCorretora !== 'Todos' || 
    selectedClasses.length > 0 || 
    selectedSubtypes.length > 0 || 
    selectedLiquidez.length > 0 || 
    selectedB3Status.length > 0 || 
    selectedLiquidated.length > 0 || 
    selectedVenceAte !== '' ||
    selectedMonth !== 'Todos' || 
    searchQuery !== '';

  // Sum active filters
  const filterCount = 
    (selectedCorretora !== 'Todos' ? 1 : 0) + 
    selectedClasses.length + 
    selectedSubtypes.length + 
    selectedLiquidez.length + 
    selectedB3Status.length + 
    selectedLiquidated.length + 
    (selectedVenceAte !== '' ? 1 : 0) +
    (selectedMonth !== 'Todos' ? 1 : 0) + 
    (searchQuery !== '' ? 1 : 0);

  return (
    <div className="min-h-screen bg-[#f7f9fc] flex text-[#1a1c1e] font-sans overflow-x-hidden antialiased">
      
      {/* --- RECOLLAPSED NAV RAIL SIDEBAR --- */}
      <motion.aside
        animate={{ width: sidebarCollapsed ? '72px' : '240px' }}
        transition={{ type: 'spring', damping: 24, stiffness: 220 }}
        className="shrink-0 bg-white border-r border-[#dfe2e7] flex flex-col justify-between py-6 pr-2 pl-3 relative z-20 shadow-xs hidden sm:flex"
      >
        {/* Top Block: Floating Action Button (FAB) */}
        <div className="space-y-6 shrink-0 pt-2">
          {/* Floating Action Button (FAB) inside nav rail */}
          <div className="pt-1 pl-1">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={handleOpenAddAsset}
              className={`flex items-center bg-[#d1e4ff] hover:bg-[#b0d2ff] text-[#001d36] font-display font-semibold text-xs shadow-sm hover:shadow-md rounded-2xl cursor-pointer transition-all ${
                sidebarCollapsed ? 'p-3 justify-center w-11 h-11' : 'py-3 px-4 gap-2.5 w-full justify-start'
              }`}
              title="Adicionar Novo Ativo"
              id="add-asset-sidebar"
            >
              <Plus size={18} className="text-[#0061a4] stroke-[3]" />
              {!sidebarCollapsed && <span>Novo Ativo</span>}
            </motion.button>
          </div>
        </div>

        {/* Center Block: Active menu centered vertically relative to the screen height as required */}
        <div className="flex-1 flex flex-col justify-center items-center py-8">
          <div className="w-full">
            {sidebarCollapsed ? (
              <button
                onClick={() => setActiveTab('carteira')}
                className="w-full flex flex-col items-center justify-center py-2 relative cursor-pointer group"
                title="Histórico"
              >
                <div className={`w-14 h-8 rounded-full flex items-center justify-center transition-all ${
                  activeTab === 'carteira'
                    ? 'bg-[#d3e3fd] text-[#041e49]'
                    : 'text-[#43474e] group-hover:bg-[#f1f3f8]'
                }`}>
                  <History size={20} className={activeTab === 'carteira' ? 'stroke-[2.5]' : 'stroke-2'} />
                </div>
                <span className={`text-[10px] font-sans font-semibold mt-1.5 tracking-wide ${
                  activeTab === 'carteira'
                    ? 'text-[#001d36] font-bold'
                    : 'text-[#43474e]'
                }`}>
                  Histórico
                </span>
              </button>
            ) : (
              <button
                onClick={() => setActiveTab('carteira')}
                className={`w-full flex items-center gap-3.5 px-4 h-11 rounded-full cursor-pointer transition-all ${
                  activeTab === 'carteira'
                    ? 'bg-[#d3e3fd] text-[#041e49] font-bold shadow-xs'
                    : 'text-[#43474e] hover:bg-[#f1f3f8] font-semibold'
                }`}
                title="Histórico"
              >
                <div className="flex items-center justify-center shrink-0">
                  <History size={20} className={activeTab === 'carteira' ? 'stroke-[2.5]' : 'stroke-2'} />
                </div>
                <span className="text-xs transition-opacity duration-150">
                  Histórico
                </span>
              </button>
            )}
          </div>
        </div>


      </motion.aside>

      {/* --- MAIN PAGE CONTENT WRAPPER --- */}
      <div className="flex-1 flex flex-col min-w-0 h-screen overflow-y-auto lg:overflow-hidden">
        
        {/* --- CLEANED HIGH-DENSITY TOP BAR (No "Painel Financeiro" and No subtitle) --- */}
        <header className="bg-white/80 backdrop-blur-md border-b border-[#dfe2e7] px-5 py-3 flex flex-col sm:flex-row items-center justify-between gap-3 sticky top-0 z-30 select-none">
          <div className="w-full sm:w-auto">
            <h2 className="text-xl font-display font-bold tracking-tight text-[#001d36]">
              Histórico de Posicionamento da Carteira
            </h2>
          </div>

          {/* Quick interaction filters row */}
          <div className="flex items-center justify-between sm:justify-end gap-2.5 w-full sm:w-auto overflow-x-auto shrink-0 py-1">
            


            {/* Mês de Referência Filter (Dropdown Menu) */}
            <div className="flex items-center gap-1.5 shrink-0 bg-[#f1f3f8] hover:bg-[#ebedf2] border border-transparent px-2.5 h-8 rounded-full transition-all">
              <Calendar size={13} className="text-[#0061a4]" />
              <select
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                className="bg-transparent border-none outline-none text-[11px] font-semibold text-[#101c2b] pr-4 appearance-none cursor-pointer font-sans"
              >
                <option value="Todos">Todos os Meses</option>
                {MESES_LIST.map((m) => (
                  <option key={m} value={m}>{m}</option>
                ))}
              </select>
            </div>

            {/* Exportar Button with Live Loading Action */}
            <button
              onClick={handleExportData}
              disabled={isExporting}
              className={`inline-flex items-center justify-center gap-1.5 h-8 px-3 text-[11px] font-bold rounded-full outline-none transition-all cursor-pointer ${
                isExporting 
                  ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' 
                  : 'bg-[#d1e4ff] hover:bg-[#b0d2ff] text-[#001d36]'
              }`}
              title="Exportar Ativos sob filtros"
            >
              {isExporting ? (
                <>
                  <span className="w-3.5 h-3.5 border-2 border-emerald-700 border-t-transparent rounded-full animate-spin shrink-0" />
                  <span>Exportando...</span>
                </>
              ) : (
                <>
                  <Upload size={12} />
                  <span>Exportar</span>
                </>
              )}
            </button>

            {/* Importar Button with Live Loading Action */}
            <button
              onClick={handleImportSpreadsheet}
              disabled={isImporting}
              className={`inline-flex items-center justify-center gap-1.5 h-8 px-3 text-[11px] font-bold rounded-full outline-none transition-all cursor-pointer ${
                isImporting 
                  ? 'bg-yellow-50 text-amber-700 border border-yellow-200' 
                  : 'bg-[#d1e4ff] hover:bg-[#b0d2ff] text-[#001d36]'
              }`}
              title="Importar Nova LCI/CDB de planilha"
            >
              {isImporting ? (
                <>
                  <span className="w-3.5 h-3.5 border-2 border-amber-700 border-t-transparent rounded-full animate-spin shrink-0" />
                  <span>Carregando...</span>
                </>
              ) : (
                <>
                  <Download size={12} />
                  <span>Importar</span>
                </>
              )}
            </button>
          </div>
        </header>

        {/* --- COMPACT HIGH-USAGE LAYOUT GRID --- */}
        <div className="p-5 grid grid-cols-1 lg:grid-cols-5 gap-5 items-stretch flex-1 min-h-0 overflow-y-auto lg:overflow-hidden">
          
          {/* MAIN COLUMN (Takes 4 cols wide of 5 for optimal desktop width) */}
          <section className="lg:col-span-4 flex flex-col gap-4 h-full min-h-0">
            
            {/* --- CORE TABLE CONTAINER --- */}
            {activeTab === 'carteira' && (
              <div className="bg-white rounded-[20px] border border-[#dfe2e7] shadow-xs overflow-hidden relative flex-1 flex flex-col min-h-0">
                
                {/* Scrollable table view block */}
                <div className="overflow-y-auto overflow-x-auto w-full inline-block block mt-1 relative scrollbar-sm flex-1 min-h-0">
                  <table className="w-full border-collapse text-left relative min-w-[1240px]">
                    
                    {/* FIXED THEAD */}
                    <thead className="sticky top-0 bg-[#f7f9fc] z-20 font-bold border-b border-[#ebedf2] shadow-xs">
                      <tr className="select-none h-11 text-xs text-[#43474e] bg-[#f7f9fc]">
                        {renderSortableHeader('corretora', 'Corretora', 'w-[124px]', 'left')}
                        {renderSortableHeader('displayName', 'Descrição Comercial', 'w-[240px]', 'left')}
                        {renderSortableHeader('tipo', 'Classe', 'w-[90px]', 'left')}
                        {renderSortableHeader('subtipo', 'Subtipo', 'w-[110px]', 'left')}
                        {renderSortableHeader('vencimento', 'Vencimento', 'w-[95px]', 'center')}
                        {renderSortableHeader('observacao', 'Nota / Objetivo', 'w-[110px]', 'left')}
                        {renderSortableHeader('transacoes', 'Transações', 'w-[125px]', 'right')}
                        {renderSortableHeader('valorAnterior', 'Valor Anterior', '', 'right')}
                        {renderSortableHeader('valorAtual', 'Valor Atualizado', '', 'right')}
                        {renderSortableHeader('valorizacao', 'Valorização', 'w-[145px]', 'center')}
                        {renderSortableHeader('codigoB3', 'B3', 'w-11', 'center')}
                      </tr>
                    </thead>

                    {/* SCROLLABLE BODY */}
                    <tbody className="divide-y divide-[#ebedf2]">
                      <AnimatePresence initial={false}>
                        {sortedFilteredAssets.length > 0 ? (
                          sortedFilteredAssets.map((asset) => (
                            <AssetRow
                              key={asset.id}
                              asset={asset}
                              onRowClick={handleOpenEditAsset}
                              monthTransactionsBalance={getAssetTransactionsBalanceForMonth(asset, selectedMonth)}
                            />
                          ))
                        ) : (
                          <tr>
                            <td colSpan={11} className="py-16 text-center text-[#535f70] font-sans">
                              <div className="max-w-sm mx-auto">
                                <p className="text-base font-bold font-display text-gray-950 mb-0.5">Nenhum ativo localizado</p>
                                <p className="text-xs text-gray-500">Ajuste os filtros de corretoras ou mude o mês de referência acima para listar novos ativos.</p>
                                <button
                                  onClick={handleClearFilters}
                                  className="mt-3 px-4.5 py-1.5 bg-[#d1e4ff] hover:bg-[#b0d2ff] text-[#001d36] font-display font-bold text-xs rounded-full cursor-pointer transition-all active:scale-95"
                                >
                                  Limpar filtros ativos
                                </button>
                              </div>
                            </td>
                          </tr>
                        )}
                      </AnimatePresence>
                    </tbody>

                  </table>
                </div>



              </div>
            )}


            {/* --- BROKERS BUTTONS DECK (ALWAYS VISIBLE BELOW THE TABLE) --- */}
            <div className="bg-white rounded-[20px] border border-[#dfe2e7] p-4 shadow-xs select-none">
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                <div className="flex items-center gap-1.5 shrink-0">
                  <Filter size={13} className="text-[#0061a4]" />
                  <span className="text-[10px] font-bold text-gray-500 uppercase tracking-widest font-mono">Corretora Distribuidora:</span>
                </div>
                
                <div className="flex flex-wrap gap-1.5 w-full sm:justify-end">
                  {/* Highlighted "Todos" button (Dark Red) */}
                  <button
                    onClick={() => handleSelectCorretora('Todos')}
                    className={`px-4 py-1.5 text-xs font-sans font-bold rounded-xl border cursor-pointer transition-all duration-150 ${
                      selectedCorretora === 'Todos'
                        ? 'bg-red-800 hover:bg-red-900 text-white border-transparent ring-2 ring-red-800/25 font-extrabold shadow-md'
                        : 'bg-red-50 text-red-800 border-red-200/50 hover:bg-red-100 font-bold'
                    }`}
                    id="broker-pill-todos"
                  >
                    Todos
                  </button>

                  {CORRETORAS_LIST.map((broker) => {
                    const isSelected = selectedCorretora === broker;
                    return (
                      <button
                        key={broker}
                        onClick={() => handleSelectCorretora(broker)}
                        className={`px-3 py-1.5 text-xs font-sans font-semibold rounded-xl border cursor-pointer transition-all duration-150 ${
                          isSelected
                            ? 'bg-[#001d36] text-white border-transparent shadow-xs font-bold'
                            : 'bg-[#f7f9fc] text-[#43474e] border-[#c3c7cf]/60 hover:bg-[#ebedf2]'
                        }`}
                        id={`broker-pill-${broker.replace(/\s+/g, '-').toLowerCase()}`}
                      >
                        {broker}
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

          </section>

          {/* RIGHT SIDEBAR: VERTICAL STACK OF SUMMARY CARDS (Placed to the right of the table!) */}
          <aside className="lg:col-span-1 flex flex-col gap-4 lg:overflow-y-auto lg:max-h-full scrollbar-sm pb-2">
            
            {/* FILTERS PANEL */}
            <div className="bg-white rounded-[20px] border border-[#dfe2e7] p-4.5 space-y-4 shadow-xs select-none">
              <div className="flex items-center justify-between border-b border-[#ebedf2] pb-2">
                <div className="flex items-center gap-1.5">
                  <Filter size={14} className="text-[#0061a4]" />
                  <h3 className="text-xs font-bold text-gray-900 uppercase tracking-widest font-sans">Filtros da Carteira</h3>
                </div>
                {isFilterModified && (
                  <button
                    onClick={handleClearFilters}
                    className="text-[10px] font-bold text-red-600 hover:underline cursor-pointer"
                  >
                    Resetar
                  </button>
                )}
              </div>

              {/* Classes Filter Option */}
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Layers size={13} className="text-[#0061a4] shrink-0" />
                  <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider font-mono">Classe</span>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {(['Renda Fixa', 'Renda Variável', 'Fundos'] as const).map((cls) => {
                    const isSelected = selectedClasses.includes(cls);
                    return (
                      <button
                        key={cls}
                        type="button"
                        onClick={() => handleToggleClass(cls)}
                        className={`px-2.5 py-1 text-xs font-sans font-semibold rounded-lg border cursor-pointer transition-all duration-150 ${
                          isSelected
                            ? 'bg-[#001d36] text-white border-transparent shadow-xs font-bold'
                            : 'bg-[#f7f9fc] text-[#43474e] border-[#c3c7cf]/60 hover:bg-[#ebedf2]'
                        }`}
                      >
                        {cls}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Subtype Filter: Segmented by Class */}
              {selectedClasses.length > 0 && (
                <div className="space-y-3 pt-2 border-t border-[#ebedf2]/50">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Filter size={13} className="text-[#0061a4] shrink-0" />
                      <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider font-mono">Subtipos por Classe</span>
                    </div>
                    {selectedSubtypes.length > 0 && (
                      <button
                        onClick={() => setSelectedSubtypes([])}
                        className="text-[9px] font-bold text-red-500 hover:underline cursor-pointer"
                      >
                        Limpar
                      </button>
                    )}
                  </div>
                  
                  <div className="space-y-2.5 max-h-60 overflow-y-auto scrollbar-sm pr-1">
                    {(['Renda Fixa', 'Renda Variável', 'Fundos'] as const).map((cls) => {
                      if (!selectedClasses.includes(cls)) {
                        return null;
                      }
                      
                      const subtypes = SUBTYPES_MAP[cls] || [];
                      return (
                        <div key={cls} className="space-y-1.5 bg-gray-50/60 p-2 rounded-xl border border-[#dfe2e7]/40">
                          <div className="flex items-center justify-between px-0.5">
                            <span className="text-[9px] font-extrabold text-blue-800/80 uppercase tracking-widest">{cls}</span>
                          </div>
                          <div className="flex flex-wrap gap-1">
                            {subtypes.map((sub) => {
                              const isSelected = selectedSubtypes.includes(sub);
                              return (
                                <button
                                  key={sub}
                                  type="button"
                                  onClick={() => handleToggleSubtype(sub)}
                                  className={`px-2 py-0.5 text-[10.5px] font-sans font-medium rounded-md border cursor-pointer transition-all duration-150 ${
                                    isSelected
                                      ? 'bg-[#002d56] text-white border-transparent shadow-xs font-bold'
                                      : 'bg-white text-[#43474e] border-[#c3c7cf]/40 hover:bg-gray-100 hover:text-[#1a1c1e]'
                                  }`}
                                >
                                  {sub}
                                </button>
                              );
                            })}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Liquidez Filter */}
              <div className="space-y-2 pt-2 border-t border-[#ebedf2]/50">
                <div className="flex items-center gap-1.5">
                  <Calendar size={13} className="text-[#0061a4] shrink-0" />
                  <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider font-mono">Liquidez</span>
                </div>
                <div className="flex flex-wrap gap-1">
                  {(Array.from(new Set(assets.map(a => a.liquidez || getAssetLiquidez(a)).filter(Boolean))).sort() as string[]).map((liq) => {
                    const isSelected = selectedLiquidez.includes(liq);
                    return (
                      <button
                        key={liq}
                        type="button"
                        onClick={() => handleToggleLiquidez(liq)}
                        className={`px-2 py-0.5 text-[10.5px] font-sans font-semibold rounded-md border cursor-pointer transition-all duration-150 ${
                          isSelected
                            ? 'bg-[#004176] text-white border-transparent shadow-xs font-bold'
                            : 'bg-white text-[#43474e] border-[#c3c7cf]/40 hover:bg-gray-100 hover:text-[#1a1c1e]'
                        }`}
                      >
                        {liq}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* B3 & Ativos Liquidados Filters in Same row */}
              <div className="grid grid-cols-2 gap-3 pt-2 border-t border-[#ebedf2]/50">
                {/* B3 Informado Filter */}
                <div className="space-y-2">
                  <div className="flex items-center gap-1.5">
                    <Info size={13} className="text-[#0061a4] shrink-0" />
                    <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider font-mono">B3 Informado</span>
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {['Sim', 'Não'].map((status) => {
                      const isSelected = selectedB3Status.includes(status);
                      return (
                        <button
                          key={status}
                          type="button"
                          onClick={() => handleToggleB3Status(status)}
                          className={`px-2.5 py-0.5 text-[11px] font-sans font-semibold rounded-md border cursor-pointer transition-all duration-150 ${
                            isSelected
                              ? 'bg-[#004176] text-white border-transparent shadow-xs font-bold'
                              : 'bg-white text-[#43474e] border-[#c3c7cf]/40 hover:bg-gray-100 hover:text-[#1a1c1e]'
                          }`}
                        >
                          {status}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Ativos Liquidados Filter */}
                <div className="space-y-2">
                  <div className="flex items-center gap-1.5">
                    <RotateCw size={13} className="text-[#0061a4] shrink-0" />
                    <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider font-mono">Liquidados</span>
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {['Sim', 'Não'].map((status) => {
                      const isSelected = selectedLiquidated.includes(status);
                      return (
                        <button
                          key={status}
                          type="button"
                          onClick={() => handleToggleLiquidated(status)}
                          className={`px-2.5 py-0.5 text-[11px] font-sans font-semibold rounded-md border cursor-pointer transition-all duration-150 ${
                            isSelected
                              ? 'bg-[#004176] text-white border-transparent shadow-xs font-bold'
                              : 'bg-white text-[#43474e] border-[#c3c7cf]/40 hover:bg-gray-100 hover:text-[#1a1c1e]'
                          }`}
                        >
                          {status}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* Vence até Filter */}
              <div className="space-y-2 pt-2 border-t border-[#ebedf2]/50">
                <div className="flex items-center gap-1.5">
                  <Calendar size={13} className="text-[#0061a4] shrink-0" />
                  <span className="text-[10px] font-bold text-gray-500 uppercase tracking-wider font-mono">Vence até</span>
                </div>
                <div className="relative w-full">
                  <select
                    value={selectedVenceAte}
                    onChange={(e) => setSelectedVenceAte(e.target.value)}
                    className="w-full bg-white border border-[#c3c7cf]/60 outline-none text-xs font-semibold text-[#101c2b] px-3 py-1.5 rounded-xl appearance-none cursor-pointer font-sans focus:border-[#0061a4] focus:ring-1 focus:ring-[#0061a4]/25"
                  >
                    <option value="">Qualquer Vencimento</option>
                    {getMaturityOptions().map((opt) => (
                      <option key={`${opt.year}-${String(opt.month + 1).padStart(2, '0')}`} value={`${opt.year}-${String(opt.month + 1).padStart(2, '0')}`}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2.5 text-gray-500">
                    <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                      <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/>
                    </svg>
                  </div>
                </div>
              </div>
            </div>

            {/* --- TWO-COLUMN BENTO GRID OF PORTFOLIO SUMMARY CARDS --- */}
            <div className="grid grid-cols-2 gap-3 select-none">
              
              {/* Card 1: Valor Anterior */}
              <div className="p-3.5 rounded-[18px] bg-white border border-gray-200 text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200">
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Valor Anterior
                  </p>
                  <div className="w-6 h-6 rounded-full bg-gray-100 border border-gray-200 text-gray-600 flex items-center justify-center shrink-0 shadow-2xs">
                    <Briefcase size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className="text-[15px] font-black font-sans tracking-tight text-gray-900 truncate leading-none">
                    {formatBRL(valorAnteriorTotal)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Soma dos valores anteriores">
                    Soma dos valores anteriores
                  </p>
                </div>
              </div>

              {/* Card 2: Valor Atual */}
              <div className="p-3.5 rounded-[18px] bg-white border border-gray-200 text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200">
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Valor Atual
                  </p>
                  <div className="w-6 h-6 rounded-full bg-gray-100 border border-gray-200 text-gray-600 flex items-center justify-center shrink-0 shadow-2xs">
                    <Wallet size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className="text-[15px] font-black font-sans tracking-tight text-gray-900 truncate leading-none">
                    {formatBRL(valorAtualTotal)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Soma do valor atualizado">
                    Soma do valor atualizado
                  </p>
                </div>
              </div>

              {/* Card 3: Aportes */}
              <div className={`p-3.5 rounded-[18px] text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200 border ${
                aportesTotal >= 0 
                  ? 'bg-blue-50/75 border-blue-200' 
                  : 'bg-red-50/75 border-red-200'
              }`}>
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Aportes
                  </p>
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border shadow-2xs transition-all ${
                    aportesTotal >= 0 
                      ? 'bg-blue-50 border-blue-100 text-blue-600' 
                      : 'bg-red-50 border-red-100 text-[#ba1a1a]'
                  }`}>
                    <Plus size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className={`${aportesTotal >= 0 ? 'text-blue-700' : 'text-red-700'} text-[15px] font-black font-sans tracking-tight truncate leading-none`}>
                    {formatBRL(aportesTotal)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Soma das transações de compras">
                    Soma das transações (compras)
                  </p>
                </div>
              </div>

              {/* Card 4: Retiradas */}
              <div className={`p-3.5 rounded-[18px] text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200 border ${
                retiradasTotal > 0 
                  ? 'bg-amber-50/75 border-amber-200' 
                  : 'bg-white border border-gray-200'
              }`}>
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Retiradas
                  </p>
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border transition-all shadow-2xs ${
                    retiradasTotal > 0 
                      ? 'bg-amber-50 border-amber-200 text-amber-600' 
                      : 'bg-gray-100 border-gray-200 text-gray-400'
                  }`}>
                    <X size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className={`${retiradasTotal > 0 ? 'text-amber-600' : 'text-gray-500'} text-[15px] font-black font-sans tracking-tight truncate leading-none`}>
                    {formatBRL(retiradasTotal === 0 ? 0 : -retiradasTotal)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Soma das transações de vendas">
                    Soma das transações (vendas)
                  </p>
                </div>
              </div>

              {/* Card 5: Crescimento */}
              <div className={`p-3.5 rounded-[18px] text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200 border ${
                crescimentoTotal >= 0 
                  ? 'bg-emerald-50/75 border-emerald-200' 
                  : 'bg-red-50/75 border-red-200'
              }`}>
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Crescimento
                  </p>
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border shadow-2xs transition-all ${
                    crescimentoTotal >= 0 
                      ? 'bg-emerald-50 border-emerald-100 text-emerald-600' 
                      : 'bg-red-50 border-red-100 text-[#ba1a1a]'
                  }`}>
                    <Layers size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className={`${crescimentoTotal >= 0 ? 'text-emerald-700' : 'text-red-700'} text-[15px] font-black font-sans tracking-tight truncate leading-none`}>
                    {formatBRL(crescimentoTotal)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Aportes − Retiradas + Valorização">
                    Aportes − Retiradas + Valorização
                  </p>
                </div>
              </div>

              {/* Card 6: Percentual de Crescimento */}
              <div className={`p-3.5 rounded-[18px] text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200 border ${
                percentualCrescimento >= 0 
                  ? 'bg-emerald-50/75 border-emerald-200' 
                  : 'bg-red-50/75 border-red-200'
              }`}>
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    % Crescimento
                  </p>
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border shadow-2xs transition-all ${
                    percentualCrescimento >= 0 
                      ? 'bg-emerald-50 border-emerald-100 text-emerald-600' 
                      : 'bg-red-50 border-red-100 text-[#ba1a1a]'
                  }`}>
                    <BarChart2 size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className={`${percentualCrescimento >= 0 ? 'text-emerald-700' : 'text-red-700'} text-[15px] font-black font-sans tracking-tight truncate leading-none`}>
                    {formatPercentReal(percentualCrescimento)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="% em relação ao valor anterior">
                    % em relação ao valor anterior
                  </p>
                </div>
              </div>

              {/* Card 7: Lucro */}
              <div className={`p-3.5 rounded-[18px] text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200 border ${
                lucroTotal >= 0 
                  ? 'bg-emerald-50/75 border-emerald-200' 
                  : 'bg-red-50/75 border-red-200'
              }`}>
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Lucro
                  </p>
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border shadow-2xs transition-all ${
                    lucroTotal >= 0 
                      ? 'bg-emerald-50 border-emerald-100 text-emerald-600' 
                      : 'bg-red-50 border-red-100 text-[#ba1a1a]'
                  }`}>
                    <TrendingUp size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className={`${lucroTotal >= 0 ? 'text-emerald-700' : 'text-red-700'} text-[15px] font-black font-sans tracking-tight truncate leading-none`}>
                    {lucroTotal >= 0 ? '+' : ''}{formatBRL(lucroTotal)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Calculo de quanto rendeu os investimentos">
                    Rendimento dos investimentos
                  </p>
                </div>
              </div>

              {/* Card 8: Valorização */}
              <div className={`p-3.5 rounded-[18px] text-[#1e1f22] flex flex-col justify-between min-h-[110px] relative overflow-hidden shadow-xs hover:shadow-md hover:scale-[1.01] hover:brightness-[0.99] transition-all duration-200 border ${
                valorizacaoPercentual >= 0 
                  ? 'bg-emerald-50/75 border-emerald-200' 
                  : 'bg-red-50/75 border-red-200'
              }`}>
                <div className="flex justify-between items-start w-full">
                  <p className="text-[10px] font-bold text-gray-600 uppercase tracking-wide font-sans truncate">
                    Valorização
                  </p>
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border shadow-2xs transition-all ${
                    valorizacaoPercentual >= 0 
                      ? 'bg-emerald-50 border-emerald-100 text-emerald-600' 
                      : 'bg-red-50 border-red-100 text-[#ba1a1a]'
                  }`}>
                    <BarChart2 size={12} className="stroke-[2.5]" />
                  </div>
                </div>
                <div className="space-y-0.5 z-10 mt-2">
                  <h4 className={`${valorizacaoPercentual >= 0 ? 'text-emerald-700' : 'text-red-700'} text-[15px] font-black font-sans tracking-tight truncate leading-none`}>
                    {formatPercentReal(valorizacaoPercentual)}
                  </h4>
                  <p className="text-[9px] text-gray-500 font-medium font-sans truncate" title="Percentual em relação ao valor final">
                    % em relação ao valor final
                  </p>
                </div>
              </div>

            </div>

            {/* ACTIVE FILTERS SUMMARY has been removed per user instructions */}

          </aside>

        </div>



      </div>

      {/* --- MODAL DIALOGS --- */}
      
      {/* 1. Add/Edit Asset Dialog modal */}
      <MD3Dialog
        isOpen={isAddAssetOpen}
        onClose={() => setIsAddAssetOpen(false)}
        onSave={handleSaveAsset}
        onDeleteAsset={handleDeleteAsset}
        editingAsset={editingAsset}
      />

      {/* 2. Add Transaction Dialog modal */}
      <MD3TransactionDialog
        isOpen={isTransactionOpen}
        onClose={() => {
          setIsTransactionOpen(false);
          setTargetAssetForTx(null);
        }}
        asset={targetAssetForTx}
        onSave={handleSaveTransaction}
      />

    </div>
  );
}
