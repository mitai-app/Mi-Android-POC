package nyc.vonley.mi.ui.main.ftp

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_f_t_p.*
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.R
import nyc.vonley.mi.databinding.FragmentFTPBinding
import nyc.vonley.mi.ui.dialogs.MiInputDialog
import nyc.vonley.mi.ui.main.MainContract
import nyc.vonley.mi.ui.main.ftp.FTPPresenter.Event.*
import nyc.vonley.mi.ui.main.ftp.adapters.FTPChildAttachChangeListener
import nyc.vonley.mi.ui.main.ftp.adapters.FTPFileTouchListener
import nyc.vonley.mi.ui.main.ftp.adapters.FTPRecyclerAdapter
import nyc.vonley.mi.ui.main.ftp.adapters.FTPScrollListener
import nyc.vonley.mi.ui.main.home.dialog
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class FTPFragment : Fragment(), FTPContract.View, ActivityResultCallback<ActivityResult> {

    private var mainView: MainContract.View? = null

    val TAG = FTPFragment::class.java.name

    @Inject
    lateinit var presenter: FTPContract.Presenter

    @Inject
    lateinit var adapter: FTPRecyclerAdapter

    private lateinit var binding: FragmentFTPBinding

    private val contentResolver: ContentResolver
        get() = requireContext().contentResolver

    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private val scroll = FTPScrollListener()
    private val attach = FTPChildAttachChangeListener()
    private val touch = FTPFileTouchListener()
    private var ftpFile: FTPFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFTPBinding.inflate(inflater, container, false)
        binding.recycler.adapter = adapter
        binding.recycler.addOnItemTouchListener(touch)
        binding.recycler.addOnChildAttachStateChangeListener(attach)
        binding.recycler.addOnScrollListener(scroll)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.init()
    }

    override fun onError(e: Throwable) {
        Snackbar.make(
            requireView(),
            e.message ?: "Something went wrong...",
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        presenter.cleanup()
        super.onDestroyView()
    }

    override fun onFTPDirOpened(files: Array<out FTPFile>) {
        adapter.set(files)
        recycler.scrollToPosition(0)
        mainView?.setSummary("CWD: ${presenter.currentPath}")
    }

    override fun onFTPDirClicked(ftpFile: FTPFile) {
        if (ftpFile.name == ".") {
            presenter.navigateTo("/")
        } else {
            presenter.navigateTo(ftpFile)
        }
    }

    override fun onFTPLongClickDir(view: View, ftpFile: FTPFile) {

    }

    override fun onFTPLongClickFile(view: View, ftpFile: FTPFile) {
        val popup = PopupMenu(view.context, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.ftp_long_click, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.ftp_rename -> {
                    MiInputDialog.createDialog("Rename to", "What would you like ${ftpFile.name}")
                        .show(childFragmentManager, TAG)
                }
                R.id.ftp_delete -> {
                    dialog("Are you sure you want to delete ${ftpFile.name}", "OK") { d, i ->
                        presenter.delete(ftpFile)
                        d.dismiss()
                    }.setNegativeButton("Cancel") { d, i ->
                        d.dismiss()
                    }.create().show()
                    true
                }
                R.id.ftp_download -> {
                    presenter.download(ftpFile)
                    true
                }
                R.id.ftp_replace -> {
                    replace(ftpFile)
                    true
                }
                else -> {}
            }
            false
        }
        popup.show()
    }


    override fun noTarget() {
        // Snackbar.make(requireView(), "There's no target set...", Snackbar.LENGTH_LONG).show()
    }

    override fun onFTPEventCompleted(upload: FTPPresenter.Event) {
        val message = when (upload) {
            UPLOAD -> "${upload.filename} upload successful!"
            DELETE -> "${upload.filename} deleted!"
            REPLACE -> "${upload.filename} replaced!"
            DOWNLOAD -> if (save(upload)) {
                "${upload.filename} downloaded! "
            } else {
                "${upload.filename} unable to save the file..."
            }
            RENAME -> {
                val data = upload.data
                if (data is FTPFile) {
                    "Renamed ${data.name} to ${upload.filename} renamed!"
                } else {
                    "Renamed to ${upload.filename}!"
                }
            }
        }
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    override fun onFTPEventFailed(upload: FTPPresenter.Event) {
        val message = when (upload) {
            UPLOAD -> "Failed to upload ${upload.filename}"
            DELETE -> "Failed to delete ${upload.filename}"
            REPLACE -> "Failed to replace ${upload.filename}"
            DOWNLOAD -> "Failed to download ${upload.filename}"
            RENAME -> {
                val data = upload.data
                if (data is FTPFile) {
                    "Failed to rename ${data.name} to ${upload.filename}"
                } else {
                    "Failed to rename to ${upload.filename}"
                }
            }
        }
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }


    override fun onFTPFileClicked(ftpFile: FTPFile) {
        Snackbar.make(
            requireView(),
            "'${ftpFile.name}' clicked",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun open() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.data = Uri.parse("/")
        intent.type = "*/*"
        startForResult.launch(Intent.createChooser(intent, "Open File"))
    }


    private fun save(event: FTPPresenter.Event): Boolean {
        try {
            val bytes = event.data as ByteArray
            val storedFile = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                event.filename
            )
            storedFile.createNewFile()
            FileOutputStream(storedFile).use {
                it.write(bytes)
                it.flush()
            }
            return true
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.message ?: "Something went wrong")
            }
        }
        return false
    }

    private fun replace(ftpFile: FTPFile) {
        this.ftpFile = ftpFile
        open()
    }

    private fun getPath(uri: Uri?): List<String?> {
        val projections =
            arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA)
        val cursor = contentResolver.query(
            uri ?: return emptyList(),
            projections,
            null,
            null,
            null
        )
        return cursor?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.columnNames?.map { cursor.getStringOrNull(cursor.getColumnIndex(it)) }
        } ?: emptyList()
    }

    val Uri.name: String?
        get() {
            return if (this.toString().startsWith("content://")) {
                val cursor = contentResolver.query(this, null, null, null, null)
                cursor?.use {
                    it.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.getString(columnIndex)
                }
            } else this.pathSegments.last()
        }

    /**
     * Duplicated
     * @see nyc.vonley.mi.ui.main.payload.PayloadFragment:93
     */
    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val uri = intent?.data
            if (uri != null) {
                val filename = uri.name
                filename?.let { name ->
                    if (BuildConfig.DEBUG) {
                        Log.e("TAG", "uri: ${uri.path}, name: $filename")
                    }
                    val stream = contentResolver.openInputStream(uri)
                    if (stream != null) {
                        val question = "Click confirm if \"${name}\" is the correct file."
                        val action =
                            Snackbar.make(requireView(), question, Snackbar.LENGTH_INDEFINITE);
                        val yes: (v: View) -> Unit = { view ->
                            ftpFile?.let {
                                presenter.replace(it, stream)
                                ftpFile = null
                            } ?: run {
                                presenter.upload(name, stream)
                            }
                            action.dismiss()
                        }
                        action.setAction("Confirm", yes)
                        action.show()
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Couldn't fetch the filestream",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } ?: run {
                    Snackbar.make(
                        requireView(),
                        "Couldn't fetch the filename :(",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }

            } else {
                val question = "I have failed you :("
                Snackbar.make(requireView(), question, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = (context as? MainContract.View)
    }

    override fun onDetach() {
        super.onDetach()
        mainView = null
    }

    override fun onDialogInput(input: String) {
        super.onDialogInput(input)
        ftpFile?.let {
            presenter.rename(ftpFile!!, input)
        } ?: run {
            dialog("Unable to rename the file :(", "OK") { dialog, i -> dialog.dismiss() }.create()
                .show()
        }
    }

    override fun onDialogCanceled() {
        super.onDialogCanceled()
        ftpFile = null
    }
}