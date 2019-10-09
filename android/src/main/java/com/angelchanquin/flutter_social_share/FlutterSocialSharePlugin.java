package com.angelchanquin.flutter_social_share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.share.Sharer;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterSocialSharePlugin
 */
public class FlutterSocialSharePlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {

    private Activity activity;
    private static CallbackManager callbackManager;

    private FlutterSocialSharePlugin(MethodChannel channel, Activity activity) {
        this.activity = activity;
        channel.setMethodCallHandler(this);
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        callbackManager = CallbackManager.Factory.create();

        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_social_share");
        final FlutterSocialSharePlugin instance = new FlutterSocialSharePlugin(channel, registrar.activity());
        registrar.addActivityResultListener(instance);
        channel.setMethodCallHandler(instance);

    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {

        switch (call.method) {
            case "shareFacebook":
                shareToFacebook((String) call.argument("url"), (String) call.argument("msg"), result);
                break;
            case "shareFacebookPhoto":
                sharePhotoToFacebook((String) call.argument("uri"), (String) call.argument("title"), (String) call.argument("description"), result);
                break;
            case "shareFacebookVideo":
                shareVideoToFacebook((String) call.argument("uri"), (String) call.argument("title"), (String) call.argument("description"), result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }


    private ShareDialog getShareDialog(final Result flutterResult) {
        ShareDialog shareDialog = new ShareDialog(activity);

        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                flutterResult.success("success");
                System.out.println("--------------------success");
            }

            @Override
            public void onCancel() {
                flutterResult.error("canceled", "User Canceled", "The user canceled the operation");
                System.out.println("-----------------onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                flutterResult.error("error", error.getMessage(), error);
                System.out.println("---------------onError");
            }
        }, 10);
        return shareDialog;
    }
    /**
     * share to Facebook
     *
     * @param url    String
     * @param msg    String
     * @param result Result
     */
    private void shareToFacebook(String url, String msg, Result result) {

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .setQuote(msg)
                .build();

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareDialog.show(activity, content);
            result.success("success");
        }
    }

    private void shareVideoToFacebook(String uri, String title, String description, Result result) {

        Uri videoUrl = Uri.fromFile(new File(uri));
        ShareVideo video = new ShareVideo.Builder()
                .setLocalUrl(videoUrl)
                .build();

        ShareHashtag hashTag = new ShareHashtag.Builder()
                .setHashtag(title + "\n " + description + "\n#Tagueo")
                .build();

        ShareVideoContent content = new ShareVideoContent.Builder()
                .setVideo(video)
                .setShareHashtag(hashTag)
                .build();

        if (ShareDialog.canShow(ShareVideoContent.class)) {
            getShareDialog(result).show(content);
        }
    }

    private void sharePhotoToFacebook(String uri, String title, String description, Result result) {

        Bitmap bitmap = BitmapFactory.decodeFile(uri);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();

        ShareHashtag hashTag = new ShareHashtag.Builder()
                .setHashtag(title + "\n" + description + "\n#Tagueo")
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setShareHashtag(hashTag)
                .build();

        if (ShareDialog.canShow(SharePhotoContent.class)) {
            getShareDialog(result).show(content);
        }
    }


    /**
     * share to System
     *
     * @param msg    String
     * @param result Result
     */
    private void shareSystem(Result result, String msg) {
        try {
            Intent textIntent = new Intent("android.intent.action.SEND");
            textIntent.setType("text/plain");
            textIntent.putExtra("android.intent.extra.TEXT", msg);
            activity.startActivity(Intent.createChooser(textIntent, "Share to"));
            result.success("success");
        } catch (Exception var7) {
            result.error("error", var7.toString(), "");
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        callbackManager.onActivityResult(requestCode, resultCode, intent);
        return false;
    }
}
