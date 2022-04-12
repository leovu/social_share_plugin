package com.cygnati.social_share_plugin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * SocialSharePlugin
 */
public class SocialSharePlugin
        implements FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.ActivityResultListener {
    private final static String TWITTER_PACKAGE_NAME = "com.twitter.android";

    private final static int TWITTER_REQUEST_CODE = 0xc0ce;

    private Activity activity;
    private MethodChannel channel;

    public SocialSharePlugin() {}

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.channel = new MethodChannel(binding.getBinaryMessenger(), "social_share_plugin");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        binding.addActivityResultListener(this);
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        binding.removeActivityResultListener(this);
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "social_share_plugin");
        final SocialSharePlugin plugin = new SocialSharePlugin();
        plugin.channel = channel;
        plugin.activity = registrar.activity();
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TWITTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d("SocialSharePlugin", "Twitter share done.");
                channel.invokeMethod("onSuccess", null);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("SocialSharePlugin", "Twitter cancelled.");
                channel.invokeMethod("onCancel", null);

            }

            return true;
        }
        return true;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        final PackageManager pm = activity.getPackageManager();
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "shareToTwitterLink":
                try {
                    pm.getPackageInfo(TWITTER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                    twitterShareLink(call.<String>argument("text"), call.<String>argument("url"));
                    result.success(true);
                } catch (PackageManager.NameNotFoundException e) {
                    openPlayStore(TWITTER_PACKAGE_NAME);
                    result.success(false);
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void openPlayStore(String packageName) {
        try {
            final Uri playStoreUri = Uri.parse("market://details?id=" + packageName);
            final Intent intent = new Intent(Intent.ACTION_VIEW, playStoreUri);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            final Uri playStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
            final Intent intent = new Intent(Intent.ACTION_VIEW, playStoreUri);
            activity.startActivity(intent);
        }
    }

    private void twitterShareLink(String text, String url) {
        final String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s&url=%s", text, url);
        final Uri uri = Uri.parse(tweetUrl);
        activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, uri), TWITTER_REQUEST_CODE);
    }
}
