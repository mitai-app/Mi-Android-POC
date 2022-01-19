package nyc.vonley.mi.di.network.impl

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.di.annotations.SharedPreferenceStorage
import nyc.vonley.mi.di.network.MiFTPClient
import nyc.vonley.mi.utils.SharedPreferenceManager
import nyc.vonley.mi.utils.set
import okhttp3.internal.notifyAll
import org.apache.commons.net.ProtocolCommandEvent
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject

class MiFTPClientImpl @Inject constructor(@SharedPreferenceStorage override val manager: SharedPreferenceManager) :
    MiFTPClient {

    override val job: Job = Job()
    private lateinit var client: FTPClient
    private val ftpPath get() = manager.ftpPath
    private val ftpUser get() = manager.ftpUser
    private val ftpPass get() = manager.ftpPass
    private var _ip: String? = null
    private var _port: Int? = null

    private val _cwd: MutableLiveData<Array<out FTPFile>> = MutableLiveData<Array<out FTPFile>>()

    companion object {
        const val TAG = ".MiFTPClientImpl"
    }


    private val callback = object : MiFTPEventListener {

        override fun onFailedToConnect() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Unable to connect")
            }
        }

        override fun onLoggedIn() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "User is logged in")
            }
        }

        override fun onInvalidCredentials() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "User entered invalid credentials")
            }
        }

        override fun onDirChanged() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Directory Changed")
            }
        }

        override fun isLoggedInAlready() {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "User is already logged in")
            }
        }

    }

    interface MiFTPEventListener {
        fun onLoggedIn()
        fun onInvalidCredentials()
        fun onDirChanged()
        fun isLoggedInAlready()
        fun onFailedToConnect()
    }

    private suspend fun _connect(ip: String, port: Int) {
        try {
            client.connect(ip, port)
            client.addProtocolCommandListener(this@MiFTPClientImpl)
            val login = client.login(ftpUser, ftpPass)
            if (login) {
                setWorkingDir(ftpPath)
                client.setFileType(FTP.BINARY_FILE_TYPE)
                callback.onLoggedIn()
            } else {
                callback.onInvalidCredentials()
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
            callback.onFailedToConnect()
        }
    }

    override fun connect(ip: String, port: Int) {
        if (!this::client.isInitialized) {
            client = FTPClient()
            _ip = ip
            _port = port
        }
        if (this::client.isInitialized) {
            if (client.isConnected) {
                client.disconnect()
            }
            val block: suspend CoroutineScope.() -> Unit = {
                if (client.isConnected) {
                    if (_ip != ip || _port != port) {
                        _ip = ip
                        _port = port
                        client.logout()
                        client.disconnect()
                        _connect(ip, port)
                    } else {
                        callback.isLoggedInAlready()
                    }
                } else {
                    _connect(ip, port)
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
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "dir: $dir")
        }
        launch {
            val changed = client.changeWorkingDirectory(dir)
            client.printWorkingDirectory()?.let {
                manager[SharedPreferenceManager.FTPPATH] = it
            }
            if (changed) {
                getGWD()
                callback.onDirChanged()
            }
        }
    }

    private suspend fun getGWD() {
        if (BuildConfig.DEBUG) {
            val cwm = client.printWorkingDirectory()
            Log.e("CWD", "CWD: $cwm")
        }
        val dir: Array<out FTPFile> = if (client.isConnected) {
            val listFiles = client.listFiles()
            listFiles.sortByDescending { it.isDirectory }
            listFiles
        } else arrayOf()
        withContext(Dispatchers.Main) {
            synchronized(_cwd) {
                _cwd.value = dir
                _cwd.notifyAll()
            }
        }
    }


    override suspend fun upload(file: String, stream: InputStream): Boolean {
        if (client.isConnected) {
            client.enterLocalPassiveMode()
            val file = client.storeFile(file, stream)
            stream.close()
            if (file) {
                getGWD()
            }
            return file
        }
        return false
    }

    override fun disconnect() {
        launch {
            try {
                client.removeProtocolCommandListener(this@MiFTPClientImpl)
                client.logout()
                client.disconnect()
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.message ?: "Something went wrong")
                }
            }
        }
    }

    override suspend fun delete(file: FTPFile): Boolean {
        if (client.isConnected) {
            if (file.isFile) {
                //TODO: WTF is this a bug?
                val deleteFile = client.deleteFile("${ftpPath}/${file.name}")
                getGWD()
                return deleteFile
            }
        }
        return false
    }

    override fun protocolCommandSent(event: ProtocolCommandEvent?) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, event?.message ?: "Sent")
        }
    }

    override fun protocolReplyReceived(event: ProtocolCommandEvent?) {

    }


}