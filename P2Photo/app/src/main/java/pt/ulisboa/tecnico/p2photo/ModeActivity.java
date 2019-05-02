package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import pt.ulisboa.tecnico.p2photo.wifi.MainActivityWifi;

public class ModeActivity extends AppCompatActivity {
    GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
        DataHolder.getInstance();

        Button wifi = (Button) findViewById(R.id.wifiButton);
        wifi.setOnClickListener(v -> {
            Intent intent = new Intent(ModeActivity.this, MainActivityWifi.class);
            startActivity(intent);
        });

    }

    public void mainAtivity(View view) {
        Intent intent = new Intent(ModeActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
