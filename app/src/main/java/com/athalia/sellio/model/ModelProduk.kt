package com.athalia.sellio.model

import android.os.Parcel
import android.os.Parcelable

data class ModelProduk(
    var idProduk: String = "",
    var namaProduk: String = "",
    var hargaProduk: Int = 0,
    var hargaModal: Int = 0,
    var idKategori: String = "",
    var idCabang: String = "",
    var fotoProduk: String = "",
    var stokProduk: Int = 0,
    var tanpaBatas: Boolean = false,
    var statusProduk: String = "1",
    var createdAt: String = "",
    var updatedAt: String = ""
) : Parcelable {

    // Properti tambahan yang tidak disimpan ke Parcel
    var jumlahTerjual: Int = 0
        get() = field
        set(value) { field = value }

    // Properti tambahan untuk keperluan tampilan
    var namaKategori: String = ""
    var namaCabang: String = ""

    // Constructor dari Parcel
    constructor(parcel: Parcel) : this(
        idProduk = parcel.readString() ?: "",
        namaProduk = parcel.readString() ?: "",
        hargaProduk = parcel.readInt(),
        hargaModal = parcel.readInt(),
        idKategori = parcel.readString() ?: "",
        idCabang = parcel.readString() ?: "",
        fotoProduk = parcel.readString() ?: "",
        stokProduk = parcel.readInt(),
        tanpaBatas = parcel.readByte() != 0.toByte(),
        statusProduk = parcel.readString() ?: "1",
        createdAt = parcel.readString() ?: "",
        updatedAt = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeInt(hargaProduk)
        parcel.writeInt(hargaModal)
        parcel.writeString(idKategori)
        parcel.writeString(idCabang)
        parcel.writeString(fotoProduk)
        parcel.writeInt(stokProduk)
        parcel.writeByte(if (tanpaBatas) 1 else 0)
        parcel.writeString(statusProduk)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModelProduk> {
        override fun createFromParcel(parcel: Parcel): ModelProduk {
            return ModelProduk(parcel)
        }

        override fun newArray(size: Int): Array<ModelProduk?> {
            return arrayOfNulls(size)
        }
    }
}