# Rumah Sehat Manna wa Salwa — Aplikasi Android (Client App)

[![Status](https://img.shields.io/badge/Status-Prototype%20%E2%80%94%20Local%20Only-orange)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Repositori ini berisi kode sumber aplikasi mobile client untuk sistem manajemen dan booking janji temu klinik **Rumah Sehat Manna wa Salwa**. Aplikasi ini dibuat menggunakan bahasa pemrograman Kotlin dengan arsitektur UI modern Jetpack Compose.

---

## 📱 Tentang Aplikasi
Aplikasi **Rumah Sehat Manna wa Salwa** dirancang untuk memudahkan pasien melakukan reservasi terapi secara mandiri, memudahkan terapis mengelola agenda praktik & menulis rekam medis, serta memfasilitasi admin dalam memverifikasi transaksi dan menyusun laporan operasional klinik secara terintegrasi.

---

## 👥 Detail Fitur Utama Aplikasi
Berikut adalah rincian fungsionalitas dan fitur teknis lengkap yang telah diimplementasikan dalam aplikasi Android berdasarkan pembagian peran (*role*):

### 1. Pasien (Patient Role)
* **Katalog Layanan Klinik (Jetpack Compose):**
  * Halaman katalog interaktif berbasis Compose untuk melihat jenis terapi yang aktif (Bekam, Akupunktur, Ramuan).
  * Dilengkapi dengan estimasi durasi pelayanan, harga terperinci, dan ikon gambar dinamis yang dimuat dari server.
* **Alur Reservasi Terpandu (Booking Flow):**
  * Alur pembuatan janji temu langkah-demi-langkah (pilih layanan $\rightarrow$ pilih tanggal $\rightarrow$ pilih jam slot praktik $\rightarrow$ pilih terapis tersedia).
  * Filter jadwal dinamis untuk memastikan tidak ada slot waktu ganda pada terapis yang sama.
* **Metode Pembayaran & Unggah Berkas:**
  * Pilihan metode pembayaran **Tunai (Cash)** atau **Transfer Bank**.
  * Pengintegrasian *System Image Picker* untuk memilih berkas bukti bayar dan mengirimkannya menggunakan `MultipartBody.Part` via Retrofit.
* **Real-time Transfer Countdown (Batas Waktu Pembayaran):**
  * Hitung mundur interaktif selama 24 jam khusus untuk metode transfer bank.
  * Proteksi logika visual: countdown hanya berjalan pada transaksi berstatus `pending/unpaid` dengan metode transfer, memastikan transaksi tunai atau yang sudah terkonfirmasi tidak mengalami kesalahan visual pembatalan.
* **Kartu Informasi Antrean Dinamis (`QueueInfoCard`):**
  * Komponen visual premium berwarna *GreenPrimary* dan *GreenSoft* yang menampilkan nomor antrean hari berjalan pasien secara real-time.
  * Nomor antrean dihitung dinamis per hari per terapis dengan logika pengurutan `booking_time` (dan *tie-breaker* `created_at` jika jam booking sama).
* **Riwayat Rekam Terapi Kronologis (Timeline):**
  * Tampilan rekam medis pribadi pasien dalam bentuk linimasa (timeline) yang rapi.
  * Pasien dapat melacak perkembangan keluhan, diagnosis terapis, titik bekam yang dipasang, serta ramuan herbal yang diresepkan dari waktu ke waktu.

### 2. Terapis (Therapist Role)
* **Dashboard Statistik Terapis:**
  * Kartu indikator performa untuk memantau total sesi terapi yang telah dilayani bulan ini.
  * Indikator janji temu mendatang yang terjadwal.
  * Indikator peringatan khusus untuk sesi yang berstatus **"Belum Isi Catatan"** (akibat tindakan *Force Complete* oleh admin).
* **Agenda Hari Ini & Manajemen Sesi:**
  * Menampilkan daftar antrean pasien secara berurutan sesuai jam pelayanan hari berjalan.
  * Tombol aksi **"Mulai Sesi"** untuk memperbarui status transaksi menjadi `in_progress`.
* **Formulir Rekam Terapi (`TherapyRecordFormScreen`):**
  * Formulir pengisian rekam medis terstruktur: keluhan utama, diagnosis klinis, titik bekam (jika memilih bekam), ramuan herbal yang diresepkan, dan catatan evaluasi terapis.
* **Penanganan Transaksi Force Completed (Pengisian Catatan Riwayat):**
  * Menyelesaikan masalah transaksi yang dipaksa selesai oleh admin (*Force Completed*).
  * Janji temu ini tetap muncul pada daftar riwayat terapis dengan status khusus. Terapis wajib mengisi rekam medis dengan menekan tombol **"Isi Catatan Terapi"** langsung dari kartu riwayat janji temu tersebut agar data rekam medis pasien tetap lengkap dan akurat.

### 3. Admin / Super Admin (Management Role)
* **Manajemen Pengguna (User Management Screen):**
  * CRUD data pengguna untuk peran Pasien, Terapis, dan Admin melalui antarmuka `AdminManageUsersScreen`.
  * Filter tab untuk memisahkan data pengguna aktif dan pengguna yang dinonaktifkan (berada di keranjang sampah / *Trash*).
  * Tombol **Restore** untuk memulihkan kembali akun dari trash ke status aktif.
  * Form pembuatan dan penyuntingan akun terapis baru dengan dropdown terapis dinamis.
* **Verifikasi Transaksi Pembayaran:**
  * Layar verifikasi khusus untuk memeriksa detail pemesanan dan berkas foto bukti transfer yang diunggah pasien.
  * Aksi persetujuan (**Accept**) untuk mengubah status menjadi `confirmed` atau penolakan (**Reject**) disertai formulir pengisian alasan penolakan yang akan dikirim ke pasien.
* **Force Complete:**
  * Tombol darurat untuk menyelesaikan transaksi secara paksa jika terapis lupa menutup sesi janji temu, guna mengunci data transaksi dan laporan keuangan.
* **Multi-Tab Ekspor Laporan & Cetak PDF:**
  * Halaman laporan interaktif (`ReportScreen`) dengan tab terpisah untuk:
    1. **Laporan Keuangan:** Rekapitulasi pendapatan tunai, transfer, dan refund.
    2. **Laporan Kunjungan Terapis:** Menampilkan kunjungan pasien dengan detail alamat klinik, STPT (Surat Terdaftar Penyehat Tradisional), dan filter gender (L/P).
    3. **Laporan Kinerja Terapis:** Statistik kontribusi porsi pelayanan dan total pendapatan per terapis.
    4. **Laporan Kegiatan Klinik:** Grafik persentase kontribusi layanan klinik terpopuler.
    5. **Laporan Komparatif Performa:** Visualisasi kontribusi porsi pengerjaan sesi antar terapis (akses eksklusif untuk Super Admin).
  * **Low-Memory PDF Downloader:** Menggunakan anotasi `@Streaming` pada Retrofit untuk mengunduh berkas PDF berukuran besar sebagai byte stream bertahap, mencegah error *Out of Memory (OOM)* pada HP.
  * **Penyimpanan Lokal MediaStore API:** Menggunakan utility `PdfGenerator` berbasis Android `MediaStore` untuk mencetak dan menulis file PDF secara lokal ke direktori aman `/Downloads/MannaWaSalwa/`.

### 4. Notifikasi FCM & Background Service
* **Notifikasi Instan Real-time:**
  * Integrasi Firebase Cloud Messaging (FCM) untuk memicu notifikasi push saat ada status transaksi berubah (misal: booking terkonfirmasi, bukti bayar ditolak, rekam medis diisi).
* **Navigasi Pintas Notifikasi (Deep Linking):**
  * Ketika pengguna mengetuk notifikasi sistem, aplikasi akan otomatis terbuka dan melakukan pengalihan rute (intent redirect) langsung ke halaman detail transaksi yang bersangkutan.

---

## 🛠️ Tech Stack
* **UI Framework:** Jetpack Compose (Kotlin)
* **Design System:** Material Design 3
* **Network Client:** Retrofit 2 & OkHttp3 (dengan Logging Interceptor & Streaming byte downloads)
* **Asynchronous Flow:** Kotlin Coroutines & SharedFlow / StateFlow
* **Real-time Sync:** Pusher Java Client & Pusher Websocket Channels
* **Notifikasi:** Firebase Cloud Messaging (FCM) & Google Client Library
* **Pagination:** Android Jetpack Paging 3

---

## ⚙️ Setup & Langkah Menjalankan Lokal

### 1. Prasyarat Lingkungan
* **Android Studio** (Versi Ladybug 2024.2.1 atau lebih baru)
* **Java Development Kit (JDK):** JDK 17
* **HP Android / Emulator:** Target API Level 26 (Android 8.0) ke atas.

### 2. Hubungkan ke Server API Backend (PENTING)
Aplikasi Android ini memerlukan server backend lokal yang aktif. 
1. Pastikan komputer backend Anda dan HP/Emulator berada dalam **satu jaringan Wi-Fi lokal yang sama**.
2. Cari tahu alamat IP lokal PC/Laptop Anda (misal `192.168.1.5`).
3. Buka proyek Android ini di Android Studio.
4. Buka file [app/build.gradle.kts](file:///g:/Coding/Rumah%20Sehat%20Manna%20wa%20Salwa/Android/rumahsehatmannawasalwa/app/build.gradle.kts#L24).
5. Ubah baris konfigurasi `BASE_URL` sesuai dengan IP PC/Laptop Anda:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://<IP_KOMPUTER_ANDA>:8000/api/\"")
   ```
6. Sinkronkan Gradle (*Sync Project with Gradle Files*).

### 3. Build & Jalankan Aplikasi
* Ketuk tombol **Run 'app'** di Android Studio untuk memasang dan menjalankan aplikasi ke HP Android / Emulator Anda secara langsung.
* Atau, jika ingin membuat file installer APK secara mandiri, jalankan perintah di terminal Android Studio:
  ```bash
  .\gradlew.bat assembleDebug
  ```
  File APK akan ter-generate di folder: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔗 Link Repositori Terkait
* **Laravel Backend API Server:** [rumah-sehat-manna-wa-salwa-back-end](https://github.com/aab-dii/rumah-sehat-manna-wa-salwa-back-end)
* **Unduh Rilis Installer APK:** Buka tab **[Releases](https://github.com/aab-dii/rumah-sehat-manna-wa-salwa/releases)** di sebelah kanan repositori ini untuk mengunduh berkas APK siap pakai (`v1.0.0-prototype`).
