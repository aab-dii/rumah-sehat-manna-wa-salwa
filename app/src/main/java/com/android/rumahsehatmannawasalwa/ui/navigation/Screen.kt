package com.android.rumahsehatmannawasalwa.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen (val route: String) {


    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object Dispatch : Screen("dispatch")
    object Splash : Screen("splash")

    // Patient
    object PatientHome : Screen("patient_home")
    object PatientAppointment : Screen("patient_appointment")
    object CreateBooking : Screen("create_booking")
    object MyBooking : Screen("patient_booking_history")
    object Booking : Screen("booking/{sId}/{sName}/{sPrice}") {
        val arguments = listOf(
            navArgument("sId") { type = NavType.StringType },
            navArgument("sName") { type = NavType.StringType },
            navArgument("sPrice") { type = NavType.IntType }
        )
        fun createRoute(sId: String, sName: String, sPrice: Int) = "booking/$sId/$sName/$sPrice"
    }
    object Record : Screen("patient_record")
    object BookingSummary : Screen("booking_summary")
    object PatientAppointmentDetail : Screen("patient_appointment_detail/{bookingId}") {
        val arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        fun createRoute(id: Int) = "patient_appointment_detail/$id"
    }
    object PatientProfile : Screen("patient_profile")
    object Notifications : Screen("notifications")
    object ProfileDetail : Screen("profile_detail")
    object CompleteProfile : Screen("complete_profile/{isFromAuth}") {
        val arguments = listOf(navArgument("isFromAuth") { type = NavType.BoolType })
        fun createRoute(isFromAuth: Boolean) = "complete_profile/$isFromAuth"
    }
    object History : Screen("history")

    // Admin
    object AdminHome : Screen("admin_home")
    object AdminAppointment : Screen("admin_appointment")
    object AdminVerification : Screen("admin_verification")
    object AdminUserDetail : Screen("admin_user_detail/{userId}") {
        val arguments = listOf(navArgument("userId") { type = NavType.IntType })
        fun createRoute(id: Int) = "admin_user_detail/$id"
    }
    object AdminManageUser : Screen("admin_manage_user")
    object AdminManageService : Screen("admin_manage_service")
    object AdminAddUser : Screen("admin_add_user")
    object AdminAppointmentDetail : Screen("admin_booking_detail/{bookingId}") {
        val arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        fun createRoute(id: Int) = "admin_booking_detail/$id"
    }
    object AdminEditUser : Screen("admin_edit_user/{userId}") {
        val arguments = listOf(navArgument("userId") { type = NavType.IntType })
        fun createRoute(id: Int) = "admin_edit_user/$id"
    }
    object AdminAddService : Screen("admin_add_service")
    object AdminServiceDetail : Screen("admin_service_detail/{serviceId}") {
        val arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        fun createRoute(id: Int) = "admin_service_detail/$id"
    }
    object AdminEditService : Screen("admin_edit_service/{serviceId}") {
        val arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        fun createRoute(id: Int) = "admin_edit_service/$id"
    }
    object AdminTherapistList : Screen("admin_therapist_list")
    object AdminPatientList : Screen("admin_patient_list")
    object AdminTherapistSchedule : Screen("admin_therapist_schedule/{therapistId}") {
        val arguments = listOf(navArgument("therapistId") { type = NavType.IntType })
        fun createRoute(id: Int) = "admin_therapist_schedule/$id"
    }

    // Therapist
    object TherapistHome : Screen("therapist_home")
    object TherapistAppointment : Screen("therapist_appointment")
    object TherapistPatientList : Screen("therapist_patient_list") // Daftar Pasien (akses riwayat terapi)
    object TherapistHistory : Screen("therapist_history") // Riwayat Terapi
    object TherapistSchedule : Screen("therapist_schedule") // Kelola Jadwal (Availability)
    object TherapyRecordForm : Screen("therapy_record_form/{bookingId}?record_id={recordId}") {
        val arguments = listOf(
            navArgument("bookingId") { type = NavType.IntType },
            navArgument("recordId") { 
                type = NavType.IntType
                defaultValue = 0
            }
        )
        fun createRoute(bId: Int, rId: Int = 0) = "therapy_record_form/$bId?record_id=$rId"
    }
    object TherapyRecordDetail : Screen("therapy_record_detail/{recordId}") {
        val arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        fun createRoute(id: Int) = "therapy_record_detail/$id"
    }
    object TherapistProfile : Screen("therapist_profile")
    object PatientHistory : Screen("patient_history/{patientId}") {
        val arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        fun createRoute(id: Int) = "patient_history/$id"
    }

    // Sprint 2.1: Super Admin
    object SuperAdminManageAdmins : Screen("super_admin_manage_admins")
    object SuperAdminAddAdmin : Screen("super_admin_add_admin")
    object SuperAdminResetPassword : Screen("super_admin_reset_password/{userId}") {
        val arguments = listOf(navArgument("userId") { type = NavType.IntType })
        fun createRoute(id: Int) = "super_admin_reset_password/$id"
    }

    object Offline : Screen ("offline")
}