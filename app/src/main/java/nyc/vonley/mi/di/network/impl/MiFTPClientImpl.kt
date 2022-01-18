package nyc.vonley.mi.di.network.impl

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.MiFTPClient
import nyc.vonley.mi.utils.SharedPreferenceManager
import nyc.vonley.mi.utils.get
import okhttp3.internal.notify
import okhttp3.internal.notifyAll
import org.apache.commons.net.ProtocolCommandEvent
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

class MiFTPClientImpl @Inject constructor(@SharedPreferenceStorage override val manager: SharedPreferenceManager) :
    MiFTPClient {

    override val job: Job = Job()
    private lateinit var client: FTPClient
    private val ftpPath get() = manager.ftpPath
    private val ftpUser get() = manager.ftpUser
    private val ftpPass get() = manager.ftpPass

    private val _cwd: MutableLiveData<Array<out FTPFile>> = MutableLiveData<Array<out FTPFile>>()

    companion object {
        const val TAG = ".MiFTPClientImpl"
    }


    private val callback = object : MiFTPEventListener {

        override fun onLoggedIn() {

        }

        override fun onInvalidCredentials() {

        }

        override fun onDirChanged() {

        }

    }

    interface MiFTPEventListener {
        fun onLoggedIn()
        fun onInvalidCredentials()
        fun onDirChanged()
    }

    override fun connect(ip: String, port: Int) {
        if (!this::client.isInitialized) client = FTPClient()
        if (this::client.isInitialized) {
            if (client.isConnected) {
                client.disconnect()
            }
            client.addProtocolCommandListener(this)
            val block: suspend CoroutineScope.() -> Unit = {
                client.connect(ip, port)
                val login = client.login(ftpUser, ftpPass)
                if (login) {
                    setWorkingDir(ftpPath)
                    client.setFileType(FTP.BINARY_FILE_TYPE)
                    withContext(Dispatchers.Main) {
                        callback.onLoggedIn()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback.onInvalidCredentials()
                    }
                }
            }
            launch(block = block)
        }
    }

    override val cwd: LiveData<Array<out FTPFile>> get() = _cwd

    override fun setWorkingDir(ftpFile: FTPFile) {
        if (ftpFile.isDirectory) {
            setWorkingDir(ftpFile.name)
        }
    }

    override fun setWorkingDir(dir: String?) {
        if(BuildConfig.DEBUG){
            Log.e(TAG, "dir: $dir")
        }
        launch {
            val changed = client.changeWorkingDirectory(dir)
            if(changed){
                getGWD()
                callback.onDirChanged()
            }
        }
    }

    private suspend fun getGWD() {

        val dir: Array<out FTPFile> = if (client.isConnected) {

            if(BuildConfig.DEBUG) {
                val cwm = client.printWorkingDirectory()
                Log.e("CWD", "CWD: $cwm")
            }

            client.listFiles()
        } else arrayOf()
        withContext(Dispatchers.Main) {
            synchronized(_cwd) {
                _cwd.value = dir
                _cwd.notifyAll()
            }
        }
    }

    override fun up() {
        launch {
            client.cdup()
            getGWD()
        }
    }

    override suspend fun upload(file: String, byteArray: ByteArray): Boolean {
        val stream = ByteArrayInputStream(byteArray)
        return upload(file, stream)
    }

    override suspend fun upload(file: String, stream: InputStream): Boolean {
        client.enterLocalPassiveMode()
        val file = client.storeFile(file, stream)
        stream.close()
        return file
    }

    override fun disconnect() {
        launch {
            client.logout()
            client.disconnect()
        }
    }


    override fun protocolCommandSent(event: ProtocolCommandEvent?) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, event?.message ?: "Sent")
        }
    }

    override fun protocolReplyReceived(event: ProtocolCommandEvent?) {

    }


}