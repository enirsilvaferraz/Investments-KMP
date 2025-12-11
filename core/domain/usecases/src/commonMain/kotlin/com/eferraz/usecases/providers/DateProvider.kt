package com.eferraz.usecases.providers

import kotlinx.datetime.YearMonth

/**
 * Provider para abstrair a obtenção da data atual.
 * Permite mockar em testes e seguir o princípio de Dependency Inversion (DIP).
 */
public interface DateProvider {
    /**
     * Obtém o mês/ano atual.
     *
     * @return O YearMonth atual
     */
    public fun getCurrentYearMonth(): YearMonth
}
