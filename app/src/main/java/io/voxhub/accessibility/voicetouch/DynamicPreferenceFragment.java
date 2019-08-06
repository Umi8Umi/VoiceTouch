/*public class PreferenceFormFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstance, String rootPreferenceKey) {
        Context activityContext = getActivity();
        
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(activityContext);
        setPreferenceScreen(preferenceScreen);

        TypedValue themeTypedValue = new TypedValue();
        activityContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);
        
        // We instance each Preference using our ContextThemeWrapper object
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle("Category test");

        EditTextPreference editTextPreference = new EditTextPreference(contextThemeWrapper);
        editTextPreference.setKey("edittext");
        editTextPreference.setTitle("EditText test");

        CheckBoxPreference checkBoxPreference = new CheckBoxPreference(contextThemeWrapper);
        checkBoxPreference.setTitle("Checkbox test");
        checkBoxPreference.setKey("checkbox");
        checkBoxPreference.setChecked(true);
        
        // It's REALLY IMPORTANT to add Preferences with child Preferences to the Preference Hierarchy first
        // Otherwise, the PreferenceManager will fail to load their keys
        
        // First we add the category to the root PreferenceScreen
        getPreferenceScreen().addPreference(preferenceCategory);

        // Then their child to it
        preferenceCategory.addPreference(editTextPreference);
        preferenceCategory.addPreference(checkBoxPreference);
    }
}*/
