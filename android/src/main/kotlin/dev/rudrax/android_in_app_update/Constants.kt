package dev.rudrax.android_in_app_update

class UpdateType {
    companion object {
        val UNKNOWN: Int = 0
        val RUNNING: Int = 1
        val DOWNLOADED: Int = 2
        val AVAILABLE: Int = 3
    }
}


class UpdateResult {
    companion object {
        val OK: Int = -1
        val CANCELED: Int = 0
        val ERROR: Int = 1
    }
}

