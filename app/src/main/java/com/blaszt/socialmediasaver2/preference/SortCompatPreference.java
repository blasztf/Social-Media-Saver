package com.blaszt.socialmediasaver2.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.data.Pair;

public class SortCompatPreference extends DialogPreference implements RadioGroup.OnCheckedChangeListener, SMSDialogPreference {
    private int mFlags;

    private RadioGroup mSortBy, mSortOrder;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SortCompatPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ctor();
    }

    public SortCompatPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctor();
    }

    public SortCompatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctor();
    }

    private void ctor() {
        setDialogLayoutResource(R.layout.preference_compat_sort);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return parseValueToInt(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
//        setSortFlags(restorePersistedValue ? getPersistedInt(mFlags) : (int) defaultValue);
        setSortFlags(getPersistedInt(mFlags));
        setSummary(parseIntToValue(mFlags));
    }

    public int getSortFlags() {
        return mFlags;
    }

    public void setSortFlags(int flags) {
        mFlags = flags;

        persistInt(flags);
    }

    private String parseIntToValue(int flags) {
        StringBuilder value = new StringBuilder();
        if ((flags & Pair.BY_NAME) != 0) {
            value.append("By Name");
        }
        else if ((flags & Pair.BY_SIZE) != 0) {
            value.append("By Size");
        }
        else if ((flags & Pair.BY_TYPE) != 0) {
            value.append("By Type");
        }
        else {
            value.append("By Date");
        }

        value.append(" & ");

        if ((flags & Pair.ORDER_ASCENDING) != 0) {
            value.append("Ascending");
        }
        else {
            value.append("Descending");
        }

        return value.toString();
    }

    private int parseValueToInt(String value) {
        int[] newValues = new int[]{Pair.BY_DATE, Pair.ORDER_DESCENDING};
        if (value != null) {
            String[] values = value.split("\\|");
            if (values.length == 2) {
                if (values[0].equals("byName")) {
                    newValues[0] = Pair.BY_NAME;
                } else if (values[0].equals("byDate")) {
                    newValues[0] = Pair.BY_DATE;
                } else if (values[0].equals("bySize")) {
                    newValues[0] = Pair.BY_SIZE;
                } else if (values[0].equals("byType")) {
                    newValues[0] = Pair.BY_TYPE;
                } else if (values[0].equals("ascending")) {
                    newValues[1] = Pair.ORDER_ASCENDING;
                } else if (values[0].equals("descending")) {
                    newValues[1] = Pair.ORDER_DESCENDING;
                }

                if (values[1].equals("byName")) {
                    newValues[0] = Pair.BY_NAME;
                } else if (values[1].equals("byDate")) {
                    newValues[0] = Pair.BY_DATE;
                } else if (values[1].equals("bySize")) {
                    newValues[0] = Pair.BY_SIZE;
                } else if (values[1].equals("byType")) {
                    newValues[0] = Pair.BY_TYPE;
                } else if (values[1].equals("ascending")) {
                    newValues[1] = Pair.ORDER_ASCENDING;
                } else if (values[1].equals("descending")) {
                    newValues[1] = Pair.ORDER_DESCENDING;
                }
            }
        }

        return newValues[0] | newValues[1];
    }

    private void checkSortRadioBtn(int flags) {
        switch (flags & (Pair.BY_NAME | Pair.BY_DATE | Pair.BY_SIZE | Pair.BY_TYPE)) {
            case Pair.BY_NAME:
                mSortBy.check(R.id.sortByName);
                break;
            case Pair.BY_DATE:
                mSortBy.check(R.id.sortByDate);
                break;
            case Pair.BY_SIZE:
                mSortBy.check(R.id.sortBySize);
                break;
            case Pair.BY_TYPE:
                mSortBy.check(R.id.sortByType);
                break;
        }

        switch (flags & (Pair.ORDER_ASCENDING | Pair.ORDER_DESCENDING)) {
            case Pair.ORDER_ASCENDING:
                mSortOrder.check(R.id.sortOrderAsc);
                break;
            case Pair.ORDER_DESCENDING:
                mSortOrder.check(R.id.sortOrderDesc);
                break;
        }
    }

    private void setupSortRadioBtn(View view) {
        mSortBy = view.findViewById(R.id.sortByGroup);
        mSortBy.setOnCheckedChangeListener(this);

        mSortOrder = view.findViewById(R.id.sortOrderGroup);
        mSortOrder.setOnCheckedChangeListener(this);
    }

    private void setSortBy(int flagBy) {
        mFlags &= (Pair.ORDER_ASCENDING | Pair.ORDER_DESCENDING);
        mFlags |= flagBy;
        setSortFlags(mFlags);
    }

    private void setSortOrder(int flagOrder) {
        mFlags &= (Pair.BY_NAME | Pair.BY_DATE | Pair.BY_SIZE | Pair.BY_TYPE);
        mFlags |= flagOrder;
        setSortFlags(mFlags);
    }

    /**
     * <p>Called when the checked radio button has changed. When the
     * selection is cleared, checkedId is -1.</p>
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.sortByName:
                setSortBy(Pair.BY_NAME);
                break;
            case R.id.sortByDate:
                setSortBy(Pair.BY_DATE);
                break;
            case R.id.sortBySize:
                setSortBy(Pair.BY_SIZE);
                break;
            case R.id.sortByType:
                setSortBy(Pair.BY_TYPE);
                break;
            case R.id.sortOrderAsc:
                setSortOrder(Pair.ORDER_ASCENDING);
                break;
            case R.id.sortOrderDesc:
                setSortOrder(Pair.ORDER_DESCENDING);
                break;
        }
    }

    @Override
    public void displayPreferenceDialog(PreferenceFragmentCompat fragment) {
        DialogFragment dialogFragment = DialogPreferenceFragment.newInstance(this);
        dialogFragment.setTargetFragment(fragment, 0);
        dialogFragment.show(fragment.getFragmentManager(), null);
    }

    public static class DialogPreferenceFragment extends PreferenceDialogFragmentCompat {
        private SortCompatPreference mInternal;

        private static DialogPreferenceFragment newInstance(DialogPreference dialogPreference) {
            final DialogPreferenceFragment fragment = new DialogPreferenceFragment();
            final Bundle bundle = new Bundle(1);
            bundle.putString(ARG_KEY, dialogPreference.getKey());
            fragment.setArguments(bundle);
            fragment.setInternal(dialogPreference);
            return fragment;
        }

        private void setInternal(DialogPreference preference) {
            mInternal = (SortCompatPreference) preference;
        }

        private SortCompatPreference getInternal() {
            return mInternal;
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            builder.setPositiveButton(null, null).setNegativeButton(null, null);
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            getInternal().setupSortRadioBtn(view);
            getInternal().checkSortRadioBtn(getInternal().mFlags);
        }

        @Override
        public void onDialogClosed(boolean b) {
            getInternal().setSummary(getInternal().parseIntToValue(getInternal().mFlags));
            setInternal(null);
        }
    }
}
