package com.eferraz.entities.value

import com.eferraz.entities.assets.CNPJ
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class CNPJTest {

    @Test
    fun `deve criar CNPJ com mascara valida`() {

        val cnpj = CNPJ("12.345.678/0001-90")

        assertEquals("12.345.678/0001-90", cnpj.get())
    }

    @Test
    fun `deve criar CNPJ sem mascara valida`() {

        val cnpj = CNPJ("12345678000190")

        assertEquals("12345678000190", cnpj.get())
    }

    @Test
    fun `deve criar CNPJ com mascara e digitos diferentes`() {

        val cnpj = CNPJ("11.222.333/4444-55")

        assertEquals("11.222.333/4444-55", cnpj.get())
    }

    @Test
    fun `deve lancar excecao para string vazia`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("")
        }
    }

    @Test
    fun `deve lancar excecao para CNPJ com menos de 14 digitos`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("1234567890123") // 13 dígitos
        }
    }

    @Test
    fun `deve lancar excecao para CNPJ com mais de 14 digitos`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("123456789012345") // 15 dígitos
        }
    }

    @Test
    fun `deve lancar excecao para mascara incorreta`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("12-345-678/0001.90") // máscara incorreta
        }
    }

    @Test
    fun `deve lancar excecao para mascara incompleta`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/0001") // máscara incompleta
        }
    }

    @Test
    fun `deve lancar excecao para CNPJ com caracteres nao numericos`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/0001-AB") // contém letras
        }
    }

    @Test
    fun `deve lancar excecao para CNPJ com espacos`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/0001 90") // contém espaço
        }
    }

    @Test
    fun `deve lancar excecao para separadores em posicoes erradas`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("123.45678/0001-90") // separadores em posições erradas
        }
    }

    @Test
    fun `deve lancar excecao para mascara com numero incorreto de digitos`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("12.345.678/000-90") // número incorreto de dígitos na máscara
        }
    }

    @Test
    fun `deve lancar excecao para string com apenas caracteres especiais`() {

        assertFailsWith<IllegalArgumentException> {
            CNPJ("..//--")
        }
    }
}

