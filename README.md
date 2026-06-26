# Rumah Sehat Manna wa Salwa — Aplikasi Android (Client App)

[![Status](https://img.shields.io/badge/Status-Prototype%20%E2%80%94%20Local%20Only-orange)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Repositori ini berisi kode sumber aplikasi mobile client untuk sistem manajemen dan booking janji temu klinik **Rumah Sehat Manna wa Salwa**. Aplikasi ini dibuat menggunakan bahasa pemrograman Kotlin dengan arsitektur UI modern Jetpack Compose.

---

## 📱 Tentang Aplikasi
Aplikasi **Rumah Sehat Manna wa Salwa** dirancang untuk memudahkan pasien melakukan reservasi terapi secara mandiri, memudahkan terapis mengelola agenda praktik & menulis rekam medis, serta memfasilitasi admin dalam memverifikasi transaksi dan menyusun laporan operasional klinik secara terintegrasi.

---

## 👥 Peran Pengguna & Fitur Utama

### 1. Pasien (Patient Role)
* **Katalog Layanan:** Memilih jenis terapi yang aktif (Bekam, Akupunktur, Ramuan).
* **Reservasi Jadwal (Booking):** Memilih terapis, tanggal, serta jam slot terapi yang kosong (slot waktu dihitung dinamis dari jadwal aktif terapis).
* **Metode Pembayaran:** Mendukung pembayaran Tunai (Cash) dan Transfer Bank (dengan pengunggahan foto bukti transfer).
* **Real-time Transfer Countdown:** Batas transfer 24 jam dengan timer hitung mundur interaktif untuk menghindari pembatalan otomatis.
* **Antrean Dinamis:** Pasien mendapatkan nomor antrean hari berjalan per terapis secara dinamis berdasarkan jam booking (dan *tie-breaker created_at*).
* **Rekam Terapi:** Melihat riwayat diagnosa dan tindakan terapis secara transparan setelah sesi diselesaikan.

### 2. Terapis (Therapist Role)
* **Dashboard Statistik:** Menampilkan total sesi bulanan, sesi terjadwal, dan sesi berstatus "Belum Isi Catatan" (Force Completed oleh Admin).
* **Agenda Hari Ini:** Daftar antrean pasien yang akan dilayani pada hari berjalan.
* **Mulai Sesi:** Mengubah status janji temu menjadi "Sedang Berlangsung".
* **Isi Rekam Medis:** Formulir pengisian diagnosa keluhan, tindakan yang diambil, dan catatan tambahan terapis untuk merampungkan janji temu menjadi "Selesai".

### 3. Admin / Super Admin (Management Role)
* **Verifikasi Transaksi:** Memeriksa berkas bukti transfer pasien untuk menyetujui atau menolak (disertai pengisian alasan penolakan).
* **Force Complete:** Menyelesaikan sesi janji temu yang terlewat ditutup oleh terapis agar pembayaran & status laporan terekam rapi.
* **Ekspor Laporan PDF:** Menghasilkan laporan berformat PDF standar A4 Landscape:
  1. *Laporan Keuangan* (Pendapatan riil tunai/transfer, refund).
  2. *Laporan Kunjungan Terapis* (Hardcoded detail alamat klinik, penutupan STPT, total pasien L/P).
  3. *Laporan Kinerja Terapis* (Sesi & pendapatan terapis).
  4. *Laporan Kegiatan Klinik* (Layanan terpopuler, grafik sesi).
  5. *Laporan Komparatif Terapis* (Khusus Super Admin - visualisasi grafik kontribusi sesi terapis).

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
