package nyc.vonley.mi.di.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.flow
import nyc.vonley.mi.di.network.ClientSync
import nyc.vonley.mi.di.repository.base.BaseRepository
import nyc.vonley.mi.models.Console
import nyc.vonley.mi.persistence.ConsoleDao
import javax.inject.Inject

class ConsoleRepository @Inject constructor(
    private val sync: ClientSync,
    dao: ConsoleDao
) : BaseRepository<ConsoleDao>(dao) {

    fun getMyConsoles(): LiveData<List<Console>> {
        if (sync.isConnected) {
            val wifi = sync.connectionInfo.ssid
            val consoles = if (wifi != null) {
                dao.get(wifi)
            } else {
                dao.getAll()
            }
            return consoles
        }
        return dao.getAll()
    }

    @WorkerThread
    suspend fun getConsoles(
        onSuccess: () -> Unit
    ) = flow {
        val consoles = getMyConsoles()
        emit(consoles)
        onSuccess()
    }

}
