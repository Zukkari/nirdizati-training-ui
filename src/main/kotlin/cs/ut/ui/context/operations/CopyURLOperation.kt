package cs.ut.ui.context.operations

import cs.ut.jobs.SimulationJob
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.util.Clients

class CopyURLOperation(context: SimulationJob) : Operation<SimulationJob>(context) {
    enum class URL(val value: String) {
        VALIDATION("validation?id=%s")
    }

    override fun perform() {
        val url = formBaseUrl() + URL.VALIDATION.value.format(context.id)
        Clients.evalJavaScript("copyToClipboard('$url')")
    }

    private fun formBaseUrl(): String {
        val sb = StringBuilder(Executions.getCurrent().scheme)
        sb.append("://")
        sb.append(Executions.getCurrent().serverName)

        val port = Executions.getCurrent().serverPort
        if (port != 80) {
            sb.append(":$port")
        }

        // Separator
        sb.append("/#")

        return sb.toString()
    }
}