# Rumah Sehat Manna wa Salwa — Aplikasi Android (Client App)

[![Status](https://img.shields.io/badge/Status-Prototype%20%E2%80%94%20Local%20Only-orange)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Repositori ini berisi kode sumber aplikasi mobile client untuk sistem manajemen dan booking janji temu klinik **Rumah Sehat Manna wa Salwa**. Aplikasi ini dibuat menggunakan bahasa pemrograman Kotlin dengan arsitektur UI modern Jetpack Compose.

---

## 📱 Tentang Aplikasi
Aplikasi **Rumah Sehat Manna wa Salwa** dirancang untuk memudahkan pasien melakukan reservasi terapi secara mandiri, memudahkan terapis mengelola agenda praktik & menulis rekam medis, serta memfasilitasi admin dalam memverifikasi transaksi dan menyusun laporan operasional klinik secara terintegrasi.

## ✨ Fitur Utama

### 👤 Pasien
- Registrasi & login akun (Email/Password atau Google Sign-In)
- Booking terapi mandiri — pilih layanan, terapis, tanggal, dan jam
- Dua metode pembayaran: **Tunai** atau **Transfer Bank** (upload foto bukti bayar)
- Countdown timer 24 jam untuk batas waktu upload bukti transfer
- Nomor antrean hari ini secara real-time (per terapis)
- Lihat riwayat rekam medis & catatan diagnosa dari terapis secara kronologis

### 👨‍⚕️ Terapis
- Dashboard ringkasan total sesi bulan ini & daftar agenda hari ini
- Mulai sesi terapi & isi formulir rekam medis pasien (keluhan, diagnosa, tindakan)
- Isi catatan rekam medis susulan untuk sesi yang diselesaikan paksa oleh admin
- Kelola jadwal mingguan praktik & tandai hari libur/cuti
- Tutup Darurat (*Emergency Close*) — batalkan seluruh antrean hari ini sekarang
- Lihat laporan kunjungan & performa bulanan pribadi
- Hubungi pasien via WhatsApp langsung dari detail booking

### 🛡️ Admin / Super Admin
- Booking terapi langsung atas nama pasien (otomatis terkonfirmasi)
- Verifikasi atau tolak bukti transfer bank pasien (disertai catatan alasan penolakan)
- Force Complete sesi yang terlewat ditutup oleh terapis
- Kelola katalog layanan klinik (tambah, edit, nonaktifkan)
- Kelola daftar pengguna — tambah akun baru, nonaktifkan, atau restore akun dari trash
- Ekspor laporan PDF bulanan: Keuangan, Kunjungan Terapis, Kinerja, & Kegiatan Klinik
- Laporan komparatif performa antar terapis *(khusus Super Admin)*

---

## 🛠️ Tech Stack
* **Bahasa Pemrograman:** Kotlin
* **UI Framework:** Jetpack Compose (Modern Declarative UI)
* **Desain & Styling:** Material Design 3 (M3) dengan kustomisasi tema dinamis dan komponen modular
* **Manajemen Status (State Management):** ViewModel, StateFlow, LiveData
* **Injeksi Dependensi (Dependency Injection):** Dagger Hilt (`hilt-navigation-compose`)
* **Asynchronous & Threading:** Kotlin Coroutines & Flow (dengan penanganan pembatalan Job otomatis untuk mencegah race condition)
* **Jaringan (Networking):** Retrofit 2 & OkHttp 3 (disertai `logging-interceptor`)
* **Pemuatan Gambar:** Coil Compose (Image Loader)
* **Notifikasi & Integrasi Cloud:**
  * Firebase Cloud Messaging (FCM) untuk push notification real-time
  * Firebase Authentication (untuk integrasi Google Sign-In)
* **Utilitas Tambahan:**
  * Android-Image-Cropper (untuk memotong foto bukti transfer/profil)
  * Paging 3 (`paging-runtime-ktx` & `paging-compose` untuk pagination data)
  * Implicit Intent untuk membuka aplikasi WhatsApp pihak ketiga secara langsung

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
