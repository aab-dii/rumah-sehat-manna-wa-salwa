# Rumah Sehat Manna wa Salwa — Proses Bisnis & Integrasi Sistem

Dokumen ini berisi pemetaan alur kerja (End-to-End) sistem Rumah Sehat Manna wa Salwa, mulai dari aplikasi Mobile (Android Kotlin) hingga Backend (Laravel API). Dokumentasi ini disusun untuk mempermudah pemahaman logika program dalam tesis.

---

## 1. Autentikasi dan Manajemen Sesi
**Alur:** Validasi Firebase → Sinkronisasi UID ke Backend → Pemberian Token Sanctum.

### Mobile (AuthRepository.kt)
```kotlin
suspend fun login(email: String, pass: String): ApiResult<User> {
    val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
    val idToken = authResult.user?.getIdToken(false)?.await()?.token
    return apiService.syncFirebase(mapOf("id_token" to idToken))
}
```

### API Interface (ApiService.kt)
```kotlin
@POST("user/sync-firebase")
suspend fun syncFirebase(@Body request: Map<String, String>): Response<UserResponse>
```

### Backend (AuthController.php)
```php
public function syncFirebase(Request $request) {
    $verifiedIdToken = $this->firebaseAuth->verifyIdToken($request->id_token);
    $uid = $verifiedIdToken->claims()->get('sub');
    $user = User::where('firebase_uid', $uid)->firstOrFail();
    $token = $user->createToken('authToken')->plainTextToken;
    return ResponseFormatter::success(['user' => $user, 'access_token' => $token]);
}
```

---

## 2. Manajemen Layanan (Master Data)
**Alur:** Pengambilan daftar layanan untuk ditampilkan di katalog pasien.

### Mobile (ServiceRepository.kt)
```kotlin
suspend fun getServiceList(): ApiResult<List<Service>> {
    val response = apiService.getServices(page = 1, limit = 100)
    return ApiResult.Success(response.body()!!.data.data)
}
```

### API Interface (ApiService.kt)
```kotlin
@GET("services")
suspend fun getServices(@Query("page") page: Int): Response<ServiceResponse>
```

### Backend (ServiceController.php)
```php
public function all(Request $request) {
    $service = Service::query();
    return ResponseFormatter::success($service->paginate($request->limit));
}
```

---

## 3. Pengelolaan Jadwal Terapis
**Alur:** Admin/Terapis mengatur jam operasional rutin.

### Mobile (AppointmentRepository.kt)
```kotlin
suspend fun getTherapistSchedule(id: Int): ApiResult<ProcessedSchedule> {
    val response = apiService.getSchedules(id)
    return ApiResult.Success(ScheduleMapper.map(response.body()!!.data))
}
```

### API Interface (ApiService.kt)
```kotlin
@GET("schedules/{therapistId}")
suspend fun getSchedules(@Path("therapistId") id: Int): Response<ApiResponse<List<Schedule>>>
```

### Backend (ScheduleController.php)
```php
public function updateSchedule(Request $request) {
    $schedule = Schedule::updateOrCreate(
        ['therapist_id' => $request->therapist_id, 'day' => $request->day],
        ['start_time' => $request->start_time, 'end_time' => $request->end_time, 'is_active' => $request->is_active]
    );
    return ResponseFormatter::success($schedule);
}
```

---

## 4. Proses Transaksi Janji Temu (Booking)
**Alur:** Pemilihan jadwal → Upload bukti transfer → Validasi Admin.

### Mobile (AppointmentRepository.kt)
```kotlin
suspend fun createAppointment(params: CreateAppointment): ApiResult<Int> {
    val requestMap = buildRequestMap(params)
    val imagePart = prepareImage(params.proofUri)
    return apiService.createAppointment(requestMap, imagePart)
}
```

### API Interface (ApiService.kt)
```kotlin
@Multipart @POST("bookings")
suspend fun createAppointment(@PartMap d: Map<String, RequestBody>, @Part p: MultipartBody.Part?): Response<BookingCreateResponse>
```

### Backend (BookingController.php)
```php
public function store(StoreBookingRequest $request) {
    return DB::transaction(function () use ($request) {
        $booking = Booking::create($request->all());
        $proofPath = $request->file('proof_of_transfer')->store('proofs', 'public');
        Transaction::create(['booking_id' => $booking->id, 'proof_of_transfer' => $proofPath]);
        return ResponseFormatter::success($booking);
    });
}
```

---

## 5. Monitoring Janji Temu (List & Detail)
**Alur:** Melihat riwayat janji temu aktif dan detail status pembayaran.

### Mobile (AppointmentRepository.kt)
```kotlin
fun getBookings(page: Int, status: String?): Flow<ApiResult<List<BookingListItem>>> = flow {
    val response = apiService.getBookings(page = page, status = status)
    emit(ApiResult.Success(response.data.data))
}

fun fetchAppointmentDetail(id: Int) = flow {
    val response = apiService.getBookingDetail(id)
    emit(ApiResult.Success(response))
}
```

### API Interface (ApiService.kt)
```kotlin
@GET("bookings")
suspend fun getBookings(@Query("page") p: Int, @Query("status") s: String?): BookingListResponse

@GET("bookings/{id}")
suspend fun getBookingDetail(@Path("id") id: Int): DetailAppointmentResponse
```

### Backend (BookingController.php)
```php
public function all(Request $request) {
    $user = Auth::user();
    $booking = Booking::with(['service', 'patient', 'transaction']);
    if ($user->role === 'pasien') $booking->where('patient_id', $user->id);
    return ResponseFormatter::success($booking->paginate($request->limit));
}

public function show($id) {
    $booking = Booking::with(['patient', 'therapist', 'service', 'transaction']).find($id);
    return ResponseFormatter::success($booking);
}
```

---

## 6. Pencatatan dan Riwayat Rekam Medis
**Alur:** Input hasil terapi → Akses riwayat kesehatan pasien.

### Mobile (TherapyRecordRepository.kt)
```kotlin
fun createTherapyRecord(req: TherapyRecordRequest) = flow {
    val response = apiService.createTherapyRecord(req)
    emit(ApiResult.Success(response.body()!!.data))
}

fun getTherapyRecords(patientId: Int?) = Pager(config = PagingConfig(10)) {
    TherapyRecordPagingSource(apiService, patientId)
}.flow
```

### API Interface (ApiService.kt)
```kotlin
@POST("therapy-records")
suspend fun createTherapyRecord(@Body r: TherapyRecordRequest): Response<TherapyRecordDetailResponse>

@GET("therapy-records")
suspend fun getTherapyRecords(@Query("patient_id") id: Int?, @Query("page") p: Int): TherapyRecordListResponse
```

### Backend (TherapyRecordController.php)
```php
public function all(Request $request) {
    $query = TherapyRecord::with(['patient', 'therapist', 'booking.service']);
    if (Auth::user()->role === 'pasien') $query->where('patient_id', Auth::id());
    return ResponseFormatter::success($query->paginate($request->limit));
}
```

---

## 7. Integrasi Notifikasi (FCM)
**Alur:** Device Token registration → Event-driven notification.

### Mobile (AuthRepository.kt)
```kotlin
suspend fun updateFcmToken(token: String) {
    apiService.updateFcmToken(UpdateFcmTokenRequest(token))
}
```

### Backend (Listener & Service)
```php
// SendFcmNotification.php
public function handle(BookingStatusUpdated $event) {
    if ($event->booking->status === 'confirmed') {
        FcmService::send($event->booking->patient->fcm_token, "Janji Temu Dikonfirmasi! ✅", "Jadwal Anda telah disetujui.");
    }
}
```
