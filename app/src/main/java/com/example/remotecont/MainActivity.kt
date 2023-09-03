package com.example.remotecont

import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.activity.ComponentActivity
import com.example.remotecont.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.remotecont.ui.theme.RemoteContTheme
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

private const val TAG = "MainActivity"
internal const val TCP_BUFFER_SIZE = 16 * 1024

class MainActivity : ComponentActivity() {

    private var tcpSocket: ServerSocket? = null

    private var communicationSocket: Socket? = null

    lateinit var h264DecoderSync: H264DecoderSync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView: View = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions

        actionBar?.hide()
        setContentView(R.layout.main)

        initSyncDecoder()

        try {
            tcpSocket = ServerSocket(9090).apply {
                reuseAddress = true
                soTimeout = 0
            }
        } catch (exception: Exception) {
            println("")
        }

        Thread {
            while (true) {
                communicationSocket = tcpSocket?.accept()?.apply {
                    reuseAddress = true
                    soTimeout = 0
                }

                val data = ByteArray(TCP_BUFFER_SIZE)

                while (communicationSocket != null && communicationSocket!!.isConnected) {
                    val dataLength = communicationSocket!!.getInputStream().read(data)
                    if (dataLength == -1) break

                    val packageData = data.sliceArray(IntRange(0, dataLength - 1))

                    if (::h264DecoderSync.isInitialized) {
                        h264DecoderSync.pushData(packageData)
                    }
                }
            }
        }.start()
    }

    private fun initSyncDecoder() {
        val syncTextureView = findViewById<TextureView>(R.id.syncTextureView)
        syncTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                if (::h264DecoderSync.isInitialized.not()) {
                    h264DecoderSync = H264DecoderSync(syncTextureView)
                    h264DecoderSync.start()
                }
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {}
        }
    }

    private fun localIP(): InetAddress {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val address = networkInterface.inetAddresses
            while (address.hasMoreElements()) {
                val tmpAddress = address.nextElement()
                if (!tmpAddress.isLoopbackAddress && tmpAddress is Inet4Address) {
                    return tmpAddress
                }
            }
        }
        throw IllegalStateException("IP Address not found!")
    }
}