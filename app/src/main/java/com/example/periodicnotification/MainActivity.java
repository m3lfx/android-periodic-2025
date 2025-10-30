package com.example.periodicnotification;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Button setNotification;
    ConstraintLayout constraintLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setNotification = findViewById(R.id.buttonSetNotification);

        setNotification.setOnClickListener(v -> {

            /*
            The POST_NOTIFICATION permission requires API 33 and above.
            we should handle this with an if statement
            */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){

                    /*
                    In an educational UI, explain to the user why your app requires this
                    permission for a specific feature to behave as expected, and what
                    features are disabled if it's declined. In this UI, include a
                    "cancel" or "no thanks" button that lets the user continue
                    using your app without granting the permission. So we show a snackbar message for this.
                    If you want you can create dialog message or bottom sheet dialog
                    */
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){

                        Snackbar.make(
                                constraintLayout,
                                "Please allow the permission to take notification",
                                Snackbar.LENGTH_LONG).setAction("Allow", v1 -> {
                                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.POST_NOTIFICATIONS},1);
                                }
                        ).show();

                    }else {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
                    }

                }else {
                    setNotificationTime();
                }
            }else {
                setNotificationTime();
            }

        });
    }

    public void setNotificationTime(){

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR);
        int currentMinute = calendar.get(Calendar.MINUTE);

        //creating MaterialTimePicker and set the current time
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText("Set Notification Time")
                .build();
        timePicker.show(getSupportFragmentManager(),"1");

        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get the selected time
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Log.i("hour", calendar.toString());


                Intent i = new Intent(getApplicationContext(),NotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, i, PendingIntent.FLAG_IMMUTABLE);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);

            }
        });
    }


    //handle the permission result
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            setNotificationTime();

        }else {
            Snackbar.make(
                    constraintLayout,
                    "Please allow the permission to take notification",
                    Snackbar.LENGTH_LONG).setAction("Allow", v1 -> {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
                    }
            ).show();
        }
    }

}