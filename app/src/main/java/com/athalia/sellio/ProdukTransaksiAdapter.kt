package com.athalia.sellio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.databinding.ItemProdukTransaksiBinding
import com.athalia.sellio.model.ModelProduk
import java.text.NumberFormat
import java.util.Locale

class ProdukTransaksiAdapter(
    private val produkList: ArrayList<ModelProduk>,
    private val onItemClick: (ModelProduk) -> Unit
) : RecyclerView.Adapter<ProdukTransaksiAdapter.ProdukViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val binding = ItemProdukTransaksiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProdukViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        holder.bind(produkList[position])
    }

    override fun getItemCount() = produkList.size

    inner class ProdukViewHolder(
        private val binding: ItemProdukTransaksiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produk: ModelProduk) {
            binding.tvNamaProduk.text = produk.namaProduk
            binding.tvHarga.text = formatRupiah(produk.hargaProduk.toLong())
            binding.tvStok.text = "Stok: ${produk.stokProduk}"

            binding.root.setOnClickListener {
                onItemClick(produk)
            }
        }

        private fun formatRupiah(amount: Long): String {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            return "Rp ${formatter.format(amount)}"
        }
    }
}