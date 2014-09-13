package com.emmaguy.cleanstatusbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.emmaguy.cleanstatusbar.prefs.TimePreference;


public class MainActivity extends Activity {
    public static final String PREFS_KEY_API_VALUE = "api_level";
    public static final String PREFS_KEY_CLOCK_TIME = "clock_time";
    public static final String PREFS_KEY_BACKGROUND_COLOUR = "background_colour";

    private static final String PREFS_KEY_IS_RUNNING = "clean_status_bar_is_active";

    public static final int VERSION_CODE_L = 21; // TODO: change to Build.VERSION_CODES.L when it's released

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);

            initialiseClickListener(PREFS_KEY_IS_RUNNING);

            updateClockTimeTitle();

            initSummary();
        }

        private void updateClockTimeTitle() {
            Preference clockTime = findPreference(PREFS_KEY_CLOCK_TIME);
            if (clockTime != null) {
                clockTime.setTitle(getString(R.string.clock_time) + " - " + getPreferenceManager().getSharedPreferences().getString(PREFS_KEY_CLOCK_TIME, TimePreference.DEFAULT_TIME_VALUE));
            }
        }

        private void initialiseClickListener(String key) {
            Preference resetPref = findPreference(key);
            if (resetPref != null) {
                resetPref.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            toggleCleanStatusBarService();
        }

        @Override
        public void onPause() {
            super.onPause();

            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(PREFS_KEY_IS_RUNNING)) {
                toggleCleanStatusBarService();
                return true;
            }
            return false;
        }

        private boolean isCleanStatusBarRunning() {
            return ((CheckBoxPreference) findPreference(PREFS_KEY_IS_RUNNING)).isChecked();
        }

        private void toggleCleanStatusBarService() {
            if (isCleanStatusBarRunning()) {
                startService();
            } else {
                stopService();
            }
        }

        private void stopService() {
            getActivity().stopService(new Intent(getActivity(), CleanStatusBarService.class));
        }

        private void startService() {
            getActivity().startService(new Intent(getActivity(), CleanStatusBarService.class));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(findPreference(key));

            if (key.equals(PREFS_KEY_CLOCK_TIME)) {
                updateClockTimeTitle();
            }

            if (isCleanStatusBarRunning()) {
                stopService();
                startService();
            }
        }

        protected void initSummary() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initPrefsSummary(getPreferenceScreen().getPreference(i));
            }
        }

        protected void initPrefsSummary(Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initPrefsSummary(cat.getPreference(i));
                }
            } else {
                updatePrefsSummary(p);
            }
        }

        protected void updatePrefsSummary(Preference pref) {
            if (pref == null) {
                return;
            }

            if (pref instanceof ListPreference) {
                ListPreference lst = (ListPreference) pref;
                String currentValue = lst.getValue();

                int index = lst.findIndexOfValue(currentValue);
                CharSequence[] entries = lst.getEntries();
                if (index >= 0 && index < entries.length) {
                    pref.setSummary(entries[index]);
                }
            }
        }
    }
}
