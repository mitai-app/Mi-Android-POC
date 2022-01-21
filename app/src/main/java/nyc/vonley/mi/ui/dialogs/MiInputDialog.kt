package nyc.vonley.mi.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import nyc.vonley.mi.R
import nyc.vonley.mi.base.BaseContract
import nyc.vonley.mi.databinding.DialogMiInputBinding


class MiInputDialog private constructor() : DialogFragment(), View.OnClickListener {

    private val TAG = MiInputDialog::class.java.name

    private lateinit var binding: DialogMiInputBinding
    private lateinit var title: String
    private lateinit var hint: String
    private var value: String? = null

    private var iView: BaseContract.View? = null
        set(value) {
            if (value != null) {
                Log.i(TAG, "Attached from: ${value.javaClass.name}")
            } else if (field != null) {
                Log.i(TAG, "Detached from ${field?.javaClass?.name}")
            }
            field = value
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState)
        arguments?.let {
            title = it.getString("title")!!
            hint = it.getString("hint")!!
            value = it.getString("value");
        }
        return onCreateDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMiInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layout.hint = title
        value?.let {
            binding.input.setText(it)
        }
        binding.submit.setOnClickListener(this)
        binding.cancel.setOnClickListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        iView = context as? BaseContract.View
    }

    override fun onDetach() {
        super.onDetach()
        iView = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        iView?.onDialogCanceled()
    }

    companion object {

        fun createDialog(title: String, hint: String, value: String? = null): MiInputDialog {
            val miInputDialog = MiInputDialog().apply {
                val bundle = Bundle()
                bundle.putString("hint", hint)
                bundle.putString("title", title)
                bundle.putString("value", value)
                arguments = bundle
            }
            return miInputDialog
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.submit -> {
                iView?.onDialogInput(binding.input.text.toString())
                dismiss()
            }
            R.id.cancel -> {
                dismiss()
            }
        }
    }

}