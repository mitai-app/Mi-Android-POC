package nyc.vonley.mi.ui.main.payload

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import nyc.vonley.mi.databinding.FragmentPayloadBinding
import nyc.vonley.mi.models.Console
import okhttp3.Response
import java.io.DataInputStream
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [PayloadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class PayloadFragment : Fragment(), ActivityResultCallback<ActivityResult>, PayloadContract.View {


    private val assets get() = requireContext().assets

    @Inject
    lateinit var presenter: PayloadContract.Presenter

    private val contentResolver: ContentResolver
        get() = requireContext().contentResolver

    lateinit var startForResult: ActivityResultLauncher<Intent>
    lateinit var binding: FragmentPayloadBinding

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.browseBtn.setOnClickListener(this@PayloadFragment::open)
        presenter.init()
    }


    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val uri = intent?.data
            if (uri != null) {
                //val bytes = file.readBytes()
                //val openInputStream = assets.open("payloads/orbis/75x.bin")
                val openInputStream = contentResolver.openInputStream(uri)
                val dis = DataInputStream(openInputStream)
                val bytes = dis.readBytes()
                dis.close()
                val question = "Click confirm if this is the correct payload."
                val action = Snackbar.make(requireView(), question, Snackbar.LENGTH_INDEFINITE);
                val yes: (v: View) -> Unit = { view ->
                    presenter.sendPayload(bytes)
                    action.dismiss()
                }
                action.setAction("Confirm", yes)
                action.show()
            } else {
                val question = "I have failed you :("
                Snackbar.make(requireView(), question, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPayloadSent(response: Response) {
        Toast.makeText(requireContext(), "CODE: ${response.code}, ${response.message}", Toast.LENGTH_LONG).show()

    }

    override fun onError(e: Throwable) {
        Toast.makeText(requireContext(), "We couldn't send the payload. \nError Message: ${e.message}", Toast.LENGTH_LONG).show()
    }
}