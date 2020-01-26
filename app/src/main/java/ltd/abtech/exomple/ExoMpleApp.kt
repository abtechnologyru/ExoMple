package ltd.abtech.exomple

import android.app.Application
import timber.log.Timber

class ExoMpleApp : Application() {

    private class ExoTimberTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String? {
            return "[${LOG_TAG}] ${super.createStackElementTag(element)}"
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(ExoTimberTree())
    }
}