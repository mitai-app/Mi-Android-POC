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
        val currentPath: String

        fun navigateTo(ftpFile: FTPFile)
        fun navigateTo(path: String)
        fun delete(ftpFile: FTPFile)
        fun download(ftpFile: FTPFile)
        fun replace(ftpFile: FTPFile, stream: InputStream)
        fun replace(ftpFile: FTPFile, file: File) = replace(ftpFile, FileInputStream(file))
        fun replace(ftpFile: FTPFile, bytes: ByteArray) = replace(ftpFile, ByteArrayInputStream(bytes))
        fun upload(filename: String, stream: InputStream)
        fun upload(filename: String, bytes: ByteArray) = upload(filename, ByteArrayInputStream(bytes))
        fun upload(file: File) = upload(file.name, FileInputStream(file))
        fun rename(ftpFile: FTPFile, input: String)
    }

    interface View : BaseContract.View {
        fun onFTPDirOpened(files: Array<out FTPFile>)
        fun onFTPFileClicked(ftpFile: FTPFile)
        fun onFTPDirClicked(ftpFile: FTPFile)
        fun onFTPLongClickDir(view: android.view.View, ftpFile: FTPFile)
        fun onFTPLongClickFile(view: android.view.View, ftpFile: FTPFile)
        fun noTarget()
        fun onFTPEventCompleted(upload: FTPPresenter.Event)
        fun onFTPEventFailed(upload: FTPPresenter.Event)
        fun open()
    }

}
