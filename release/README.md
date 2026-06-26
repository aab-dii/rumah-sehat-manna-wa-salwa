# Rilis Aplikasi Android — Rumah Sehat Manna wa Salwa

Selamat datang di repositori rilis aplikasi mobile **Rumah Sehat Manna wa Salwa**. Halaman ini memuat panduan instalasi, konfigurasi jaringan, serta akun uji coba untuk mempermudah jalannya presentasi sidang tesis atau pengujian aplikasi.

---

## 📱 Unduh Aplikasi
File installer APK dapat langsung diunduh melalui repositori ini:
* **File APK:** [rumah-sehat-manna-wa-salwa.apk](rumah-sehat-manna-wa-salwa.apk)
* **Versi Rilis:** v1.0.0-prototype
* **Target Android SDK:** Android 8.0 (API Level 26) ke atas

---

## 🛠️ Langkah Instalasi di Handphone
1. Unduh file **[rumah-sehat-manna-wa-salwa.apk](rumah-sehat-manna-wa-salwa.apk)** ke penyimpanan internal handphone Android Anda.
2. Buka file manager di HP, lalu ketuk file APK tersebut untuk memulai instalasi.
3. Jika muncul peringatan keamanan sistem, masuk ke **Pengaturan** HP Anda lalu aktifkan opsi **"Izinkan Instalasi dari Sumber Tidak Dikenal"** (*Allow installation from Unknown Sources*).
4. Selesaikan proses instalasi hingga aplikasi berhasil terpasang di HP Anda.

---

## 🔗 Konfigurasi Jaringan & Koneksi Backend (PENTING untuk Sidang)
Aplikasi mobile ini berkomunikasi dengan Laravel Backend API menggunakan jaringan lokal nirkabel (Wi-Fi). 

### 1. Spesifikasi Koneksi Saat Ini (Hardcoded)
Installer APK ini di-build dengan konfigurasi alamat server lokal berikut:
* **API BASE URL:** `http://192.168.1.5:8000/api/`
* **Admin WhatsApp Contact:** `+6285220264022`

### 2. Prasyarat agar Aplikasi Berhasil Terhubung ke Backend:
1. **Satu Jaringan Wi-Fi:** Pastikan Handphone Android Anda dan PC/Laptop yang menjalankan Laravel backend terhubung ke **jaringan Wi-Fi yang sama**.
2. **Kesesuaian IP PC:** Pastikan PC/Laptop Anda memiliki IP Address **`192.168.1.5`**.
3. **Menjalankan Server Backend:** Pastikan backend dijalankan dengan opsi binding ke semua interface (`0.0.0.0`):
   ```bash
   php artisan serve --host=0.0.0.0 --port=8000
   ```
4. **Firebase & Pusher:** Pastikan koneksi internet aktif pada jaringan Wi-Fi tersebut agar push notification FCM dan real-time event Pusher berjalan lancar.

> [!NOTE]
> Jika IP PC/Laptop Anda berubah (misal dari `192.168.1.5` ke IP lainnya), Anda harus merubah nilai `BASE_URL` di file [build.gradle.kts](file:///g:/Coding/Rumah%20Sehat%20Manna%20wa%20Salwa/Android/rumahsehatmannawasalwa/app/build.gradle.kts#L24) kemudian melakukan build ulang menggunakan perintah:
> ```bash
> .\gradlew.bat assembleDebug
> ```

---

## 👥 Akun Uji Coba (Testing Accounts)
Berikut daftar akun yang dapat digunakan untuk demonstrasi aplikasi berdasarkan role masing-masing:

### 1. Pasien (Patient App)
* **Email:** `pasien@gmail.com`
* **Password:** `password`
* *Fungsi:* Melakukan pemesanan janji temu (booking), membayar via cash/transfer, mengunggah bukti bayar, dan melihat catatan rekam medis pribadi.

### 2. Terapis (Therapist App)
* **Email:** `terapis@gmail.com`
* **Password:** `password`
* *Fungsi:* Mengelola jadwal pelayanan, melihat agenda hari ini, memulai sesi terapi, serta menulis rekam medis pasien (*Therapy Notes*).

### 3. Admin / Super Admin
* **Email:** `admin@gmail.com` (atau `superadmin@gmail.com`)
* **Password:** `password`
* *Fungsi:* Melakukan verifikasi pembayaran transfer, melakukan tindakan paksa selesai (*force completed*) pada janji temu, mengelola data master layanan, dan melihat laporan grafik performa & keuangan klinik.

---

## 📂 Struktur Rilis Direktori
```
/release
  ├── README.md                          <-- (Dokumen panduan rilis ini)
  └── rumah-sehat-manna-wa-salwa.apk     <-- (File installer aplikasi Android)
```
