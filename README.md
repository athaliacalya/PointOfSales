# 🛍️ Sellio - Smart Point of Sales System

<p align="center">
  <img src="app/src/main/res/drawable/ic_account.png" width="100" alt="Sellio Logo"/>
</p>

<p align="center">
  <strong>Sellio</strong> adalah aplikasi kasir pintar (Point of Sales - POS) berbasis Android yang dirancang khusus untuk membantu pemilik bisnis, manajer cabang, dan kasir dalam mengelola operasional penjualan, inventaris, karyawan, dan laporan keuangan secara efisien dan waktu nyata (real-time).
</p>

---

## 🚀 Fitur Utama

- **🔑 Sistem Keamanan & Auto-Login**: Halaman masuk (Landing Page) modern dengan validasi email/password lengkap dan status login persisten menggunakan `SharedPreferences`.
- **📊 Laporan Keuangan Real-time**: Grafik dan tabel interaktif untuk memantau pendapatan, total penjualan harian, dan ringkasan transaksi.
- **🛒 Manajemen Transaksi & Cetak Struk**: Modul kasir intuitif untuk memproses pesanan, mengelola keranjang belanja, memilih metode pembayaran, serta menerbitkan struk fisik/digital.
- **📦 Manajemen Produk & Kategori**: Mengatur menu makanan, barang, atau jasa lengkap dengan harga, deskripsi, foto, dan kategori.
- **👥 Pengelolaan Karyawan & Pelanggan**: Menyimpan basis data karyawan beserta jabatannya dan data pelanggan dengan sistem tingkat keanggotaan (Member Level) & poin loyalitas.
- **🏢 Manajemen Multi-Cabang**: Mengelola informasi operasional di berbagai cabang gerai bisnis Anda.
- **☁️ Firebase Realtime Database**: Penyimpanan data yang andal, aman, dan sinkron di semua perangkat kasir secara langsung.

---

## 📸 Antarmuka Aplikasi (Screenshots)

Berikut adalah tampilan visual premium dari aplikasi **Sellio**:

### 🔑 Autentikasi & Profil Pengguna
| Landing Page | Form Login | Profil & Manajemen Akun |
| :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/landing_page.jpeg" width="220" alt="Landing Page"/> | <img src="app/src/main/res/drawable/login.jpeg" width="220" alt="Login"/> | <img src="app/src/main/res/drawable/akun.jpeg" width="220" alt="Akun"/> |

### 📊 Dashboard & Transaksi Utama
| Beranda (Dashboard) | Halaman Transaksi Kasir | Proses Pembayaran |
| :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/beranda.jpeg" width="220" alt="Beranda"/> | <img src="app/src/main/res/drawable/transaksi.jpeg" width="220" alt="Transaksi"/> | <img src="app/src/main/res/drawable/pembayaran.jpeg" width="220" alt="Pembayaran"/> |

### 🧾 Laporan Keuangan & Riwayat
| Struk Pembayaran | Riwayat Transaksi | Grafik Laporan Keuangan |
| :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/struk.jpeg" width="220" alt="Struk"/> | <img src="app/src/main/res/drawable/riwayat_transaksi.jpeg" width="220" alt="Riwayat Transaksi"/> | <img src="app/src/main/res/drawable/laporan_keuangan.jpeg" width="220" alt="Laporan Keuangan"/> |

### 📦 Manajemen Menu & Kategori
| Daftar Menu (Produk) | Tambah/Edit Menu Baru | Pencarian Kategori | Tambah Kategori Baru |
| :---: | :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/daftar_menu.jpeg" width="180" alt="Daftar Menu"/> | <img src="app/src/main/res/drawable/tambah_menu.jpeg" width="180" alt="Tambah Menu"/> | <img src="app/src/main/res/drawable/cari_kategori.jpeg" width="180" alt="Cari Kategori"/> | <img src="app/src/main/res/drawable/tambah_kategori.jpeg" width="180" alt="Tambah Kategori"/> |

### 👥 Karyawan, Pelanggan, & Cabang
| Data Pegawai | Tambah Pegawai | Data Pelanggan | Tambah Pelanggan | Manajemen Cabang | Edit Cabang |
| :---: | :---: | :---: | :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/data_pegawai.jpeg" width="140" alt="Data Pegawai"/> | <img src="app/src/main/res/drawable/tambah_pegawai.jpeg" width="140" alt="Tambah Pegawai"/> | <img src="app/src/main/res/drawable/data_pelanggan.jpeg" width="140" alt="Data Pelanggan"/> | <img src="app/src/main/res/drawable/tambah_pelanggan.jpeg" width="140" alt="Tambah Pelanggan"/> | <img src="app/src/main/res/drawable/manajemen_cabang.jpeg" width="140" alt="Manajemen Cabang"/> | <img src="app/src/main/res/drawable/edit_cabang.jpeg" width="140" alt="Edit Cabang"/> |

---

## 🛠️ Teknologi & Stack
- **Bahasa Pemrograman**: Kotlin (100% Native Android)
- **Desain Layout**: Android XML Layout (Material Design Components)
- **Basis Data**: Firebase Realtime Database
- **Manajemen Sesi**: SharedPreferences (Local Storage)
- **Komponen Utama**: CardView, TextInputLayout, ConstraintLayout, NestedScrollView, RecyclerView.

---

## 💻 Cara Menjalankan Proyek

1. **Persiapan**:
   - Instal [Android Studio](https://developer.android.com/studio) versi terbaru.
   - Hubungkan proyek Android Anda dengan Firebase (pastikan file `google-services.json` sudah diletakkan pada folder `/app`).

2. **Kloning Repositori**:
   ```bash
   git clone https://github.com/athaliacalya/PointOfSales.git
   ```

3. **Buka Proyek**:
   - Buka Android Studio -> Pilih **Open** -> Arahkan ke folder hasil klon proyek Sellio ini.

4. **Jalankan Aplikasi**:
   - Pilih perangkat emulator atau perangkat fisik Android yang terhubung.
   - Tekan tombol **Run (Shift + F10 / ikon Play)**.

5. **Kredensial Akun Default (untuk Pengujian Kasir)**:
   - **Email**: `admin@sellio.com`
   - **Kata Sandi**: `admin123`

---

## ✒️ Kontributor
- **Nama**: Athalia Calya
- **GitHub**: [@athaliacalya](https://github.com/athaliacalya)
