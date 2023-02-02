package com.example.sharedialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.CallbackManager;
import com.facebook.share.widget.ShareDialog;

/**
 * 카카오톡, 페이스북, URL 복사 기능이 있는 팝업 다이얼로그
 * <p>
 *     Created by Myeong Hoon Lim on 2021-07-07
 * </p>
 */
public class KakaoFacebookShareDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private ConstraintLayout mClKakaoShare;
    private ConstraintLayout mClUrlShare;

    // 페이스북 관련 컴포넌트 및 필요 변수 [Start]
    private ConstraintLayout mClFacebookShare;
    private CallbackManager mCallbackManager;
    private ShareDialog mShareDialog;
    // 페이스북 관련 컴포넌트 [End]

    private String mShareUrl = "";
    private String mMessageTitle = "";
    private String mImgPath = "";

    // 클립보드에 복사된 문자열(url)
    private String mCopiedClipBoardStr = "";

    // 카카오톡 템플릿 타입
    private String mKakaoTemplateType = "";

    // 카카오톡 상품 상세 공유하기에 들어가는 데이터 모델
    KakaoCommerceTemplateData mKakaoCommerceTemplateData = new KakaoCommerceTemplateData();

    // ShareDialog 생성자
    public KakaoFacebookShareDialog(
            @NonNull Context context,
            String url,
            String messageTitle,
            String imgPath,
            @Nullable String kakaoTemplateType,
            @Nullable KakaoCommerceTemplateData kakaoCommerceTemplateData,
            @Nullable CallbackManager callbackManager) {
        super(context);
        mContext = context;
        mShareUrl = url;
        mMessageTitle = messageTitle;
        mImgPath = imgPath;
        mKakaoTemplateType = kakaoTemplateType;
        mKakaoCommerceTemplateData = kakaoCommerceTemplateData;
        mCallbackManager = callbackManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 오로지 xml만을 보여주기 위해 윈도우 프레임 타이틀 제거
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(false);
        this.getWindow().setBackgroundDrawableResource(R.drawable.bg_round_popup);
        setContentView(R.layout.dialog_share_link);

        // xml 뷰 초기화
        initView();
    }

    private void initView() {
        // 함수 내에서 처리할 수 있는 클로즈 아이콘 영역
        findViewById(R.id.img_close).setOnClickListener(this);

        mClKakaoShare = findViewById(R.id.cl_kakao_share);
        mClFacebookShare = findViewById(R.id.cl_facebook_share);
        mClUrlShare = findViewById(R.id.cl_url_share);
        mClKakaoShare.setOnClickListener(this);
        mClFacebookShare.setOnClickListener(this);
        mClUrlShare.setOnClickListener(this);

        mShareDialog = new ShareDialog((Activity) mContext);
    }

    /**
     * SNS 공유하기 다이얼로그 클릭 액션
     * <p>
     *     Created by Myeong Hoon Lim on 2021-07-29
     * </p>
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_close:            // 팝업 클로즈
                Log.d("SHARE TAG", "SHARE DIALOG CLOSE!");
                dismiss();
                break;
            case R.id.cl_kakao_share:       // 카카오톡으로 url 공유
                Log.d("SHARE TAG","SHARE URL THROUGH KAKAO TALK!");

                // 공유할 웹페이지 URL
                //  * 주의: 개발자사이트 Web 플랫폼 설정에 공유할 URL의 도메인이 등록되어 있어야 합니다.
                shareThroughKakaoLink();
                break;
            case R.id.cl_facebook_share:    // 페이스북으로 url 공유
                Log.d("SHARE TAG","SHARE URL THROUGH FACEBOOK!");

                shareThroughFacebook();
                break;
            case R.id.cl_url_share:         // 클립보드에 url 복사
                Log.d("SHARE TAG","SHARE URL THROUGH CLIPBOARD!");

                ActionClipBoardCopy();
                break;
            default:
                // 이 팝업 다이얼로그에서는 우측 상단 클로즈와 각각의 공유 아이콘 클릭 이벤트밖에 설정한 게 없으므로 default를 비워 놓았다.
                break;
        }
    }

    /**
     * 카카오링크 템플릿 타입에 따라 스크랩, 커머스 템플릿으로 나눠 처리하는 메소드
     * <p>
     *     Created by Myeong Hoon Lim on 2021-07-29
     * </p>
     */
    private void shareThroughKakaoLink() {
        boolean isKakaoLinkTemplateScrap = mKakaoTemplateType.equals("카카오 공유 타입");
        if (!(mKakaoTemplateType.equals(""))) {
            if (isKakaoLinkTemplateScrap) {
                // 스크랩 템플릿 메시지로 전송
                KakaoUtil.getInstance().shareThroughScrapTemplate(
                        mContext,
                        mShareUrl,
                        mMessageTitle,
                        mImgPath);
            } else {
                // 커머스 템플릿 메시지로 전송
                KakaoUtil.getInstance().shareThroughCommerceTemplate(
                        mContext,
                        mKakaoCommerceTemplateData
                );
            }
        } else {
            // 타입이 null이거나 ""(빈 값)일 때는 아무것도 실행하지 않기 위해 else문을 비워 놓았다.
        }
    }

    /**
     * 페이스북으로 공유하기
     * <p>
     *     Created by Myeong Hoon Lim on 2021-07-29
     * </p>
     */
    private void shareThroughFacebook() {
        FacebookUtil.getInstance().shareThroughFacebook(
                mContext,
                mShareDialog,
                mCallbackManager,
                mShareUrl
        );
    }

    /**
     * url 클립보드에 복사하기
     * <p>
     *     Created by Myeong Hoon Lim on 2021-07-29
     * </p>
     */
    private void ActionClipBoardCopy() {
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Label", mShareUrl);
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(mContext, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
    }
}