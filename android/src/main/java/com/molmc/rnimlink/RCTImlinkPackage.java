package com.molmc.rnimlink;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hhe on 16/12/21.
 */

public class RCTImlinkPackage implements ReactPackage {

	@Override
	public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
		List<NativeModule> modules = new ArrayList<>();
		modules.add(new RCTImlinkModule(reactContext));
		return modules;
	}

	@Override
	public List<Class<? extends JavaScriptModule>> createJSModules() {
		return Collections.emptyList();
	}

	@Override
	public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
		return Collections.emptyList();
	}
}
