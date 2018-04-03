package cs.ut.configuration

/**
 * Helper object that reads traverses the configuration tree
 *
 * @see Configuration
 */
object ConfigurationReader {
    private var configuration = Configuration.readSelf()

    private const val pathDelimiter = "/"

    /**
     * Reloads the configuration into memory
     */
    fun reload() {
        configuration = Configuration.readSelf()
    }

    /**
     * Find configuration node based on given path.
     * Path should be delimited using delimiter as the "pathDelimiter" value
     *
     * @param path to look for
     * @return configuration node corresponding to given path
     */
    fun findNode(path: String): ConfigNode {
        var currentNode: ConfigNode? = null

        path.split(pathDelimiter).forEach { p ->
            val nodes = if (currentNode == null) configuration.childNodes else currentNode!!.childNodes
            currentNode = nodes.first { it.identifier == p }
        }

        return currentNode ?: throw NoSuchElementException("Node with path $path could not be found")
    }
}