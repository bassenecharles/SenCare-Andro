package com.github.bassenecharles.sencare

import java.math.BigDecimal

data class Formule(
    val id: Long = 0,
    val name: String,
    val price: BigDecimal,
    val description: String
)
