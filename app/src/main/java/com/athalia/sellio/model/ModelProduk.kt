package com.athalia.sellio.model

import android.os.Parcel
import android.os.Parcelable
import java.util.NavigableMap

data class ModelProduk(
    val idProduk: String = null,
    val namaProduk: String? = null,
    val hargaProduk: Int? = 0,
    val idKategori: String? = null,
    val idCabang: String? = null,
    val fotoProduk: String? = null,
    val stokProduk: Int? = 0,
    val tanpaBatas: Boolean? = false,
    val statusProduk: String? = null,
    var createdAT: String? = null,
    var updatedAt: String? = null
) : Parcelable {

    var jumlahTerjual: Int = 0
        get() = field
        set(value) {field = value}

    constructor(parcel: Parcel) : this(
        idProduk = parcel.readString(),
        namaProduk = parcel.readString(),
        hargaProduk = parcel.readValue(Int::class.java.classLoader) as? Int,
        idKategori = parcel.readString(),
        idCabang = parcel.readString(),
        fotoProduk = parcel.readString(),
        stokProduk = parcel.readValue(Int::class.java.classLoader) as? Int,
        tanpaBatas = parcel.readValue(Boolean::class.java.classLoader) as? Int,
        statusProduk = parcel.readString(),
        createdAt = parcel.readString(),
        updatedAt = parcel.readString()
    )

    override fun ariteToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeValue(hargaProduk)
        parcel.writeString(idKategori)
        parcel.writeString(idCabang)
    }
}
