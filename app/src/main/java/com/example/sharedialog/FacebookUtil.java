package com.example.sharedialog;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class FacebookUtil {

    private static FacebookUtil instance = new FacebookUtil();

    public static synchronized FacebookUtil getInstance() {
        return instance;
    }

    /**
     * 페이스북 다이얼로그로 url을 공유하는 메소드
     * @param context           현재 액티비티
     * @param shareDialog       페이스북 공유하기 다이얼로그
     * @param callbackManager   페이스북 공유하기 callback manager
     * @param url               페이스북으로 공유할 url
     * <p>
     *      Created by Myeong Hoon Lim on 2021-08-10
     * </p>
     */
    public synchronized void shareThroughFacebook(Context context, ShareDialog shareDialog, CallbackManager callbackManager, String url) {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(url))
                    .build();
            shareDialog.show(linkContent);

            // this part is optional
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Log.d("FACEBOOK TAG", "FACEBOOK SHARE SUCCESS");
                }

                @Override
                public void onCancel() {
                    Log.d("FACEBOOK TAG","FACEBOOK SHARE CANCEL");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("FACEBOOK TAG","FACEBOOK SHARE ERROR");
                    Log.d("FACEBOOK TAG","FACEBOOK SHARE ERROR EXCEPTION: " + error.getMessage());
                }
            });

        } else {
            // 페이스북 공유하기 다이얼로그를 ShareLinkContent 클래스를 통해 실행하지 못할 때는 아무것도 하지 않기 위해 else문을 비워 놓았다.
        }
    }

}