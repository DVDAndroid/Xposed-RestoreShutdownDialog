/*
 * Copyright 2016 dvdandroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dvd.android.xposed.restoreshutdowndialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.XResources;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public static String CLASS_GLOBAL_ACTIONS;
    public static String CLASS_GLOBAL_POWER_ACTIONS;
    private Context mContext;
    private Object mWindowManagerFuncs;
    private String powerOffString;
    private String shutdownConfirmString;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        if (Build.VERSION.SDK_INT > 23) { // for context.getSharedPreferences("", MODE_WORLD_READABLE) disabled from nougat.
            findAndHookMethod("android.app.ContextImpl", null, "checkMode", int.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return null;
                }
            });
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (!resParam.packageName.equals("android")) return;

        XResources r = resParam.res;
        powerOffString = r.getString(r.getIdentifier("power_off", "string", "android"));
        shutdownConfirmString = r.getString(r.getIdentifier("shutdown_confirm", "string", "android"));
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!(lpparam.packageName.equals("android"))) return;

        if (Build.VERSION.SDK_INT >= 26)
            CLASS_GLOBAL_ACTIONS = "com.android.server.policy.LegacyGlobalActions";
        else if (Build.VERSION.SDK_INT >= 23)
            CLASS_GLOBAL_ACTIONS = "com.android.server.policy.GlobalActions";
        else
            CLASS_GLOBAL_ACTIONS = "com.android.internal.policy.impl.GlobalActions";

        CLASS_GLOBAL_POWER_ACTIONS = CLASS_GLOBAL_ACTIONS + ".PowerAction";

        final Class<?> globalActionsClass = XposedHelpers.findClass(CLASS_GLOBAL_ACTIONS, lpparam.classLoader);

        try {
            XposedBridge.hookAllConstructors(globalActionsClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    mContext = (Context) param.args[0];
                    mWindowManagerFuncs = param.args[1];
                }
            });

            findAndHookMethod(CLASS_GLOBAL_POWER_ACTIONS, lpparam.classLoader, "onPress", getMethodReplacement());
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private XC_MethodReplacement getMethodReplacement() {
        String display = Build.DISPLAY;

        if (display.contains("cm")) {
            // CM BASED ROM
            return new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(
                        MethodHookParam methodHookParam) throws Throwable {
                    final boolean quickbootEnabled = Settings.System.getInt(mContext.getContentResolver(), "enable_quickboot", 0) == 1;
                    // go to quickboot mode if enabled
                    if (quickbootEnabled) {
                        // TODO: is working?
                        XposedHelpers.callMethod(methodHookParam.thisObject, "startQuickBoot");
                        return null;
                    }

                    showShutdownDialog();
                    return null;
                }
            };
        } else {
            // AOSP BASED ROM
            return new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    XposedHelpers.callMethod(mWindowManagerFuncs, "shutdown", true);
                    return null;
                }
            };
        }
    }

    private void showShutdownDialog() throws PackageManager.NameNotFoundException {
        if (mContext == null)
            return;

        final Context context = mContext.createPackageContext(getClass().getPackage().getName(), Context.CONTEXT_IGNORE_SECURITY);

        AlertDialog sConfirmDialog = new AlertDialog.Builder(context)
                .setTitle(powerOffString)
                .setMessage(shutdownConfirmString)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        XposedHelpers.callMethod(mWindowManagerFuncs, "shutdown", false);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();

        sConfirmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        sConfirmDialog.show();
    }
}