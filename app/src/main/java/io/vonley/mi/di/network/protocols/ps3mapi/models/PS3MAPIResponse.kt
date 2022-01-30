package io.vonley.mi.di.network.protocols.ps3mapi.models

data class PS3MAPIResponse(
    val success: Boolean = false,
    val response: String,
    val code: Code?
){

    enum class SYSCALL8MODE {
        ENABLED, ONLY_COBRAMAMBA_AND_PS3API_ENABLED, ONLY_PS3MAPI_ENABLED, FAKEDISABLED, DISABLED
    }

    enum class Code(val value: Int) {
        DATACONNECTIONALREADYOPEN(125), MEMORYSTATUSOK(150), COMMANDOK(200), REQUESTSUCCESSFUL(226), ENTERINGPASSIVEMOVE(227),
        PS3MAPICONNECTED(220), PS3MAPICONNECTEDOK(230), MEMORYACTIONCOMPLETED(250), MEMORYACTIONPENDING(350)
    }

    companion object {


        private fun findResponse(value: Int) = Code.values().find { c -> c.value == value }

        private fun parseResponse(success: Boolean, response: String): PS3MAPIResponse {
            if (success) {
                val responseCode = Integer.valueOf(response.substring(0, 3)).toInt()
                var buffer = response.substring(4).replace("\r", "").replace("\n", "")
                if (buffer.contains("OK: ")) {
                    buffer = buffer.replace("OK: ", "")
                }
                return create(success, buffer, findResponse(responseCode))
            }
            return create(success, response, null)
        }

        fun parse(
            response: String,
        ): PS3MAPIResponse {
            return parseResponse(true, response)
        }

        fun create(
            success: Boolean,
            response: String,
            code: Code?
        ): PS3MAPIResponse {
            return PS3MAPIResponse(success, response, code)
        }
    }
}
