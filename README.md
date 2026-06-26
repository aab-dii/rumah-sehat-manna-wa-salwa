# Rumah Sehat Manna wa Salwa — Aplikasi Android (Client App)

[![Status](https://img.shields.io/badge/Status-Prototype%20%E2%80%94%20Local%20Only-orange)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Repositori ini berisi kode sumber aplikasi mobile client untuk sistem manajemen dan booking janji temu klinik **Rumah Sehat Manna wa Salwa**. Aplikasi ini dibuat menggunakan bahasa pemrograman Kotlin dengan arsitektur UI modern Jetpack Compose.

---

## 📱 Tentang Aplikasi
Aplikasi **Rumah Sehat Manna wa Salwa** dirancang untuk memudahkan pasien melakukan reservasi terapi secara mandiri, memudahkan terapis mengelola agenda praktik & menulis rekam medis, serta memfasilitasi admin dalam memverifikasi transaksi dan menyusun laporan operasional klinik secara terintegrasi.

## 🚀 Fitur Utama Aplikasi (Berdasarkan Modul)

Aplikasi mobile **Rumah Sehat Manna wa Salwa** mengimplementasikan seluruh fitur utama yang terbagi ke dalam 5 modul fungsional sesuai dengan dokumentasi resmi proyek:

### Modul 1: Autentikasi & Manajemen Akun (M-01)
* **Registrasi Akun Mandiri:** Form pendaftaran pasien baru secara daring dengan validasi data nomor handphone, format email, dan batas keamanan sandi (8-64 karakter sesuai standar OWASP).
* **Login Multi-Metode:** Dukungan login konvensional (Email & Password) serta login sekali ketuk menggunakan **Google Sign-In** terintegrasi Firebase Auth.
* **Penyimpanan Sesi Aman:** Fitur masuk otomatis (*auto-login*) yang menjaga status autentikasi aktif menggunakan penyimpanan data lokal terenkripsi via `UserPreference`.
* **Manajemen Profil:** Pengguna dapat memperbarui foto profil (disertai fitur potong gambar menggunakan *Android-Image-Cropper*), mengubah informasi kontak (No. HP hanya menerima input angka), alamat, pekerjaan, serta memperbarui kata sandi secara aman.
* **Fokus Otomatis Form (Android):** Sistem navigasi input pintar yang memindahkan fokus kursor secara otomatis ke baris input pertama yang kosong/mengalami error validasi.
* **Manajemen Pengguna (Admin & Super Admin Screen):** 
  * Layar `AdminManageUsersScreen` untuk melihat daftar seluruh pengguna secara alfabetis (A-Z).
  * Membuat akun Pasien dan Terapis baru secara manual.
  * Tab visual data aktif dan data terhapus (*Trash* / *Soft Deleted*) beserta tombol *Restore* untuk memulihkan akun.

### Modul 2: Reservasi Terapi & Pengelolaan Transaksi (M-02)
* **Booking Layanan Terapi (Oleh Pasien):** Pemilihan layanan terapi medis beserta terapis yang diinginkan melalui kalender slot waktu operasional terapis yang diperbarui secara dinamis.
* **Pemisahan Alur Pembayaran:**
  * **Metode Tunai (Cash):** Booking langsung dibuat dengan status pembayaran awal *unpaid*, dan janji temu divalidasi oleh admin untuk dijadwalkan (lunas saat pasien hadir di klinik).
  * **Metode Transfer Bank:** Sistem memberikan batas waktu transfer (24 jam dengan countdown timer interaktif). Pasien wajib mengunggah foto bukti transfer bank melalui aplikasi.
* **Verifikasi Pembayaran & Antrean Dinamis:**
  * Halaman admin untuk memvalidasi bukti pembayaran transfer (terima untuk mengubah status transaksi menjadi *paid*, booking terkonfirmasi (*confirmed*), atau tolak dengan mencantumkan alasan penolakan).
  * Komponen visual `QueueInfoCard` premium (berwarna *GreenPrimary* dan *GreenSoft*) untuk menampilkan nomor urut antrean harian pasien secara real-time.
* **Pencegahan Bentrok Jadwal (Double Booking Prevention):** Validasi ketat di sisi UI untuk mencegah pemesanan ganda di waktu yang sama bagi pasien dan terapis.
* **Pengelolaan Operasional (Terapis):**
  * Terapis dapat mengatur jadwal mingguan rutin mereka sendiri langsung dari aplikasi.
  * Fitur penandaan Hari Libur/Cuti Terapis untuk mengunci tanggal tertentu di kalender pasien.
  * Prosedur **Emergency Close (Tutup Darurat)** bagi terapis untuk membatalkan seluruh antrean aktif hari ini dan mengunci sisa slot secara instan saat kondisi mendesak.
* **Integrasi Komunikasi WhatsApp:** Akses instan menghubungi admin klinik (untuk pasien) atau pasien (untuk terapis) dengan tombol bantu mengambang (FAB) yang menggunakan *Implicit Intent* untuk otomatis menyusun draf pesan teks detail janji temu ke aplikasi WhatsApp.

### Modul 3: Rekam Medis Digital (M-03)
* **Pencatatan Klinis Terapis:** Terapis mengisi keluhan pasien, diagnosis, tindakan terapi yang diberikan (bekam/akupunktur/ramuan), dan catatan tambahan medis saat menyelesaikan layanan terapi. Pengisian catatan ini secara otomatis mengubah status janji temu menjadi *completed* (selesai).
* **Catatan Medis Susulan:** Memungkinkan pengisian catatan medis untuk janji temu yang ditutup sepihak oleh admin (*force completed*). Janji temu ini tetap muncul pada riwayat terapis dan dilengkapi tombol **"Isi Catatan Terapi"** agar terapis dapat menginput rekam medis susulan.
* **Riwayat Rekam Medis Pasien:** Halaman riwayat medis kronologis (*Timeline UI*) yang menyajikan riwayat pengobatan dan detail rekam medis dari kunjungan-kunjungan sebelumnya secara lengkap.
* **Filter Riwayat Medis:** Penyaringan catatan medis lama berdasarkan jenis layanan terapi yang pernah diambil.

### Modul 4: Notifikasi & Komunikasi Real-time (M-04)
* **Push Notification Firebase:** Notifikasi lokal instan di handphone pengguna untuk status pembayaran dikonfirmasi/ditolak, pembatalan janji temu, dan pembaruan status layanan terapis.
* **Deep-Link Navigation:** Menekan notifikasi di ponsel akan langsung mengarahkan pengguna secara presisi ke halaman detail janji temu yang bersangkutan di aplikasi Android berdasarkan peran pengguna (Pasien atau Terapis).

### Modul 5: Modul Laporan & Analitik Keuangan (M-05)
* **Laporan Keuangan & Tren Kunjungan:** Tampilan grafik kontribusi persentase pendapatan dan kemajuan bulanan di halaman `ReportScreen`.
* **Analisis Kinerja Terapis:** Metrik penilai produktivitas (jumlah sesi pelayanan) bagi masing-masing terapis.
* **Ekspor Laporan PDF:** 
  * Konfigurasi Retrofit `@Streaming` untuk mengunduh berkas laporan PDF berukuran besar dari server sebagai byte stream bertahap, mencegah error *Out of Memory (OOM)* pada HP.
  * Integrasi utility `PdfGenerator` berbasis Android `MediaStore` API untuk menulis berkas PDF laporan secara lokal ke direktori aman `/Downloads/MannaWaSalwa/`.

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
