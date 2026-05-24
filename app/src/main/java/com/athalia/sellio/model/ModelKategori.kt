package com.athalia.sellio.model
import android.os.Parcel
import android.os.Parcelable

data class ModelKategori(
    var idKategori: String? =null,
    var namaKategori: String? =null,
    var statusKategori: String? =null
) : Parcelable {
    //Constructor dari parcel
    constructor(parcel: Parcel) : this(
        idKategori =  parcel.readString(),
        namaKategori = parcel.readString(),
        statusKategori = parcel.readString()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idKategori)
        parcel.writeString(namaKategori)
        parcel.writeString(statusKategori)
    }

    companion object CREATOR : Parcelable.Creator<ModelKategori> {
        override fun createFromParcel(parcel: Parcel): ModelKategori {
            return ModelKategori(parcel)
        }

        override fun newArray(size: Int): Array<ModelKategori?> {
            return arrayOfNulls(size)
        }
    }
}