package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.ref.WeakReference;
import java.util.Map;

public class SquawkFirebaseMessagingService extends FirebaseMessagingService {

    private static final String AUTHOR = SquawkContract.COLUMN_AUTHOR;
    private static final String MESSAGE = SquawkContract.COLUMN_MESSAGE;
    private static final String KEY = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String DATE = SquawkContract.COLUMN_DATE;

    // COMPLETED (1) Make a new Service in the fcm package that extends from FirebaseMessagingService.
    public SquawkFirebaseMessagingService() {
    }

    // COMPLETED (2) As part of the new Service - Override onMessageReceived. This method will
    // be triggered whenever a squawk is received. You can get the data from the squawk
    // message using getData(). When you send a test message, this data will include the
    // following key/value pairs:
    // test: true
    // author: Ex. "TestAccount"
    // authorKey: Ex. "key_test"
    // message: Ex. "Hello world"
    // date: Ex. 1484358455343
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        // COMPLETE (3) As part of the new Service - If there is message data, get the data using
        // the keys and do two things with it :
        // 1. Display a notification with the first 30 character of the message
        // 2. Use the content provider to insert a new message into the local database
        // Hint: You shouldn't be doing content provider operations on the main thread.
        // If you don't know how to make notifications or interact with a content provider
        // look at the notes in the classroom for help.
        sendNotification(data);
        saveMessage(data);
    }

    private void sendNotification(Map<String, String> data) {
        Intent startActivityIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                1,
                startActivityIntent,
                PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(data.get(AUTHOR))
                .setContentText(data.get(MESSAGE).substring(0, 30))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_duck)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && notificationBuilder != null) {
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    private void saveMessage(final Map<String, String> data) {
        SaveMessageTask saveMessageTask = new SaveMessageTask(this, data);
        saveMessageTask.execute();
    }

    private static class SaveMessageTask extends android.os.AsyncTask<Void, Void, Void> {

        private WeakReference<Service> service;
        private Map<String, String> data;

        SaveMessageTask(Service context, Map<String, String> data) {
            this.service = new WeakReference<>(context);
            this.data = data;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentValues message = new ContentValues();
            message.put(SquawkContract.COLUMN_AUTHOR, data.get(AUTHOR));
            message.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(KEY));
            message.put(SquawkContract.COLUMN_DATE, data.get(DATE));
            message.put(SquawkContract.COLUMN_MESSAGE, data.get(MESSAGE));
            service.get().getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, message);
            return null;
        }
    };

}
