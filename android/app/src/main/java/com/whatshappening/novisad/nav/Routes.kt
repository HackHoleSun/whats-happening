package com.whatshappening.novisad.nav

object Routes {
    const val Home   = "home"
    const val Detail = "detail/{eventId}"
    const val Search = "search"
    const val Map    = "map"
    const val Saved  = "saved"

    fun detail(id: String) = "detail/$id"
}
