package nyc.vonley.mi.ui.main.payload

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.BuildConfig
import nyc.vonley.mi.databinding.FragmentPayloadBinding
import nyc.vonley.mi.ui.main.MainContract
import nyc.vonley.mi.ui.main.home.dialog
import nyc.vonley.mi.ui.main.payload.adapters.PayloadAdapter
import okhttp3.Response
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [PayloadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class PayloadFragment : Fragment(), ActivityResultCallback<ActivityResult>, PayloadContract.View,
    SwipeRefreshLayout.OnRefreshListener {


    private val assets get() = requireContext().assets

    @Inject
    lateinit var presenter: PayloadContract.Presenter

    private var mainView: MainContract.View? = null

    private val contentResolver: ContentResolver
        get() = requireContext().contentResolver

    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentPayloadBinding
    private val payloadAdapter = PayloadAdapter()

    private var swipeCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            if (swipeDir == ItemTouchHelper.LEFT || swipeDir == ItemTouchHelper.RIGHT) {
                val position = viewHolder.bindingAdapterPosition
                payloadAdapter.remove(position)
            }
        }
    }
    private val swipeTouchHelper by lazy { ItemTouchHelper(swipeCallback) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)
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


    override fun open() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.data = Uri.parse("/")
        intent.type = "*/*"
        startForResult.launch(Intent.createChooser(intent, "Open Folder"))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayloadBinding.inflate(inflater, container, false)
        binding.recycler.adapter = payloadAdapter
        swipeTouchHelper.attachToRecyclerView(binding.recycler)
        binding.root.setOnRefreshListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.init()
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


    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val uri = intent?.data
            if (uri != null) {
                val filename = uri.name
                filename?.let { name ->
                    if (BuildConfig.DEBUG) {
                        Log.e("TAG", "uri: ${uri.path}, name: $filename")
                    }
                    val stream = contentResolver.openInputStream(uri)?.readBytes()
                    if (stream != null) {
                        payloadAdapter.add(PayloadAdapter.Payload(name, stream))
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Couldn't fetch the filestream :(",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } ?: run {
                    Snackbar.make(
                        requireView(),
                        "Couldn't fetch the file :(",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                val question = "I have failed you :("
                Snackbar.make(requireView(), question, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPayloadSent(response: Response) {
        Toast.makeText(
            requireContext(),
            "CODE: ${response.code}, ${response.message}",
            Toast.LENGTH_LONG
        ).show()

    }

    override fun onError(e: Throwable) {
        Toast.makeText(
            requireContext(),
            "We couldn't send the payloads",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPayloadFailed(payload: PayloadAdapter.Payload) {
        payloadAdapter.update(payload)
    }

    override fun onFinished() {
        payloadAdapter.clear()
        binding.root.isRefreshing = false
    }

    override fun onWriting(payload: PayloadAdapter.Payload) {

    }

    override fun onSent(payload: PayloadAdapter.Payload) {
        payloadAdapter.update(payload)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainView = context as? MainContract.View
    }

    override fun onDetach() {
        super.onDetach()
        mainView = null
    }

    override fun onRefresh() {
        if (payloadAdapter.itemCount > 0) {
            val name = payloadAdapter.payloads.map { it.name }.toList().joinToString(",")
            val jbPort = presenter.manager.featurePort
            val question =
                "Click confirm if \"${name}\" are the correct payloads, and we are sending it to the right plugin, \"${jbPort.title}\" (Port ${jbPort.ports.first()}) otherwise press cancel."
            dialog(question, "Confirm") { dialog, i ->
                presenter.sendMultiplePayloads(payloadAdapter.payloads)
                dialog.dismiss()
            }.setNegativeButton("Cancel")
            { dialog, i ->
                binding.root.isRefreshing = false
                dialog.dismiss()
            }.create().show()
        }
    }
}