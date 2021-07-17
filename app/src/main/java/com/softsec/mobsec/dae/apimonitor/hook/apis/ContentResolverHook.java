package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ContentResolverHook extends Hook {

	public static final String TAG = "ContentResolver";

	private static Map<String, String> privacyUriMap;

	static {
		privacyUriMap = new HashMap<>();
		privacyUriMap.put("content://com.android.contacts", "联系人");
		privacyUriMap.put("content://media/external/video/media", "视频");
		privacyUriMap.put("content://media/external/images/media", "图片");
		privacyUriMap.put("content://sms", "短信");
		privacyUriMap.put("content://mms-sms", "短信");
		privacyUriMap.put("content://contacts/", "联系人");
		privacyUriMap.put("content://call_log", "通话记录");
		privacyUriMap.put("content://browser/bookmarks", "浏览器书签");
		privacyUriMap.put("content://com.android.calendar", "日历信息");
	}

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {



		Method queryMethod = Reflector.findMethod(ContentResolver.class, "query",
				Uri.class, String[].class, String.class, String[].class, String.class);
		MethodHookHandler.hookMethod(queryMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				String privacyType = getPrivacyType(uri);
				if (!"".equals(privacyType)) {
					String queryStr = concatenateQuery(uri, (String[]) param.args[1], (String) param.args[2], (String[]) param.args[3],
							(String) param.args[4]);
					String[] callingInfo = getCallingInfo(param.method.getName());
					Cursor cursor = (Cursor)(param.getResult());
					StringBuilder sb = new StringBuilder();
					while(cursor.moveToNext()) {
						int len = cursor.getColumnCount();
						for(int i = 0; i < len; i++) {
							sb.append(cursor.getString(i)).append(',');
						}
						sb.append(';');
					}
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.addRelatedAttrs("result", sb.toString());
					logger.recordAPICalling(param, "本机数据库查询",
							"数据类型", privacyType,
							"URI", uri.toString(),
							"querySQL", queryStr);
				}
			}
		});

		Method registerContentObserverMethod = Reflector.findMethod(ContentResolver.class,
				"registerContentObserver", Uri.class, boolean.class, ContentObserver.class, int.class);
		MethodHookHandler.hookMethod(registerContentObserverMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				String privacyType = getPrivacyType(uri);
				if (!"".equals(privacyType)) {
					String[] callingInfo = getCallingInfo(param.method.getName());
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.recordAPICalling(param,
						"监听本机数据",
							"数据类型", privacyType,
							"URI", uri.toString(),
							"ClassName", param.args[1].getClass().toString());
				}
			}
		});

		Method insertMethod = Reflector.findMethod(ContentResolver.class, "insert",
				Uri.class, ContentValues.class);
		MethodHookHandler.hookMethod(insertMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				String privacyType = getPrivacyType(uri);
				if (!"".equals(privacyType)) {
					String insertStr = concatenateInsert(uri, (ContentValues) param.args[1]);
					String[] callingInfo = getCallingInfo(param.method.getName());
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.recordAPICalling(param, "本机数据库添加",
							"类型", privacyType,
							"URI", uri.toString(),
							"insertSQL", insertStr);
				}
			}
		});

		Method deletemethod = Reflector.findMethod(ContentResolver.class, "delete",
				Uri.class, String.class, String[].class);
		MethodHookHandler.hookMethod(deletemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];

				String privacyType = getPrivacyType(uri);
				if (!"".equals(privacyType)) {
					String deleteStr = concatenateDelete(uri, (String) param.args[1], (String[]) param.args[2]);
					String[] callingInfo = getCallingInfo(param.method.getName());
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.recordAPICalling(param, "本机数据库删除",
							"数据类型", privacyType,
							"URI", uri.toString(),
							"deleteSQL", deleteStr);
				}
			}

		});

		Method updatemethod = Reflector.findMethod(ContentResolver.class, "update",
				Uri.class, ContentValues.class, String.class, String[].class);
		MethodHookHandler.hookMethod(updatemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				String privacyType = getPrivacyType(uri);
				if (!"".equals(privacyType)) {
					String updateStr = concatenateUpdate(uri, (ContentValues) param.args[1], (String) param.args[2], (String[]) param.args[3]);
					String[] callingInfo = getCallingInfo(param.method.getName());
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.recordAPICalling(param, "本机数据库修改",
							"数据类型", privacyType,
							"URI", uri.toString(),
							"updateSQL", updateStr);
				}
			}
		});
	}

	private String getPrivacyType(Uri uri) {
		String url = uri.toString().toLowerCase();
		for(String key : privacyUriMap.keySet()) {
			if(url.startsWith(key)) {
				return privacyUriMap.get(key);
			}
		}
		return "";
	}

	private String concatenateStringArray(String[] array, String splitstr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i == array.length - 1) {
				sb.append(array[i]);
			} else {
				sb.append(array[i]).append(splitstr);
			}
		}
		return sb.toString();
	}

	private String concatenateQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		StringBuilder sb = new StringBuilder("select ");
		if (projection == null) {
			sb.append("* ");
		} else {
			sb.append(concatenateStringArray(projection, ","));
		}
		sb.append(" from [" + uri.toString() + "] ");
		if (!TextUtils.isEmpty(selection)) {
			sb.append(" where ");
			if (selectionArgs == null) {
				sb.append(selection);
			} else {
				String selectstr = selection;
				for (int i = 0; i < selectionArgs.length; i++) {
					selectstr = selectstr.replaceFirst("/?", selectionArgs[i]);
				}
				sb.append(selectstr);
			}
		}
		if (!TextUtils.isEmpty(sortOrder)) {
			sb.append(" order by " + sortOrder);
		}
		return sb.toString();
	}

	private String concatenateInsert(Uri uri, ContentValues cv) {
		StringBuilder sb = new StringBuilder();
		sb.append(" insert into ");
		sb.append("[" + uri.toString() + "]");
		sb.append(" ( ");
		String[] keysArray = new String[cv.size()];
		keysArray = this.getContentValuesKeySet(cv).toArray(keysArray);
		sb.append(concatenateStringArray(keysArray, ","));
		sb.append(" ) ");
		sb.append(" values (");
		for (int i = 0; i < keysArray.length; i++) {
			if (i == keysArray.length - 1) {
				sb.append(" " + cv.get(keysArray[i]));
			} else {
				sb.append(" " + cv.get(keysArray[i]) + ",");
			}
		}
		sb.append(" )");
		return sb.toString();
	}

	private String concatenateDelete(Uri uri, String selection, String[] selectionArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append(" delete from ");
		sb.append("[" + uri.toString() + "]");
		if (!TextUtils.isEmpty(selection)) {
			sb.append(" where ");
			if (selectionArgs == null) {
				sb.append(selection);
			} else {
				String selectstr = selection;
				for (String selectionArg : selectionArgs) {
					selectstr = selectstr.replaceFirst("/?", selectionArg);
				}
				sb.append(selectstr);
			}
		}
		return sb.toString();
	}

	private String concatenateUpdate(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append(" update ");
		sb.append("[").append(uri.toString()).append("]");
		sb.append(" set ");
		String[] keysArray = (String[]) this.getContentValuesKeySet(cv).toArray();
		for (int i = 0; i < keysArray.length; i++) {
			if (i == keysArray.length - 1) {
				sb.append(" " + keysArray[i] + "=" + cv.get(keysArray[i]));
			} else {
				sb.append(" " + keysArray[i] + "=" + cv.get(keysArray[i]) + ", ");
			}
		}
		if (!TextUtils.isEmpty(selection)) {
			sb.append(" where ");
			if (selectionArgs == null) {
				sb.append(selection);
			} else {
				String selectstr = selection;
				for (int i = 0; i < selectionArgs.length; i++) {
					selectstr = selectstr.replaceFirst("/?", selectionArgs[i]);
				}
				sb.append(selectstr);
			}
		}
		return sb.toString();
	}

	private Set<String> getContentValuesKeySet(ContentValues cv){
		HashMap<String, Object> mValue =  (HashMap<String, Object>) Reflector.getFieldOjbect(ContentValues.class, cv, "mValues");
		return mValue.keySet();
	}


}

