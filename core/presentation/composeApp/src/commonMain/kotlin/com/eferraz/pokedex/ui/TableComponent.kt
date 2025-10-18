package com.eferraz.pokedex.ui

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun <T> TableView(
    data: List<T>,
    header: @Composable HeaderScope.() -> Unit,
    body: @Composable BodyScope.(data: T) -> Unit,
) {

    val header: HeaderScopeImpl = HeaderScopeImpl().apply { header() }



    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
    ) {

    }
}

internal interface HeaderScope {
    fun header(title: String)
}

internal interface BodyScope {
    fun body(content: String)
}

@Preview
@Composable
private fun TableViewPreview() {
    TableView(
        data = listOf<FixedIncomeInvestment>(),
        header = {
            header("Owner")
        },
        body = { it ->
            body(it.owner)
        }
    )
}

internal class HeaderScopeImpl : HeaderScope {
    override fun header(title: String) {
        TODO("Not yet implemented")
    }

}