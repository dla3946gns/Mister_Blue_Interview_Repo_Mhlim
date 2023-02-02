package com.example.sharedialog;

import android.content.Context;
import android.util.Log;

import com.kakao.sdk.link.LinkClient;
import com.kakao.sdk.link.model.LinkResult;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Commerce;
import com.kakao.sdk.template.model.CommerceTemplate;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.DefaultTemplate;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class KakaoUtil {

    private static KakaoUtil instance = new KakaoUtil();

    public static synchronized KakaoUtil getInstance() {
        return instance;
    }

    // 카카오링크 공유하기에 쓰일 Android Execution Parameter와 iOS Execution Parameter 변수
    Map<String, String> mAndroidParams = new HashMap<>();
    Map<String, String> mIOSParams = new HashMap<>();
    Content mContent = null;
    Link mLink = null;

    /**
     * 메인 기획전/이벤트 상세 페이지에서 쓸 카카오톡 공유하기 메소드
     * @param context
     * @param url
     * @param imgPath
     */
    public synchronized void shareThroughScrapTemplate(Context context, String url, String title, String imgPath) {

        // Execution Params
        mAndroidParams.put("target", url);
        mIOSParams.put("target", url);

        // Link 객체
        mLink = new Link(url, url, mAndroidParams, mIOSParams);

        // Content 객체
        mContent = new Content(title, imgPath, mLink);

        // 스크랩 메시지 탬플릿
        FeedTemplate feedTemplate = new FeedTemplate(mContent);

        // 카카오톡이 설치되어 있을 때
        if (LinkClient.getInstance().isKakaoLinkAvailable(context)) {
            kakaoLinkShareTemplate(context, feedTemplate);
        }
        // 카카오톡이 설치되어 있지 않을 때
        else {
            // 카카오톡 설치 후 사용해주시기 바랍니다. 팝업 띄워주기
            popupWithoutKakaoTalk(context);
        }
    }

    /**
     * 상품 상세 페이지에서 쓸 카카오톡 공유하기 메소드
     * @param context
     * @param kakaoCommerceTemplateData
     */
    public synchronized void shareThroughCommerceTemplate(Context context, KakaoCommerceTemplateData kakaoCommerceTemplateData) {

        mAndroidParams.put("target", kakaoCommerceTemplateData.url);
        mIOSParams.put("target", kakaoCommerceTemplateData.url);
        mLink = new Link(kakaoCommerceTemplateData.url, kakaoCommerceTemplateData.url, mAndroidParams, mIOSParams);

        // 커머스 템플릿에 쓸 데이터 세팅
        Commerce commerce = null;
        if (kakaoCommerceTemplateData.imageUrl != null && !kakaoCommerceTemplateData.imageUrl.equals("")) {
            mContent = new Content(
                    kakaoCommerceTemplateData.title,
                    kakaoCommerceTemplateData.imageUrl,
                    mLink);
        } else {
            // 구매하기 버튼만 넣기 위해서 else문은 비워 놓았다.
        }

        if (kakaoCommerceTemplateData.regularPrice == kakaoCommerceTemplateData.discountPrice) {
            commerce = new Commerce(
                    kakaoCommerceTemplateData.regularPrice,
                    null,
                    null,
                    null,
                    kakaoCommerceTemplateData.productName);
        } else {
            commerce = new Commerce(
                    kakaoCommerceTemplateData.regularPrice,
                    kakaoCommerceTemplateData.discountPrice,
                    kakaoCommerceTemplateData.fixedDiscountPrice,
                    kakaoCommerceTemplateData.discountRate,
                    kakaoCommerceTemplateData.productName);
        }

        // 구매하기와 공유하기 버튼 세팅
        List<Button> buttons = new ArrayList<>();

        // 구매하기 버튼은 버튼 해당 제품 정보를 담은 link를 넣어주기
        Button btnBuy = new Button("구매하기", mLink);

        /**
         * 공유하기 버튼은 해당 제품 정보와 intent 등의 정보를 따로 넘겨 공유하기 intent를
         * 받아오면 이벤트 공유하기 팝업을 띄우고 카카오톡 공유를 하게 하는지
         * 바로 카카오톡 공유를 하게 하는지 결정하기
         */
        buttons.add(btnBuy);
        // 커머스 템플릿
        CommerceTemplate commerceTemplate = new CommerceTemplate(mContent, commerce, buttons);

        // 카카오톡이 설치되어 있을 때
        if (LinkClient.getInstance().isKakaoLinkAvailable(context)) {
            kakaoLinkShareTemplate(context, commerceTemplate);
        }
        // 카카오톡이 설치되어 있지 않을 때
        else {
            // 카카오톡 설치 후 사용해주시기 바랍니다. 팝업 띄워주기
            popupWithoutKakaoTalk(context);
        }
    }

    private void kakaoLinkShareTemplate(Context context, DefaultTemplate defaultTemplate) {
        LinkClient.getInstance().defaultTemplate(context, defaultTemplate, null, new Function2<LinkResult, Throwable, Unit>() {
            @Override
            public Unit invoke(LinkResult linkResult, Throwable throwable) {
                if (throwable != null) {
                    Log.e("TAG", "카카오링크 보내기 실패", throwable);
                }
                else if (linkResult != null) {
                    context.startActivity(linkResult.getIntent());

                    // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Log.w("TAG", "Warning Msg: "+ linkResult.getWarningMsg());
                    Log.w("TAG", "Argument Msg: "+ linkResult.getArgumentMsg());
                } else {
                    // 위 사항 이외의 경우엔 아무것도 실행하지 않기 위해 else문을 비워 놓았다.
                }
                return null;
            }
        });
    }

    private void popupWithoutKakaoTalk(Context context) {
        CommandUtil.getInstance().showCommonOneButtonDialog(
                context,
                "카카오톡 설치 후 사용해주시기 바랍니다.",
                "확인",
                CommonPopupDialog.COMMON_DIALOG_OPTION_CLOSE_DIALOG,
                null
        );
    }

}