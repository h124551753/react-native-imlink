package com.molmc.rnimlink;

import android.os.AsyncTask;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.molmc.rnimlink.esptouch.EsptouchTask;
import com.molmc.rnimlink.esptouch.IEsptouchListener;
import com.molmc.rnimlink.esptouch.IEsptouchResult;
import com.molmc.rnimlink.esptouch.IEsptouchTask;
import com.molmc.rnimlink.esptouch.wifi.EspWifiAdmin;

import java.util.List;

/**
 * Created by hhe on 16/12/21.
 */

public class RCTImlinkModule extends ReactContextBaseJavaModule {

	private final ReactApplicationContext _reactContext;
	private EspWifiAdmin mWifiAdmin;
	private IEsptouchTask mEsptouchTask;


	public RCTImlinkModule(ReactApplicationContext reactContext) {
		super(reactContext);
		_reactContext = reactContext;
        mWifiAdmin = new EspWifiAdmin(reactContext);
	}

	@Override
	public String getName() {
		return "ImLink";
	}


	@ReactMethod
	public void getSsid(Callback callback){
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
		callback.invoke(apSsid);
	}

	@ReactMethod
	public void stop(){
		if (mEsptouchTask != null){
			mEsptouchTask.interrupt();
		}
	}

	@ReactMethod
	public void start(final ReadableMap options, final Promise promise){
		String apSsid = options.getString("ssid");
		String apPassword = options.getString("password");
		String apBssid = mWifiAdmin.getWifiConnectedBssid();
		String isSsidHiddenStr = "NO";
		int taskCount = options.getInt("count");
		if (taskCount>1){
			new EsptouchAsyncTaskMulti(promise).execute(apSsid, apBssid, apPassword,
					isSsidHiddenStr, String.valueOf(taskCount));
		}else{
			new EsptouchAsyncTaskSingle(promise).execute(apSsid, apBssid, apPassword,
					isSsidHiddenStr);
		}
	}

	private class EsptouchAsyncTaskSingle extends AsyncTask<String, Void, IEsptouchResult> {

		private Promise mPromise;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();

		public EsptouchAsyncTaskSingle(Promise promise){
			this.mPromise = promise;
		}

		@Override
		protected void onPreExecute() {
			//TODO pre execute
		}

		@Override
		protected IEsptouchResult doInBackground(String... params) {
			synchronized (mLock) {
				String apSsid = params[0];
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				boolean isSsidHidden = false;
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
						isSsidHidden, getCurrentActivity());
			}
			IEsptouchResult result = mEsptouchTask.executeForResult();
			return result;
		}

		@Override
		protected void onPostExecute(IEsptouchResult result) {
			Boolean resolved = false;
			WritableMap map = Arguments.createMap();

			// it is unnecessary at the moment, add here just to show how to use isCancelled()
			if (!result.isCancelled()) {
				if (result.isSuc()) {
					resolved = true;
					map.putString("bssid", result.getBssid());
					map.putString("ipv4", result.getInetAddress().getHostAddress());
				} else {

				}
			}

			if (resolved){
				mPromise.resolve(map);
			}else{
				mPromise.reject("900", "Error run Imlink");
			}
		}
	}

	private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
		//TODO show multi config result

	}

	private IEsptouchListener myListener = new IEsptouchListener() {

		@Override
		public void onEsptouchResultAdded(final IEsptouchResult result) {
			onEsptoucResultAddedPerform(result);
		}
	};

	private class EsptouchAsyncTaskMulti extends AsyncTask<String, Void, List<IEsptouchResult>> {

		private Promise mPromise;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();

		public EsptouchAsyncTaskMulti(Promise promise){
			this.mPromise = promise;
		}

		@Override
		protected void onPreExecute() {
			//TODO pre execute

		}

		@Override
		protected List<IEsptouchResult> doInBackground(String... params) {
			int taskResultCount = -1;
			synchronized (mLock) {
				// !!!NOTICE
				String apSsid = mWifiAdmin.getWifiConnectedSsidAscii(params[0]);
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				String taskResultCountStr = params[4];
				boolean isSsidHidden = false;
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
						isSsidHidden, getCurrentActivity());
				mEsptouchTask.setEsptouchListener(myListener);
			}
			List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
			return resultList;
		}

		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {
			IEsptouchResult firstResult = result.get(0);

			WritableArray ret = Arguments.createArray();

			Boolean resolved = false;

			// check whether the task is cancelled and no results received
			if (!firstResult.isCancelled()) {
				// max results to be displayed, if it is more than maxDisplayCount,
				// just show the count of redundant ones
				// the task received some results including cancelled while
				// executing before receiving enough results
				if (firstResult.isSuc()) {
					StringBuilder sb = new StringBuilder();
					for (IEsptouchResult resultInList : result) {
						if(!resultInList.isCancelled() &&resultInList.getBssid() != null) {
							WritableMap map = Arguments.createMap();
							map.putString("bssid", resultInList.getBssid());
							map.putString("ipv4", resultInList.getInetAddress().getHostAddress());
							ret.pushMap(map);
							resolved = true;
							if (!resultInList.isSuc())
								break;
						}
					}
				} else {

				}
			}

			if(resolved) {
				mPromise.resolve(ret);
			} else {
				mPromise.reject("900", "Error run Imlink");
			}

		}
	}
}
