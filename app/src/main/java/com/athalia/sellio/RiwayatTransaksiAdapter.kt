package com.athalia.sellio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athalia.sellio.databinding.ItemRiwayatTransaksiBinding
import com.athalia.sellio.model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class RiwayatTransaksiAdapter(
    private val transaksiList: ArrayList<ModelTransaksi>,
    private val onItemClick: (ModelTransaksi) -> Unit,
    private val onPrintClick: (ModelTransaksi) -> Unit
) : RecyclerView.Adapter<RiwayatTransaksiAdapter.RiwayatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val binding = ItemRiwayatTransaksiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RiwayatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        holder.bind(transaksiList[position])
    }

    override fun getItemCount() = transaksiList.size

    fun updateList(newList: ArrayList<ModelTransaksi>) {
        transaksiList.clear()
        transaksiList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class RiwayatViewHolder(
        private val binding: ItemRiwayatTransaksiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaksi: ModelTransaksi) {
            binding.tvNoTransaksi.text = transaksi.idTransaksi
            binding.tvTotal.text = formatRupiah(transaksi.total)
            binding.tvTanggal.text = transaksi.tanggal
            binding.tvWaktu.text = transaksi.waktu

            val jumlahItem = transaksi.items.sumOf { it.jumlah }
            binding.tvJumlahItem.text = "$jumlahItem item"

            when (transaksi.status) {
                "selesai" -> {
                    binding.chipStatus.text = "Selesai"
                    binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                }
                "batal" -> {
                    binding.chipStatus.text = "Batal"
                    binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
                }
                else -> {
                    binding.chipStatus.text = "Proses"
                    binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                }
            }

            binding.ivPrint.setOnClickListener {
                onPrintClick(transaksi)
            }

            binding.root.setOnClickListener {
                onItemClick(transaksi)
            }
        }

        private fun formatRupiah(amount: Long): String {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            return "Rp ${formatter.format(amount)}"
        }
    }
}