package com.plural_pinelabs.expresscheckoutsdk.common

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

 /**
  * Utility object for checking if a device is rooted.
  */
 internal object RootUtil {
         fun isDeviceIsRooted(): Boolean {
            return (checkRootedFiles() || checkRootedProcesses() || checkTagsAndKeys())
        }

        private fun checkRootedFiles(): Boolean {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            for (path in paths) {
                if (File(path).exists()) return true
            }
            return false
        }

        private fun checkRootedProcesses(): Boolean {
            var process: Process? = null
            return try {
                process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
                val `in` = BufferedReader(InputStreamReader(process.inputStream))
                `in`.readLine() != null
            } catch (t: Throwable) {
                false
            } finally {
                process?.destroy()
            }
        }

        private fun checkTagsAndKeys(): Boolean {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }
}