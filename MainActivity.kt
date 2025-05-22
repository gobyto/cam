
package com.xdd.cam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var serverIpEditText: EditText
    private lateinit var scanButton: Button
    private lateinit var cameraButton: Button
    private lateinit var client: OkHttpClient
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverIpEditText = findViewById(R.id.serverIpEditText)
        scanButton = findViewById(R.id.scanButton)
        cameraButton = findViewById(R.id.cameraButton)
        client = OkHttpClient()

        scanButton.setOnClickListener {
            startActivityForResult(
                Intent(this, QRScanActivity::class.java),
                REQUEST_QR_SCAN
            )
        }

        cameraButton.setOnClickListener {
            startActivityForResult(
                Intent(this, CameraActivity::class.java),
                REQUEST_CAMERA
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_QR_SCAN -> {
                if (resultCode == RESULT_OK) {
                    val qrContent = data?.getStringExtra("qr_content") ?: ""
                    uploadQRCode(qrContent)
                }
            }
            REQUEST_CAMERA -> {
                if (resultCode == RESULT_OK) {
                    uploadPhoto(currentPhotoPath)
                }
            }
        }
    }

    private fun uploadQRCode(content: String) {
        val serverIp = serverIpEditText.text.toString()
        val requestBody = FormBody.Builder()
            .add("qr_content", content)
            .build()

        val request = Request.Builder()
            .url("http://$serverIp/api/qrcode")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "上传失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "上传成功", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun uploadPhoto(photoPath: String) {
        val file = File(photoPath)
        if (!file.exists()) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "文件不存在", Toast.LENGTH_SHORT).show()
            }
            return
        }
        val serverIp = serverIpEditText.text.toString()
        val file = File(photoPath)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("http://$serverIp/api/campic")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "上传失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "上传成功", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    companion object {
        const val REQUEST_QR_SCAN = 1001
        const val REQUEST_CAMERA = 1002
    }
}
