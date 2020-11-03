package com.blaszt.socialmediasaver2.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceDataStore;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.data.Pair;

public class SortPreference extends Preference implements RadioGroup.OnCheckedChangeListener {
    private int mFlags;

    private RadioGroup mSortBy, mSortOrder;

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating. For example, a {@link CheckBoxPreference}
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * {@link CheckBoxPreference} class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @param defStyleRes  A resource identifier of a style resource that
     *                     supplies default values for the view, used only if
     *                     defStyleAttr is 0 or can not be found in the theme. Can be 0
     *                     to not look for defaults.
     * @see #SortPreference(Context, AttributeSet)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SortPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating. For example, a {@link CheckBoxPreference}
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * {@link CheckBoxPreference} class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #SortPreference(Context, AttributeSet)
     */
    public SortPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Constructor that is called when inflating a Preference from XML. This is
     * called when a Preference is being constructed from an XML file, supplying
     * attributes that were specified in the XML file. This version uses a
     * default style of 0, so the only attribute values applied are those in the
     * Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can
     *                access the current theme, resources, {@link SharedPreferences},
     *                etc.
     * @param attrs   The attributes of the XML tag that is inflating the
     *                preference.
     * @see #SortPreference(Context, AttributeSet, int)
     */
    public SortPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor to create a Preference.
     *
     * @param context The Context in which to store Preference values.
     */
    public SortPreference(Context context) {
        super(context);
    }

    /**
     * Binds the created View to the data for this Preference.
     * <p>
     * This is a good place to grab references to custom Views in the layout and
     * set properties on them.
     * <p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param view The View that shows this Preference.
     * @see #onCreateView(ViewGroup)
     */
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        setupSortRadioBtn(view);
        checkSortRadioBtn(mFlags);
    }

    /**
     * Creates the View to be shown for this Preference in the
     * {@link PreferenceActivity}. The default behavior is to inflate the main
     * layout of this Preference (see {@link #setLayoutResource(int)}. If
     * changing this behavior, please specify a {@link ViewGroup} with ID
     * {@link android.R.id#widget_frame}.
     * <p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param parent The parent that this View will eventually be attached to.
     * @return The View that displays this Preference.
     * @see #onBindView(View)
     */
    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.preference_sort, parent, false);
    }

    /**
     * Called when a Preference is being inflated and the default value
     * attribute needs to be read. Since different Preference types have
     * different value types, the subclass should get and return the default
     * value which will be its value type.
     * <p>
     * For example, if the value type is String, the body of the method would
     * proxy to {@link TypedArray#getString(int)}.
     *
     * @param a     The set of attributes.
     * @param index The index of the default value attribute.
     * @return The default value of this preference type.
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return parseValueToInt(a.getString(index));
    }

    /**
     * Implement this to set the initial value of the Preference.
     * <p>
     * <p>If <var>restorePersistedValue</var> is true, you should restore the
     * Preference value from the {@link SharedPreferences}. If
     * <var>restorePersistedValue</var> is false, you should set the Preference
     * value to defaultValue that is given (and possibly store to SharedPreferences
     * if {@link #shouldPersist()} is true).
     * <p>
     * <p>In case of using {@link PreferenceDataStore}, the <var>restorePersistedValue</var> is
     * always {@code true}. But the default value (if provided) is set.
     * <p>
     * <p>This may not always be called. One example is if it should not persist
     * but there is no default value given.
     *
     * @param restorePersistedValue True to restore the persisted value;
     *                              false to use the given <var>defaultValue</var>.
     * @param defaultValue          The default value for this Preference. Only use this
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setSortFlags(restorePersistedValue ? getPersistedInt(mFlags) : (int) defaultValue);
    }

    public int getSortFlags() {
        return mFlags;
    }

    public void setSortFlags(int flags) {
        mFlags = flags;

        persistInt(flags);
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
}
