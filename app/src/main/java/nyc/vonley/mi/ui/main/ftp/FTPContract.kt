package nyc.vonley.mi.ui.main.ftp

import androidx.lifecycle.Observer
import nyc.vonley.mi.base.BaseContract
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream

interface FTPContract {

    interface Presenter : BaseContract.Presenter, Observer<Array<out FTPFile>> {
        fun navigateTo(ftpFile: FTPFile)
        fun navigateTo(path: String)
        fun delete(ftpFile: FTPFile)
        fun download(ftpFile: FTPFile, location: String)
        fun replace(ftpFile: FTPFile, file: File)
        fun upload(filename: String, stream: InputStream)
        fun upload(filename: String, bytes: ByteArray) = upload(filename, ByteArrayInputStream(bytes))
        fun upload(file: File) = upload(file.name, FileInputStream(file))
    }

    interface View : BaseContract.View {
        fun onFTPDirOpened(files: Array<out FTPFile>)
        fun onFTPFileClicked(ftpFile: FTPFile)
        fun onFTPDirClicked(ftpFile: FTPFile)
        fun onFTPLongClickDir(view: android.view.View, ftpFile: FTPFile)
        fun onFTPLongClickFile(view: android.view.View, ftpFile: FTPFile)
        fun onFileUpload(filename: String)
        fun onFileFailed(filename: String)
        fun onFTPFileDeleted(ftpFile: FTPFile)
        fun onFTPFailedToDelete(ftpFile: FTPFile)
    }

}
