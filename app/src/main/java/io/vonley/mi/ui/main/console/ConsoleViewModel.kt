package io.vonley.mi.ui.main.console

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import io.vonley.mi.di.repository.ConsoleRepository
import io.vonley.mi.models.Console
import kotlin.coroutines.CoroutineContext

class ConsoleViewModel(
    application: Application, respository: ConsoleRepository
) : AndroidViewModel(application), CoroutineScope {

    private var _consoles: LiveData<List<Console>> = respository.getMyConsoles()

    val consoles get() = _consoles

    class Factory(val application: Application, private val dao: ConsoleRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(
                Application::class.java,
                ConsoleRepository::class.java
            ).newInstance(application, dao)
        }
    }

    protected val job = Job()

    override val coroutineContext: CoroutineContext = job + Dispatchers.IO
}