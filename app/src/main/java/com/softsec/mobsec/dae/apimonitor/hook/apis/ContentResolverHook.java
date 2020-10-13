package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.net.Uri;
import android.text.TextUtils;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ContentResolverHook extends Hook {

	public static final String TAG = "DAEAM_ContentResolver";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		logger.setTag(TAG);

		Method queryMethod = Reflector.findMethod(ContentResolver.class, "query",
				Uri.class, String[].class, String.class, String[].class, String.class);
		methodHookImpl.hookMethod(queryMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				if (isSensitiveUri(uri)) {
					String queryStr = concatenateQuery(uri, (String[]) param.args[1], (String) param.args[2], (String[]) param.args[3],
							(String) param.args[4]);
					logger.recordAPICalling(param, "查询数据库", "URI", uri.toString(), "querySQL", queryStr);
				}
			}
		});

		Method registerContentObserverMethod = Reflector.findMethod(ContentResolver.class,
				"registerContentObserver", Uri.class, boolean.class, ContentObserver.class, int.class);
		methodHookImpl.hookMethod(registerContentObserverMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				if (isSensitiveUri(uri)) logger.recordAPICalling(param,
						"监听数据库","URI", uri.toString(),
						"ClassName", param.args[1].getClass().toString());
			}
		});

		Method insertMethod = Reflector.findMethod(ContentResolver.class, "insert",
				Uri.class, ContentValues.class);
		methodHookImpl.hookMethod(insertMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				if (isSensitiveUri(uri)) {
					String insertStr = concatenateInsert(uri, (ContentValues) param.args[1]);
					logger.recordAPICalling(param, "向数据库添加数据", "URI", uri.toString(), "insertSQL", insertStr);
				}
			}
		});

		Method deletemethod = Reflector.findMethod(ContentResolver.class, "delete",
				Uri.class, String.class, String[].class);
		methodHookImpl.hookMethod(deletemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];

				if (isSensitiveUri(uri)) {
					String deleteStr = concatenateDelete(uri, (String) param.args[1], (String[]) param.args[2]);
					logger.recordAPICalling(param, "删除数据库数据",  "URI", uri.toString(), "deleteSQL", deleteStr);
				}
			}

		});

		Method updatemethod = Reflector.findMethod(ContentResolver.class, "update",
				Uri.class, ContentValues.class, String.class, String[].class);
		methodHookImpl.hookMethod(updatemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Uri uri = (Uri) param.args[0];
				if (isSensitiveUri(uri)) {
					String updateStr = concatenateUpdate(uri, (ContentValues) param.args[1], (String) param.args[2], (String[]) param.args[3]);
					logger.recordAPICalling(param, "更新数据库数据", "URI", uri.toString(), "updateSQL", updateStr);
				}
			}
		});
	}

	private static final String[] privacyUris = { "content://com.android.contacts", "content://sms", "content://mms-sms", "content://contacts/",
			"content://call_log", "content://browser/bookmarks" };

	private boolean isSensitiveUri(Uri uri) {
		String url = uri.toString().toLowerCase();
		for (int i = 0; i < privacyUris.length; i++) {
			if (url.startsWith(privacyUris[i])) {
				return true;
			}
		}
		return false;
	}

	private String concatenateStringArray(String[] array, String splitstr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i == array.length - 1)
				sb.append(array[i]);
			else
				sb.append(array[i] + splitstr);
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
		if (!TextUtils.isEmpty(sortOrder))
			sb.append(" order by " + sortOrder);
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
			if (i == keysArray.length - 1)
				sb.append(" " + cv.get(keysArray[i]));
			else
				sb.append(" " + cv.get(keysArray[i]) + ",");
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
			if (selectionArgs == null)
				sb.append(selection);
			else {
				String selectstr = selection;
				for (int i = 0; i < selectionArgs.length; i++) {
					selectstr = selectstr.replaceFirst("/?", selectionArgs[i]);
				}
				sb.append(selectstr);
			}
		}
		return sb.toString();
	}

	private String concatenateUpdate(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append(" update ");
		sb.append("[" + uri.toString() + "]");
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
			if (selectionArgs == null)
				sb.append(selection);
			else {
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

