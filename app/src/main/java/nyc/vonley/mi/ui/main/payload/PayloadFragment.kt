package nyc.vonley.mi.ui.main.payload

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.databinding.FragmentPayloadBinding
import java.io.DataInputStream
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PayloadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class PayloadFragment : Fragment(), ActivityResultCallback<ActivityResult>, PayloadContract.View {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    @Inject
    lateinit var presenter: PayloadContract.Presenter

    private val contentResolver: ContentResolver
        get() = requireContext().contentResolver

    lateinit var startForResult: ActivityResultLauncher<Intent>
    lateinit var binding: FragmentPayloadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

    private fun open(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.data = Uri.parse("/")
        intent.type = "*/*"
        startForResult.launch(Intent.createChooser(intent, "Open Folder"))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPayloadBinding.inflate(inflater, container, false)
        binding.browseBtn.setOnClickListener(this@PayloadFragment::open)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PayloadFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PayloadFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val uri = intent?.data
            if (uri != null) {
                val file = DataInputStream(contentResolver.openInputStream(uri))
                val bytes = file.readBytes()
                file.close()
                val question = "Is this the correct payload?"
                val action = Snackbar.make(requireView(), question, Snackbar.LENGTH_INDEFINITE);
                val yes: (v: View) -> Unit = { view ->
                    presenter.sendPayload(bytes)
                    action.dismiss()
                }
                val no: (v: View) -> Unit = { view ->
                    action.dismiss()
                }
                action.setAction("Yes", yes).setAction("No", no)
                action.show()
            } else {
                val question = "I have failed you :("
                Snackbar.make(requireView(), question, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onError(e: Throwable) {

    }
}