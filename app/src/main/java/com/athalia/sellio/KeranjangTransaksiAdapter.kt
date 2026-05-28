package com.athalia.sellio

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.databinding.ItemKeranjangTransaksiBinding
import com.athalia.sellio.model.ItemTransaksi
import java.text.NumberFormat
import java.util.Locale

class KeranjangTransaksiAdapter(
    private val items: ArrayList<ItemTransaksi>,
    private val onItemChanged: () -> Unit,
    private val onItemRemoved: () -> Unit
) : RecyclerView.Adapter<KeranjangTransaksiAdapter.KeranjangViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeranjangViewHolder {
        val binding = ItemKeranjangTransaksiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KeranjangViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeranjangViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class KeranjangViewHolder(
        private val binding: ItemKeranjangTransaksiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemTransaksi) {
            binding.tvNamaProduk.text = item.namaProduk
            binding.tvHarga.text = formatRupiah(item.harga.toLong())
            binding.tvJumlah.text = item.jumlah.toString()
            binding.tvSubtotal.text = formatRupiah(item.subtotal)

            binding.btnMinus.setOnClickListener {
                if (item.jumlah > 1) {
                    item.jumlah--
                    item.subtotal = item.harga.toLong() * item.jumlah
                    binding.tvJumlah.text = item.jumlah.toString()
                    binding.tvSubtotal.text = formatRupiah(item.subtotal)
                    onItemChanged()
                } else {
                    Toast.makeText(binding.root.context, "Minimal 1 item", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnPlus.setOnClickListener {
                item.jumlah++
                item.subtotal = item.harga.toLong() * item.jumlah
                binding.tvJumlah.text = item.jumlah.toString()
                binding.tvSubtotal.text = formatRupiah(item.subtotal)
                onItemChanged()
            }

            binding.btnDelete.setOnClickListener {
                val position = items.indexOf(item)
                if (position >= 0) {
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    onItemRemoved()
                }
            }
        }

        private fun formatRupiah(amount: Long): String {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            return "Rp ${formatter.format(amount)}"
        }
    }
}