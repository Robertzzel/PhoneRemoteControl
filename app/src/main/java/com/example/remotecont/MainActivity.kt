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
private const val PORT = 9090

class MainActivity : ComponentActivity() {
    private var tcpSocket: ServerSocket? = null
    private var h264DecoderAsync: H264DecoderAsync? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNotificationBar()
        setContentView(R.layout.main)
        initAsyncDecoder()

        try {
            this.getFramesServerConnection()
        } catch (exception: Exception) {
            Log.e("Main", "Cannot get server conn")
        }

        Thread{this.feedReceivedFrames()}.start()
    }

    private fun initAsyncDecoder() {
        val asyncTextureView = findViewById<TextureView>(R.id.syncTextureView)
        asyncTextureView.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    h264DecoderAsync = H264DecoderAsync(surfaceTexture = asyncTextureView.surfaceTexture!!)
                }

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {}
            }
    }

    private fun feedReceivedFrames() {
        while (true) {
            val communicationSocket = tcpSocket?.accept()?.apply {
                reuseAddress = true
                soTimeout = 0
            }

            val data = ByteArray(TCP_BUFFER_SIZE)

            while (communicationSocket == null) {
                continue
            }

            while (communicationSocket.isConnected) {
                val dataLength = communicationSocket.getInputStream().read(data)
                if (dataLength == -1) break

                val packageData = data.sliceArray(IntRange(0, dataLength - 1))
                h264DecoderAsync!!.pushData(packageData)
            }
        }
    }

    private fun getFramesServerConnection() {
        tcpSocket = ServerSocket(PORT).apply {
            reuseAddress = true
            soTimeout = 0
        }
    }

    private fun hideNotificationBar() {
        val decorView: View = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions

        actionBar?.hide()
    }
}