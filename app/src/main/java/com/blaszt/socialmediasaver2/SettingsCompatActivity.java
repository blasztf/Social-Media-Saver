package com.blaszt.socialmediasaver2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.blaszt.socialmediasaver2.logger.CrashCocoExceptionHandler;
import com.blaszt.socialmediasaver2.plugin.ModPlugin;
import com.blaszt.socialmediasaver2.plugin.ModPluginEngine;
import com.blaszt.socialmediasaver2.preference.SMSDialogPreference;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsCompatActivity extends AppCompatActivity {
    public static final String EXTRA_SHOW_INIT_FRAGMENT = SettingsCompatActivity.class.getName() + ".EXTRA_SHOW_INIT_FRAGMENT";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashCocoExceptionHandler("sms"));

        if (getIntent().getBooleanExtra(EXTRA_SHOW_INIT_FRAGMENT, false)) {
            getIntent().putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            getIntent().putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compat_settings);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragmentCompat.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_sort);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getActivity().getString(R.string.pref_key_storage)));
            setupViewMediaPreference();
            bindPreferenceSummaryToValue(findPreference(getActivity().getString(R.string.pref_key_view_media)));
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof SMSDialogPreference) {
                ((SMSDialogPreference) preference).displayPreferenceDialog(this);
            }
            else super.onDisplayPreferenceDialog(preference);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsCompatActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void setupViewMediaPreference() {
            CharSequence[][] entries = getPluginEntries();
            DropDownPreference preference = (DropDownPreference) findPreference(getActivity().getString(R.string.pref_key_view_media));
            preference.setEntries(entries[0]);
            preference.setEntryValues(entries[1]);
        }

        private CharSequence[][] getPluginEntries() {
            int size = ModPluginEngine.getInstance(getActivity()).each().size() + 1;
            CharSequence[] entries = new CharSequence[size];
            CharSequence[] entryValues = new CharSequence[size];
            int i = 0;
            entries[i] = "All";
            entryValues[i] = "all";
            for (ModPlugin plugin : ModPluginEngine.getInstance(getActivity()).each()) {
                entries[++i] = String.format("%s only", plugin.getName());
                entryValues[i] = plugin.getName();
            }
            return new CharSequence[][] { entries, entryValues };
        }
    }
}
