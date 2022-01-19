package nyc.vonley.mi.di.network

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import nyc.vonley.mi.utils.SharedPreferenceManager
import org.apache.commons.net.ProtocolCommandListener
import org.apache.commons.net.ftp.FTPFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

interface MiFTPClient : CoroutineScope, ProtocolCommandListener {
    val job: Job
    val manager: SharedPreferenceManager
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job
    val cwd: LiveData<Array<out FTPFile>>
    fun connect(ip: String, port: Int = 2121)
    fun setWorkingDir(dir: String?)
    fun setWorkingDir(ftpFile: FTPFile)
    suspend fun upload(file: String, byteArray: InputStream): Boolean
    suspend fun upload(file: String, byteArray: ByteArray): Boolean = upload(file, ByteArrayInputStream(byteArray))
    suspend fun upload(file: File): Boolean = upload(file.name, FileInputStream(file))
    suspend fun delete(file: FTPFile): Boolean
    fun disconnect()

}