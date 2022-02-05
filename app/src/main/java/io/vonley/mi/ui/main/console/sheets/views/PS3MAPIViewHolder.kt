package io.vonley.mi.ui.main.console.sheets.views

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import io.vonley.mi.databinding.ViewPs3mapiBinding
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.di.network.protocols.ps3mapi.PS3MAPI
import io.vonley.mi.extensions.e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PS3MAPIViewHolder(
    val binding: ViewPs3mapiBinding,
    override val protocol: PS3MAPI
) : RecyclerView.ViewHolder(binding.root),
    ViewHolderProtocol<PS3MAPI> {

    val TAG = this::class.java.simpleName

    override fun init() {
        binding.vhConsoleId.refresh.setOnClickListener {
            launch {
                try {
                    protocol.boot(Boot.SOFTREBOOT)
                } catch (e: Exception) {
                }
            }
        }
        binding.vhConsoleId.restart.setOnClickListener {
            launch {
                try {
                    protocol.boot(Boot.REBOOT)
                } catch (e: Exception) {
                }
            }
        }
        binding.vhConsoleId.shutdown.setOnClickListener {
            launch {
                try {
                    protocol.boot(Boot.SHUTDOWN)
                } catch (e: Exception) {
                }
            }
        }
        binding.vhConsoleId.beep.setOnClickListener {
            launch {
                try {
                    "Hello I have been clicked".e(TAG)
                    protocol.buzzer(Buzzer.SINGLE)
                } catch (e: Exception) {
                    "Hmmm: ${e.message}".e(TAG, e)
                }
            }
        }
        binding.vhConsoleId.msg.setImeActionLabel("Send", KeyEvent.KEYCODE_ENTER)
        binding.vhConsoleId.msg.setOnEditorActionListener edit@{ v, actionId, event ->
            return@edit if (v.text.isNotEmpty()) {
                launch {
                    try {
                        protocol.notify(v.text.toString())
                        delay(500)
                        withContext(Dispatchers.IO) {
                            binding.vhConsoleId.msg.text = null
                        }
                    } catch (e: Exception) {
                    }
                }
                true
            } else false
        }
        launch {
            protocol.connect()
        }
    }

}