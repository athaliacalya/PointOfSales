package com.athalia_calya.pos.model

data class ModelPelanggan(
    var idPelanggan: String = "",
    var namaPelanggan: String = "",
    var noTelp: String = "",
    var email: String = "",
    var alamat: String = "",
    var memberLevel: String = "Regular", // Regular, Gold, Platinum
    var poin: Int = 0,
    var totalTransaksi: Int = 0,
    var createdAt: String = "",
    var updatedAt: String = ""
)
