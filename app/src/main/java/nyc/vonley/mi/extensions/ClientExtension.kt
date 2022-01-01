package nyc.vonley.mi.extensions

import nyc.vonley.mi.models.Client
import java.net.InetAddress


fun InetAddress.client(): Client {
    return Client(this)
}
