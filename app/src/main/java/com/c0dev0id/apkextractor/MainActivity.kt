package com.c0dev0id.apkextractor

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val apps = getInstalledUserApps()
        recyclerView.adapter = AppAdapter(apps, packageManager) { app ->
            shareApk(app)
        }
    }

    private fun getInstalledUserApps(): List<AppInfo> {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages
            .filter { appInfo ->
                appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }
            .map { appInfo ->
                AppInfo(
                    name = pm.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName,
                    sourceDir = appInfo.sourceDir
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    private fun shareApk(app: AppInfo) {
        val apkFile = File(app.sourceDir)
        if (!apkFile.exists()) {
            Toast.makeText(this, getString(R.string.error_apk_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val apkUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            apkFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_SUBJECT, "${app.name}.apk")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_apk_title)))
    }
}
