package com.example.helper1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val eventType = inputData.getString("event_type") ?: "event"
        val eventName = inputData.getString("event_name") ?: "Напоминание"
        val notificationId = inputData.getInt("notification_id", 0)
        val eventId = inputData.getInt("event_id", 0) // Получаем ID события

        // Выбираем канал уведомлений в зависимости от типа события
        val channelId = if (eventType == "task") "task_channel" else "event_channel"

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем Intent для открытия MainActivity при нажатии на уведомление
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("event_id", eventId) // Передаем ID события
        }

        // Создаем PendingIntent для запуска MainActivity
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId, // Уникальный ID для каждого уведомления
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Убедитесь, что каналы уведомлений созданы
        createNotificationChannels(notificationManager)

        // Название группы уведомлений
        val groupKey = "event_group"

        // Создаем уведомление с иконкой
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_notification) // Иконка уведомления
            .setContentTitle("Напоминаем")
            .setContentText(eventName)
            .setContentIntent(pendingIntent)  // Устанавливаем PendingIntent
            .setAutoCancel(true)  // Удаление уведомления после нажатия
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(groupKey) // Группировка уведомлений
            .build()

        // Создаем итоговое уведомление для группы
        val summaryNotification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_notification) // Иконка уведомления
            .setContentTitle("Напоминаем")
            .setContentText("У вас несколько напоминаний")
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("Нажмите для просмотра всех напоминаний"))
            .setGroup(groupKey) // Группировка уведомлений
            .setGroupSummary(true) // Уведомление сводки
            .build()

        // Отправьте уведомление
        notificationManager.notify(notificationId, notification)

        // Отправьте сводное уведомление (оно будет группировать все уведомления)
        notificationManager.notify(0, summaryNotification)

        return Result.success()
    }

    private fun createNotificationChannels(notificationManager: NotificationManager) {
        // Канал для событий
        val eventChannel = NotificationChannel(
            "event_channel",
            "События",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(eventChannel)

        // Канал для задач
        val taskChannel = NotificationChannel(
            "task_channel",
            "Задачи",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(taskChannel)
    }
}
