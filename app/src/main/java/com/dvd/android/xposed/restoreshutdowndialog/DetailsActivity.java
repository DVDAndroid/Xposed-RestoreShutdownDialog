package com.dvd.android.xposed.restoreshutdowndialog;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsActivity extends Activity {

    private TextView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);

        details = (TextView) findViewById(R.id.infos);

        String s = "Hardware: " + Build.HARDWARE + "\n" +
                "Product: " + Build.PRODUCT + "\n" +
                "Device manufacturer: " + Build.MANUFACTURER + "\n" +
                "Device brand: " + Build.BRAND + "\n" +
                "Device model: " + Build.MODEL + "\n" +
                "Device host: " + Build.HOST + "\n" +
                "Android SDK: " + Build.VERSION.SDK_INT + "\n" +
                "Android Release: " + Build.VERSION.RELEASE + "\n" +
                "ROM: " + Build.DISPLAY;

        details.setText(s);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Clipboard", details.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
