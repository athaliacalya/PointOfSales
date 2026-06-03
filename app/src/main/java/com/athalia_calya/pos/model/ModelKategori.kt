package com.athalia_calya.pos.model

import android.os.Parcel
import android.os.Parcelable

data class ModelKategori(
    var idKategori: String = "",  // Ubah dari String? menjadi String
    var namaKategori: String = "",  // Ubah dari String? menjadi String
    var statusKategori: String = "1"  // Ubah dari String? menjadi String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        idKategori = parcel.readString() ?: "",
        namaKategori = parcel.readString() ?: "",
        statusKategori = parcel.readString() ?: "1"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idKategori)
        parcel.writeString(namaKategori)
        parcel.writeString(statusKategori)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelKategori> {
        override fun createFromParcel(parcel: Parcel): ModelKategori {
            return ModelKategori(parcel)
        }

        override fun newArray(size: Int): Array<ModelKategori?> {
            return arrayOfNulls(size)
        }
    }
}
