package com.angelchanquin.flutter_social_share;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.facebook.CallbackManager;
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

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
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
    private final CallbackManager callbackManager;
    private final Registrar registrar;
    private final MethodChannel channel;

    private final static String INSTAGRAM_PACKAGE_NAME = "com.instagram.android";

    private FlutterSocialSharePlugin(MethodChannel channel, Registrar registrar) {
        this.channel = channel;
        this.activity = registrar.activity();
        this.registrar = registrar;
        this.callbackManager = CallbackManager.Factory.create();
        registrar.addActivityResultListener(this);
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_social_share");
        final FlutterSocialSharePlugin instance = new FlutterSocialSharePlugin(channel, registrar);
        channel.setMethodCallHandler(instance);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        final PackageManager pm = registrar.activeContext().getPackageManager();

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
            case "shareInstagramPhoto":
                try {
                    pm.getPackageInfo(INSTAGRAM_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                    instagramShare("image/*", call.<String>argument("uri"));
                    result.success(true);
                } catch (PackageManager.NameNotFoundException e) {
                    openPlayStore(INSTAGRAM_PACKAGE_NAME);
                    result.success(false);
                }
                break;
            case "shareInstagramVideo":
                try {
                    pm.getPackageInfo(INSTAGRAM_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                    instagramShare("video/*", call.<String>argument("uri"));
                    result.success("success");
                } catch (PackageManager.NameNotFoundException e) {
                    openPlayStore(INSTAGRAM_PACKAGE_NAME);
                    result.success(false);
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void openPlayStore(String packageName) {
        final Context context = registrar.activeContext();
        try {
            final Uri playStoreUri = Uri.parse("market://details?id=" + packageName);
            final Intent intent = new Intent(Intent.ACTION_VIEW, playStoreUri);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            final Uri playStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
            final Intent intent = new Intent(Intent.ACTION_VIEW, playStoreUri);
            context.startActivity(intent);
        }
    }

    private void instagramShare(String type, String filePath) {
        final Context context = registrar.activeContext();
        final File file = new File(filePath);
        final Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", file);
        final Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(type);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setPackage(INSTAGRAM_PACKAGE_NAME);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(share, "Share to"));
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
                .setHashtag(title + "\n " + description)
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
                .setHashtag(title + "\n" + description)
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setShareHashtag(hashTag)
                .build();

        if (ShareDialog.canShow(SharePhotoContent.class)) {
            getShareDialog(result).show(content);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        callbackManager.onActivityResult(requestCode, resultCode, intent);
        return false;
    }
}
