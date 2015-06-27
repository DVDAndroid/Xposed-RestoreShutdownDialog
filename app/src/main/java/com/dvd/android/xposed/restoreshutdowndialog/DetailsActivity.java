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

        StringBuilder s = new StringBuilder();

        s.append("Hardware: " + Build.HARDWARE + "\n");
        s.append("Product: " + Build.PRODUCT + "\n");
        s.append("Device manufacturer: " + Build.MANUFACTURER + "\n");
        s.append("Device brand: " + Build.BRAND + "\n");
        s.append("Device model: " + Build.MODEL + "\n");
        s.append("Device host: " + Build.HOST + "\n");
        s.append("Android SDK: " + Build.VERSION.SDK_INT + "\n");
        s.append("Android Release: " + Build.VERSION.RELEASE + "\n");
        s.append("ROM: " + Build.DISPLAY);

        details.setText(s.toString());

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
                ClipData clip = ClipData.newPlainText("Clipboard",
                        details.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
