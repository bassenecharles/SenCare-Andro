package com.github.bassenecharles.sencare

import java.math.BigDecimal

data class Patient(
    val id: Long = 0,
    val name: String,
    val prenom: String,
    val dateNaissance: String,
    val telephone: String,
    val adresse: String,
    val formule: String,
    val depense: BigDecimal,
    val montantAPayer: BigDecimal? = null,
    val medecinTraitant: String,
    val numMedecin: String
)
