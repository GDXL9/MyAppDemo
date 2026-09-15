package com.example.myappdemo

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import java.util.concurrent.Executors

/** 在后台联网更新天气，完成后刷新所有桌面小组件。 */
class WeatherUpdateJobService : JobService() {
    private val executor = Executors.newSingleThreadExecutor()

    override fun onStartJob(params: JobParameters): Boolean {
        executor.execute {
            WeatherRepository.refreshBlocking(applicationContext)
            WidgetViewsFactory.updateAllWidgets(applicationContext)
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = true

    companion object {
        private const val IMMEDIATE_JOB_ID = 2001
        private const val PERIODIC_JOB_ID = 2002

        fun scheduleImmediate(context: Context) {
            runCatching {
                val scheduler = context.getSystemService(JobScheduler::class.java)
                val info = JobInfo.Builder(
                    IMMEDIATE_JOB_ID,
                    ComponentName(context, WeatherUpdateJobService::class.java)
                )
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setOverrideDeadline(5_000)
                    .build()
                scheduler.schedule(info)
            }
        }

        fun ensurePeriodic(context: Context) {
            runCatching {
                val scheduler = context.getSystemService(JobScheduler::class.java)
                if (scheduler.allPendingJobs.any { it.id == PERIODIC_JOB_ID }) return@runCatching
                val info = JobInfo.Builder(
                    PERIODIC_JOB_ID,
                    ComponentName(context, WeatherUpdateJobService::class.java)
                )
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(60 * 60 * 1000L)
                    .build()
                scheduler.schedule(info)
            }
        }

        fun cancelAll(context: Context) {
            val scheduler = context.getSystemService(JobScheduler::class.java)
            scheduler.cancel(IMMEDIATE_JOB_ID)
            scheduler.cancel(PERIODIC_JOB_ID)
        }
    }
}
