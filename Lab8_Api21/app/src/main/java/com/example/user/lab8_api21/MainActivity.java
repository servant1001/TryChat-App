package com.example.user.lab8_api21;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button send_btn = findViewById(R.id.send_btn);
        Button cancel_btn = findViewById(R.id.cancel_btn);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    notivication();
                }else{
                    notivication2();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //notificationManager.cancelAll();
            }
        });
    }

    public void notivication(){
        NotificationCompat.Builder notivicationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Title")
                .setContentText("text");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notivicationBuilder.build());
    }

    public void notivication2(){
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("1","name",NotificationManager.IMPORTANCE_HIGH);
        /*Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle();
        bigPictureStyle.setBigContentTitle("Photo");//這個和builder的setContentTitle
        bigPictureStyle.setSummaryText("Summary Text");
        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher_background)).getBitmap();
        bigPictureStyle.bigPicture(bitmap);*/

        Notification.Builder builder = new Notification.Builder(MainActivity.this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setColor(Color.BLUE)
                .setContentTitle("Title")
                .setContentText("You have new message")
                .setChannelId("1");

        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,builder.build());
    }

}
