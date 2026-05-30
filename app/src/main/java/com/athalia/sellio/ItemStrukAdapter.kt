package com.athalia.sellio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.databinding.ItemStrukTransaksiBinding
import com.athalia.sellio.model.ItemTransaksi
import java.text.NumberFormat
import java.util.Locale

class ItemStrukAdapter(
    private val items: ArrayList<ItemTransaksi>
) : RecyclerView.Adapter<ItemStrukAdapter.StrukViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StrukViewHolder {
        val binding = ItemStrukTransaksiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StrukViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StrukViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class StrukViewHolder(
        private val binding: ItemStrukTransaksiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemTransaksi) {
            binding.tvNamaProduk.text = item.namaProduk.uppercase()
            binding.tvJumlah.text = "x${item.jumlah}"
            binding.tvHarga.text = "Rp " + formatNumber(item.harga.toLong())
            binding.tvSubtotal.text = "Rp " + formatNumber(item.subtotal)
        }

        private fun formatNumber(amount: Long): String {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            return formatter.format(amount)
        }
    }
}