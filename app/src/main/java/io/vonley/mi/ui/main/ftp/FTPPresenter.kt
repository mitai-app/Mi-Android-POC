package io.vonley.mi.ui.main.ftp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.vonley.mi.base.BasePresenter
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.di.network.MiFTPClient
import io.vonley.mi.di.network.SyncService
import io.vonley.mi.utils.SharedPreferenceManager
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import javax.inject.Inject

class FTPPresenter @Inject constructor(
    val sync: SyncService,
    val ftp: MiFTPClient,
    val view: FTPContract.View,
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : BasePresenter(), FTPContract.Presenter {
    override val currentPath: String
        get() = manager.ftpPath ?:"/"

    override fun navigateTo(ftpFile: FTPFile) {
        if (ftpFile.isDirectory) {
            ftp.setWorkingDir(ftpFile)
        }
    }

    override fun navigateTo(path: String) {
        ftp.setWorkingDir(path)
    }

    override fun delete(ftpFile: FTPFile) {
        launch {
            val delete = ftp.delete(ftpFile)
            val event = Event.DELETE.apply {
                filename = ftpFile.name
            }
            withContext(Dispatchers.Main) {
                if (delete) {
                    view.onFTPEventCompleted(event)
                } else {
                    view.onFTPEventFailed(event)
                }
            }
        }
    }

    override fun download(ftpFile: FTPFile) {
        launch {
            val event = Event.DOWNLOAD.apply {
                filename = ftpFile.name
            }
            val download = ftp.download(ftpFile)
            withContext(Dispatchers.Main) {
                download?.let {
                    event.data = it
                    view.onFTPEventCompleted(event)
                } ?: run {
                    view.onFTPEventFailed(event)
                }
            }
        }
    }

    enum class Event(var filename: String, var data: Any? = null) {
        DELETE(""),
        DOWNLOAD(""),
        RENAME(""),
        REPLACE(""),
        UPLOAD("")
    }

    override fun replace(ftpFile: FTPFile, stream: InputStream) {
        launch {
            val replaced = ftp.upload(ftpFile.name, stream)
            val replaceEvent = Event.REPLACE.apply {
                filename = ftpFile.name
            }
            withContext(Dispatchers.Main) {
                if (replaced) {
                    view.onFTPEventCompleted(replaceEvent)
                } else {
                    view.onFTPEventFailed(replaceEvent)
                }
            }
        }
    }

    override fun upload(filename: String, stream: InputStream) {
        launch {
            val upload = ftp.upload(filename, stream)
            val event = Event.UPLOAD.apply {
                this.filename = filename
            }
            withContext(Dispatchers.Main) {
                if (upload) {
                    view.onFTPEventCompleted(event)
                } else {
                    view.onFTPEventFailed(event)
                }
            }
        }
    }

    override fun rename(ftpFile: FTPFile, input: String) {
        launch {
            val rename = ftp.rename(ftpFile, input)
            val upload = Event.RENAME.apply {
                filename = input
                data = ftpFile
            }
            withContext(Dispatchers.Main) {
                if (rename) {
                    view.onFTPEventCompleted(upload)
                } else {
                    view.onFTPEventFailed(upload)
                }
            }
        }
    }


    override fun init() {
        if (!ftp.cwd.hasObservers()) {
            ftp.cwd.observeForever(this)
        }
        sync.target?.run {
            ftp.connect(ip)
        } ?: run {
            view.noTarget()
        }
    }

    override fun cleanup() {
        if (ftp.cwd.hasObservers()) {
            ftp.cwd.removeObserver(this)
            ftp.disconnect()
        }
    }

    override val TAG: String
        get() = FTPPresenter::class.java.name

    override fun onChanged(t: Array<out FTPFile>?) {
        t?.let {
            view.onFTPDirOpened(it)
        } ?: run {
            //TODO nothing?
        }
    }

}
