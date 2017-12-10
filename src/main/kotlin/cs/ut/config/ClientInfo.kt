package cs.ut.config

data class ClientInfo(
        val screenWidth: Int,
        val screenHeight: Int,
        val windowWidth: Int,
        val windowHeight: Int,
        val colorDepth: Int,
        val orientation: String,
        val maxJobCount: Int
)