package com.kalengo.weathermeida.plugins;

import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Handler;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.kalengo.weathermeida.ui.UpdateWidgetService;
import com.kalengo.weathermeida.util.Constants;
import com.kalengo.weathermeida.util.UrlUtil;

/**
 * @author Administrator 保存前端数据和前端获取数据
 */
public class DataPlugin extends CordovaPlugin {

	public static String SAVE = "save";
	public static String GET = "get";
	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

		@Override
		public void gotResult(int code, String alias, Set<String> tags) {
			String logs;
			switch (code) {
			case 0:
				logs = "Set tag and alias success" + alias;
				UrlUtil.info("Jpush", logs);
				break;

			case 6002:
				logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
				UrlUtil.info("daid", logs);
				break;

			default:
				logs = "Failed with errorCode = " + code;
				UrlUtil.info("david", logs);
			}

			// JpushUtil.showToast(logs, cordova.getActivity());
		}

	};

	private static final int MSG_SET_ALIAS = 1001;
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SET_ALIAS:
				UrlUtil.info("david", "Set alias in handler.");
				String c = (String) msg.obj;
				if (!UrlUtil.isDebug()) {
					UpdateWidgetService.city = c;
				}
				Intent intent = new Intent(cordova.getActivity(),
						UpdateWidgetService.class);
				cordova.getActivity().startService(intent);
				JPushInterface.setAliasAndTags(cordova.getActivity(),
						(String) msg.obj, null, mAliasCallback);
				break;

			default:
				UrlUtil.info("david", "Unhandled msg - " + msg.what);
			}
		}
	};

	/**
	 * web端传入数据时调用
	 * 
	 * @throws JSONException
	 */
	public boolean execute(String action, JSONArray data,
			CallbackContext callbackContext) throws JSONException {

		if (SAVE.equals(action)) {
			Constants.MAP.put(data.getString(0), data.get(1));
			// String currCity = data.getString(0);
			UrlUtil.info("david", "map--" + Constants.MAP.toString());
			String currCity = (String) Constants.MAP.get("city");
			UrlUtil.info("david", currCity);
			UrlUtil.info("david", Constants.CITY == null ? "0" : "1");
			if (Constants.CITY == null) {
				Constants.CITY = currCity;
				// Toast.makeText(this.cordova.getActivity(),
				// "city1--" + Constants.CITY, 0).show();
				if (Constants.CITY != null & !UrlUtil.isDebug()) {
					mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS,
							Constants.CITY));
					// Toast.makeText(this.cordova.getActivity(),
					// "city--" + Constants.CITY, 0).show();
				} else if (UrlUtil.isDebug()) {
					mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS,
							"Kalengo"));
				}
			} else if (!Constants.CITY.equals(currCity)) {
				Constants.CITY = currCity;
				Constants.CITYCHANGE = true;
				// } else {
				// Constants.CITYCHANGE = false;
			}

		} else if (GET.equals(action)) {
			String key = data.getString(0);
			UrlUtil.info("david", "get--" + key);
			// JSONObject json = new JSONObject();
			// json.put(key, Constants.MAP.get(key));
			Object value = Constants.MAP.get(key);
			if (value instanceof String) {
				callbackContext.success((String) Constants.MAP.get(key));
			} else if (value instanceof Integer) {
				callbackContext.success((Integer) Constants.MAP.get(key));
			} else if (value instanceof Byte) {
				callbackContext.success((Byte) Constants.MAP.get(key));
			} else if (value instanceof JSONArray) {
				callbackContext.success((JSONArray) Constants.MAP.get(key));
			} else if (value instanceof JSONObject) {
				callbackContext.success((JSONObject) Constants.MAP.get(key));
			}
			if (value == null) {
				callbackContext.success("");
			}
			return true;
		}
		return false;
	}
}
