package com.example.remotecont

import androidx.compose.material3.contentColorFor
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.locks.Lock

class InputSender(host: String, port: Int) {
    private var conn: Socket
    private var buffer: ByteArray
    private var stream: OutputStream

    init {
        conn = Socket(host, port)
        buffer = ByteArray(0)
        stream = conn.getOutputStream()
    }

    private fun add(input: String) {
        buffer += input.toByteArray()
    }

    fun start() {

    }
}