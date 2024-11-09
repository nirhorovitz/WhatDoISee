package com.example.whatdoisee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import java.util.Objects;


public class SettingsActivity extends AppCompatActivity {
    protected static final int MINIMAL_LENGTH_TO_TARGET = 0;
    protected static final int MAXIMAL_LENGTH_TO_TARGET = 40;
    protected static final int DISTANCE_RESOLUTION = 50; // 100
    protected static final int BATCH_SIZE = 10; // 100
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_frame, settingsFragment).commit();
        getSupportFragmentManager().executePendingTransactions();

        Preference reset = settingsFragment.findPreference("reset");
        if (reset != null)
            reset.setOnPreferenceClickListener(this::resetDistances);

        Preference tutorial = settingsFragment.findPreference("tutorial");
        if (tutorial != null)
            tutorial.setOnPreferenceClickListener(this::goToTutorial);
    }

    public boolean goToTutorial(Preference preference) {
        startActivity(new Intent(SettingsActivity.this, TutorialActivity.class));
        return true;
    }

    public boolean resetDistances(@NonNull Preference preference) {
        ((SeekBarPreference) Objects.requireNonNull(settingsFragment.findPreference("maximum"))).setValue(MAXIMAL_LENGTH_TO_TARGET);
        ((SeekBarPreference) Objects.requireNonNull(settingsFragment.findPreference("minimum"))).setValue(MINIMAL_LENGTH_TO_TARGET);
        ((SeekBarPreference) Objects.requireNonNull(settingsFragment.findPreference("resolution"))).setValue(DISTANCE_RESOLUTION);
        ((SeekBarPreference) Objects.requireNonNull(settingsFragment.findPreference("batchSize"))).setValue(BATCH_SIZE);
        return true;
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int maximum = sharedPreferences.getInt("maximum", 40);
        int minimum = sharedPreferences.getInt("minimum", 1);
        if (minimum > maximum)
            Toast.makeText(this, "minimum can't be bigger than maximum", Toast.LENGTH_LONG).show();
        else
            super.onBackPressed();
    }
}
