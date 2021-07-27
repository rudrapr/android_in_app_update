package dev.rudrax.android_in_app_update

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** AndroidInAppUpdatePlugin */
class AndroidInAppUpdatePlugin : FlutterPlugin, MethodCallHandler,
        ActivityAware, Application.ActivityLifecycleCallbacks {

    private lateinit var methodChannel: MethodChannel
    private var activity: Activity? = null

    private var appUpdateManager: AppUpdateManager? = null
    private var appUpdateInfo: AppUpdateInfo? = null

    val TAG = "MY_FLUTTER_PLUGIN";

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "android_in_app_update")
        methodChannel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "checkUpdateStatus" -> checkForUpdate(result)
            "startFlexibleUpdate" -> startFlexibleUpdate(result)
            "startImmediateUpdate" -> startImmediateUpdate(result)
            "installFlexibleUpdate" -> installFlexibleUpdate(result)
            else -> result.notImplemented()
        }
    }

    /// check for update
    private fun checkForUpdate(result: Result) {
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            Log.i(TAG, "checkForUpdate: ${appUpdateInfo?.updateAvailability()}, ${appUpdateInfo?.installStatus()}")
            this.appUpdateInfo = appUpdateInfo
            when {
                (appUpdateInfo?.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                        && (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) -> {
                    /// The Update is Already downloaded need to Installed
                    result.success(UpdateType.DOWNLOADED)
                }
                (appUpdateInfo?.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                        && ((appUpdateInfo.installStatus == InstallStatus.DOWNLOADING) || (appUpdateInfo.installStatus == InstallStatus.INSTALLING)) -> {
                    /// The update is in progress
                    result.success(UpdateType.RUNNING)
                }
                appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                    /// A New Update is Available.
                    result.success(UpdateType.AVAILABLE)
                }
                else -> {
                    /// Update is not available
                    result.success(UpdateType.UNKNOWN)
                }
            }
        }?.addOnFailureListener {
            result.error(it.message, null, null)
        }


    }

    private fun startImmediateUpdate(result: Result?) {
        val value = appUpdateManager?.startUpdateFlow(
                appUpdateInfo!!,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE))
        PreferenceManager.saveLastUpdateType(activity!!, AppUpdateType.IMMEDIATE)
        value?.addOnSuccessListener {
            if (it == RESULT_OK) {
                //Note: You will not receive the success callback
            } else {
                PreferenceManager.clearUpdateType(activity!!)
                result?.success(UpdateResult.CANCELED)
            }
        }?.addOnFailureListener {
            Log.i(TAG, "startImmediateUpdate: ${it.message}")
            PreferenceManager.clearUpdateType(activity!!)
            result?.error("immediate_update_failed", it.message, null)
            //TODO: Format the errors
        }
    }


    private fun startFlexibleUpdate(result: Result?) {
        val task = appUpdateManager?.startUpdateFlow(
                appUpdateInfo!!,
                activity,
                AppUpdateOptions.defaultOptions(
                        AppUpdateType.FLEXIBLE
                )
        )
        var listener: InstallStateUpdatedListener? = null
        listener = InstallStateUpdatedListener {
            if (it.installStatus == InstallStatus.DOWNLOADED) {
                Log.i(TAG, "startFlexibleUpdate: DOWNLOADED")
                result?.success(UpdateResult.OK)
                appUpdateManager?.unregisterListener(listener)
            } else if (it.installStatus == InstallStatus.CANCELED || it.installStatus == InstallStatus.FAILED) {
                Log.i(TAG, "startFlexibleUpdate: CANCELED")
                result?.success(UpdateResult.CANCELED)
                appUpdateManager?.unregisterListener(listener)

            }
        }
        task?.addOnSuccessListener { it ->
            if (it == RESULT_OK) {
                PreferenceManager.saveLastUpdateType(activity!!, AppUpdateType.FLEXIBLE)
                appUpdateManager?.registerListener(listener)
            } else {
                Log.i(TAG, "startFlexibleUpdate: CANCELED")
                PreferenceManager.clearUpdateType(activity!!)
                result?.success(UpdateResult.CANCELED)

            }
        }?.addOnFailureListener {
            Log.i(TAG, "startFlexibleUpdate: OnFailure")
            result?.error("flexible_update_failed", it.message, null)
        }

    }

    private fun installFlexibleUpdate(result: Result) {
        appUpdateManager?.completeUpdate()?.addOnSuccessListener {
            Log.i(TAG, "installFlexibleUpdate")
            result.success(RESULT_OK)
        }?.addOnFailureListener {
            Log.i(TAG, "installFlexibleUpdate")
            result.error("install_flexible_update_failed", it.message, null);
        }


    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        binding.activity.application.registerActivityLifecycleCallbacks(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        this.activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        binding.activity.application.registerActivityLifecycleCallbacks(this)
    }

    override fun onDetachedFromActivity() {
        this.activity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.i(TAG, "onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.i(TAG, "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.i(TAG, "onActivityResumed")
        if (appUpdateInfo == null) {
            appUpdateManager = AppUpdateManagerFactory.create(activity)
            val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
            appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
                this.appUpdateInfo = appUpdateInfo
                Log.i(TAG, "onActivityResumed: ${appUpdateInfo.updateAvailability()}, ${appUpdateInfo.installStatus()}")
                if (appUpdateInfo?.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                        && PreferenceManager.readLastUpdateType(activity) == AppUpdateType.IMMEDIATE) {
                    startImmediateUpdate(null);
                    /// The Immediate update is in progress show appropriate screen
                }
            }?.addOnFailureListener {

            }

        }

    }

    override fun onActivityPaused(activity: Activity) {
        Log.i(TAG, "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.i(TAG, "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.i(TAG, "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.i(TAG, "onActivityDestroyed")
    }
}
