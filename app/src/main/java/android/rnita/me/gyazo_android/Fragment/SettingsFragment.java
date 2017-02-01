package android.rnita.me.gyazo_android.Fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.rnita.me.gyazo_android.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
