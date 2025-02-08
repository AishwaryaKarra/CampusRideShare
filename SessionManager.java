package com.zion.uniride.util; 
import android.content.Context; 
import android.content.SharedPreferences; 
public class SessionManager { 
    private static final String PREF_NAME = "UniRidePrefs"; 
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; 
    private static final String KEY_USER_ROLE = "userRole"; 
 
    private static SessionManager instance; 
    private final SharedPreferences prefs; 
    private SessionManager(Context context) { 
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); 
    } 
    public static SessionManager getInstance(Context context) { 
        if (instance == null) { 
            instance = new SessionManager(context); 
        } 
        return instance; 
    } 
 
    public String getUserRole() { 
        return prefs.getString(KEY_USER_ROLE, "unknown"); 
    } 
 
    public boolean isUserLoggedIn() { 
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false); 
    } 
 
    public void setUserLoggedIn(boolean isLoggedIn) { 
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply(); 
    } 
 
    public void setUserRole(String userRole) { 
        prefs.edit().putString(KEY_USER_ROLE, userRole).apply(); 
    } 
} 
