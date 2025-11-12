package ro.pub.cs.systems.eim.lab06.clientservercommunication.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import ro.pub.cs.systems.eim.lab06.clientservercommunication.general.Constants
import ro.pub.cs.systems.eim.lab06.clientservercommunication.general.Utilities.getReader
import java.io.IOException
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ClientAsyncTask(private val serverMessageTextView: TextView) {
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    fun execute(serverAddress: String, serverPort: String) {
        // Clear text view on UI thread
        mainHandler.post { serverMessageTextView.text = "" }

        // Execute network operation on background thread
        executorService.execute {
            var socket: Socket? = null
            try {
                val port = serverPort.toInt()
                socket = Socket(serverAddress, port)
                Log.v(
                    Constants.TAG,
                    "Connection opened with ${socket.inetAddress}:${socket.localPort}"
                )
                val bufferedReader = getReader(socket)
                var currentLine: String?
                while (bufferedReader.readLine().also { currentLine = it } != null) {
                    val line = currentLine
                    // Update UI on main thread
                    mainHandler.post { serverMessageTextView.append("$line\n") }
                }
            } catch (ioException: IOException) {
                Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
            } finally {
                try {
                    socket?.close()
                    Log.v(Constants.TAG, "Connection closed")
                } catch (ioException: IOException) {
                    Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
                }
            }
        }
    }
}
