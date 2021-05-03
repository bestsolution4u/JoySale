package com.hitasoft.app.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.hitasoft.app.utils.Constants;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class LocaleManager {

    private static final String TAG = LocaleManager.class.getSimpleName();

    public static Context setLocale(Context c) {
        return updateResources(c, getLanguageCode(c));
    }

    public static Context setNewLocale(Context c, String languageCode, boolean isSavePref) {
        if (isSavePref)
            saveLanguagePref(c, languageCode);
        return updateResources(c, languageCode);
    }

    public static String getLanguageCode(Context c) {
        Constants.pref = c.getSharedPreferences("JoysalePref", MODE_PRIVATE);
        return Constants.pref.getString(Constants.PREF_LANGUAGE_CODE, Constants.LANGUAGE_CODE);
    }

    @SuppressLint("ApplySharedPref")
    private static void saveLanguagePref(Context c, String languageCode) {
        Constants.pref = c.getSharedPreferences("JoysalePref", MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        Constants.editor.putString(Constants.PREF_LANGUAGE_CODE, languageCode).commit();
    }


    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            config.setLayoutDirection(config.locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }

    public static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
    }

    public static boolean isRTL(Context context) {
        Locale primaryLocale = getLocale(context.getResources());
        boolean isTRL = (("" + primaryLocale).equals("ar") || Locale.getDefault().getLanguage().equals("ar") || Locale.getDefault().getLanguage().equals("ur"));
        return isTRL;
    }
}