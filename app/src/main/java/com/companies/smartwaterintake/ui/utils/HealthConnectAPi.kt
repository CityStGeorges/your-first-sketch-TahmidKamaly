package com.companies.smartwaterintake.ui.utils

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

object HealthConnectUtils {

    private var healthConnectClient: HealthConnectClient? = null
    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
    )

    fun checkForHealthConnectInstalled(context: Context): Int {

        val availabilityStatus =

            HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata")

        when (availabilityStatus) {

            HealthConnectClient.SDK_UNAVAILABLE -> {

                // The Health Connect SDK is unavailable on this device at the time.

                // This can be due to the device running a lower than required Android Version.

                // Apps should hide any integration points to Health Connect in this case.

            }

            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {

                // The Health Connect SDK APIs are currently unavailable, the provider is either not installed

                // or needs to be updated. You may choose to redirect to package installers to find a suitable APK.

            }

            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(context)

            }

        }

        return availabilityStatus

    }

    suspend fun checkPermissions(): Boolean {

        val granted = healthConnectClient?.permissionController?.getGrantedPermissions()



        if (granted != null) {

            return granted.containsAll(PERMISSIONS)

        }

        return false

    }

    suspend fun readStepsByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): Int {
        return try {
            val response = healthConnectClient?.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            var totalSteps = 0
            response?.records?.forEach { stepRecord ->
                totalSteps += stepRecord.count.toInt()
            }
            totalSteps
            Log.d("HealthConnect", "Reading steps from $startTime to $endTime")
        } catch (e: Exception) {
            Log.e("HealthConnect", "Failed to read steps: ${e.message}")
            0
        }
    }
}