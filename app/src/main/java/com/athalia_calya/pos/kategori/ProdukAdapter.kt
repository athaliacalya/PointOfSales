package com.athalia_calya.pos.kategori

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.athalia_calya.pos.R
import com.athalia_calya.pos.model.ModelProduk
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private var produkList: ArrayList<ModelProduk>
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(produk: ModelProduk)
        fun onEditClick(produk: ModelProduk)
        fun onDeleteClick(produk: ModelProduk)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateList(newList: ArrayList<ModelProduk>) {
        produkList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk = produkList[position]
        holder.bind(produk)
    }

    override fun getItemCount(): Int = produkList.size

    inner class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgProduk: ImageView = itemView.findViewById(R.id.img_produk)
        private val tvNamaProduk: TextView = itemView.findViewById(R.id.tv_nama_produk)
        private val tvHargaProduk: TextView = itemView.findViewById(R.id.tv_harga_produk)
        private val chipStatus: Chip = itemView.findViewById(R.id.chip_status)
        private val tvStok: TextView = itemView.findViewById(R.id.tv_stok)
        private val tvKategori: TextView = itemView.findViewById(R.id.tv_kategori)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)

        fun bind(produk: ModelProduk) {
            // Set nama produk
            tvNamaProduk.text = produk.namaProduk.ifEmpty { "Produk" }

            // Set harga produk (format Rupiah)
            tvHargaProduk.text = formatRupiah(produk.hargaProduk)

            // Set stok
            tvStok.text = if (produk.stokProduk > 0) "Stok: ${produk.stokProduk}" else "Stok: Habis"

            // Set kategori - tampilkan nama kategori
            if (produk.namaKategori.isNotEmpty()) {
                tvKategori.text = produk.namaKategori
            } else {
                tvKategori.text = "Kategori: -"
            }

            // Set status chip dengan warna yang sesuai
            when (produk.statusProduk) {
                "1", "aktif", "Aktif" -> {
                    chipStatus.text = "Aktif"
                    chipStatus.setTextColor(Color.parseColor("#4CAF50"))
                    chipStatus.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    chipStatus.chipBackgroundColor = ColorStateList.valueOf(Color.TRANSPARENT)
                    chipStatus.chipStrokeWidth = 1f
                }
                else -> {
                    chipStatus.text = "Tidak Aktif"
                    chipStatus.setTextColor(Color.parseColor("#F44336"))
                    chipStatus.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#F44336"))
                    chipStatus.chipBackgroundColor = ColorStateList.valueOf(Color.TRANSPARENT)
                    chipStatus.chipStrokeWidth = 1f
                }
            }

            // Load gambar produk dengan Glide
            if (produk.fotoProduk.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(produk.fotoProduk)
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(imgProduk)
            } else {
                imgProduk.setImageResource(R.drawable.ic_product)
            }

            // Klik item
            itemView.setOnClickListener {
                listener?.onItemClick(produk)
            }

            // Tombol edit - pindah ke ModProdukActivity
            btnEdit.setOnClickListener {
                listener?.onEditClick(produk)
            }

            // Tombol delete
            btnDelete.setOnClickListener {
                listener?.onDeleteClick(produk)
            }
        }

        private fun formatRupiah(harga: Int): String {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            return "Rp ${formatter.format(harga)}"
        }
    }
}
