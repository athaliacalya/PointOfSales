package com.athalia_calya.pos.model

import android.os.Parcel
import android.os.Parcelable

data class ModelTransaksi(
    var idTransaksi: String = "",
    var tanggal: String = "",
    var waktu: String = "",
    var items: ArrayList<ItemTransaksi> = ArrayList(),
    var subtotal: Long = 0,
    var diskon: Long = 0,
    var pajak: Long = 0,
    var total: Long = 0,
    var bayar: Long = 0,
    var kembali: Long = 0,
    var pelangganId: String = "",
    var pelangganNama: String = "",
    var kasir: String = "",
    var status: String = "selesai"
) : Parcelable {

    constructor(parcel: Parcel) : this(
        idTransaksi = parcel.readString() ?: "",
        tanggal = parcel.readString() ?: "",
        waktu = parcel.readString() ?: "",
        items = parcel.readArrayList(ItemTransaksi::class.java.classLoader) as? ArrayList<ItemTransaksi> ?: ArrayList(),
        subtotal = parcel.readLong(),
        diskon = parcel.readLong(),
        pajak = parcel.readLong(),
        total = parcel.readLong(),
        bayar = parcel.readLong(),
        kembali = parcel.readLong(),
        pelangganId = parcel.readString() ?: "",
        pelangganNama = parcel.readString() ?: "",
        kasir = parcel.readString() ?: "",
        status = parcel.readString() ?: "selesai"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idTransaksi)
        parcel.writeString(tanggal)
        parcel.writeString(waktu)
        parcel.writeList(items)
        parcel.writeLong(subtotal)
        parcel.writeLong(diskon)
        parcel.writeLong(pajak)
        parcel.writeLong(total)
        parcel.writeLong(bayar)
        parcel.writeLong(kembali)
        parcel.writeString(pelangganId)
        parcel.writeString(pelangganNama)
        parcel.writeString(kasir)
        parcel.writeString(status)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelTransaksi> {
        override fun createFromParcel(parcel: Parcel): ModelTransaksi {
            return ModelTransaksi(parcel)
        }

        override fun newArray(size: Int): Array<ModelTransaksi?> {
            return arrayOfNulls(size)
        }
    }
}

data class ItemTransaksi(
    var idProduk: String = "",
    var namaProduk: String = "",
    var harga: Int = 0,
    var jumlah: Int = 0,
    var subtotal: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        idProduk = parcel.readString() ?: "",
        namaProduk = parcel.readString() ?: "",
        harga = parcel.readInt(),
        jumlah = parcel.readInt(),
        subtotal = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeInt(harga)
        parcel.writeInt(jumlah)
        parcel.writeLong(subtotal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ItemTransaksi> {
        override fun createFromParcel(parcel: Parcel): ItemTransaksi {
            return ItemTransaksi(parcel)
        }

        override fun newArray(size: Int): Array<ItemTransaksi?> {
            return arrayOfNulls(size)
        }
    }
}
