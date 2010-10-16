package org.jtb.alogcat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class Prefs {
	private Context context = null;

	public Prefs(Context context) {
		this.context = context;
	}

	private String getString(String key, String def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String s = prefs.getString(key, def);
		return s;
	}

	private int getInt(String key, int def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int i = Integer.parseInt(prefs.getString(key, Integer.toString(def)));
		return i;
	}

	private float getFloat(String key, float def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		float f = Float.parseFloat(prefs.getString(key, Float.toString(def)));
		return f;
	}

	private long getLong(String key, long def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		long l = Long.parseLong(prefs.getString(key, Long.toString(def)));
		return l;
	}

	private void setString(String key, String val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putString(key, val);
		e.commit();
	}

	private void setBoolean(String key, boolean val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putBoolean(key, val);
		e.commit();
	}

	private void setInt(String key, int val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putString(key, Integer.toString(val));
		e.commit();
	}

	private void setLong(String key, long val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor e = prefs.edit();
		e.putString(key, Long.toString(val));
		e.commit();
	}

	private boolean getBoolean(String key, boolean def) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean b = prefs.getBoolean(key, def);
		return b;
	}

	private int[] getIntArray(String key, String def) {
		String s = getString(key, def);
		int[] ia = new int[s.length()];
		for (int i = 0; i < s.length(); i++) {
			ia[i] = s.charAt(i) - '0';
		}
		return ia;
	}
	
	public Level getLevel() {
		return Level.valueOf(getString("level", "V"));
	}

	public void setLevel(Level level) {
		setString("level", level.toString());
	}
	
	public Format getFormat() {
		String f =  getString("format", "BRIEF");
		
		// UPGRADE
		// can remove at some point
	
		if (!f.equals(f.toUpperCase())) {
			f = f.toUpperCase();
			setString("format", f);
		}
		
		return Format.valueOf(f);
	}
	
	public void setFormat(Format format) {
		setString("format", format.toString());
	}

	public Buffer getBuffer() {
		return Buffer.valueOf(getString("buffer", "MAIN"));
	}

	public void setBuffer(Buffer buffer) {
		setString("buffer", buffer.toString());
	}

	public Textsize getTextsize() {
		return Textsize.valueOf(getString("textsize", "MEDIUM"));
	}

	public void setTextsize(Textsize textsize) {
		setString("textsize", textsize.toString());
	}
	
	public String getFilter() {
		return getString("filter", null);
	}
	
	public void setFilter(String filter) {
		setString("filter", filter);
	}
	
	public boolean isAutoScroll() {
		return getBoolean("autoScroll", true);
	}
	
	public void setAutoScroll(boolean autoScroll) {
		setBoolean("autoScroll", autoScroll);
	}
	
	public int getBackgroundColor() {
		String c = getString("backgroundColor", "#ffffff");
		int color = Color.parseColor(c);
		return color;
	}
	
	public boolean isEmailHtml() {
		boolean b = getBoolean("emailHtml", false);
		return b;
	}
}
