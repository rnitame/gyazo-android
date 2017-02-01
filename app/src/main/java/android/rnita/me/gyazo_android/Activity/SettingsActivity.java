package android.rnita.me.gyazo_android.Activity;

import android.os.Bundle;
import android.rnita.me.gyazo_android.Fragment.SettingsFragment;
import android.rnita.me.gyazo_android.R;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction().replace(R.id.settingFrame,
                new SettingsFragment()).commit();
    }
}
