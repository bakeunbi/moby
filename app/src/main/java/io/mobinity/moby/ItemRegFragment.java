package io.mobinity.moby;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import io.mobinity.moby.item.ItemInfoItem;
import io.mobinity.moby.lib.BitmapLib;
import io.mobinity.moby.lib.FileLib;
import io.mobinity.moby.lib.GoLib;
import io.mobinity.moby.lib.MyLog;
import io.mobinity.moby.lib.MyToast;
import io.mobinity.moby.lib.StringLib;


public class ItemRegFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    public static final String REQUEST_SEQ = "REQUEST_SEQ";

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    Activity context;

    int mRequestSeq;

    File mImageFile;
    String mImageFilename;

    EditText mImageDescriptionEdit;
    ImageView mItemImage;

    ItemInfoItem mItemInfo;
    boolean mIsSavingImage = false;

    public static ItemRegFragment newInstance(){
        ItemRegFragment f = new ItemRegFragment();
        return f;
    }
    /**
     * 프래그먼트가 생성될 때 호출되며 인자에 저장된 REQUEST_SEQ를 멤버 변수 request_seq에 저장한다.
     * @param savedInstanceState 프래그먼트가 새로 생성되었을 경우, 이전 상태 값을 가지는 객체
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO:request의 seq 관리 필요
        mRequestSeq = 0;

//        if (getArguments() != null) {
//            mRequestSeq = getArguments().getInt(REQUEST_SEQ);
//        }

    }

    /**
     * fragment_item_reg.xml기반으로 뷰 생성
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_reg, container, false);
    }

    /**
     * onCreateView() 메소드 뒤에 호출되며 기본 정보 생성과 화면 처리를 한다.
     * @param view onCreateView() 메소드에 의해 반환된 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mItemInfo = new ItemInfoItem();
        mItemInfo.setRequest_seq(mRequestSeq);

        mImageFilename = mRequestSeq + "_" + String.valueOf(System.currentTimeMillis());
        mImageFile = FileLib.getInstance().getImageFile(context, mImageFilename);

        mItemImage = (ImageView) view.findViewById(R.id.item_image_view);
        mItemImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showImageDialog(context);
            }
        });
        mImageDescriptionEdit = (EditText) view.findViewById(R.id.image_desc);

        ImageButton next_btn = (ImageButton) view.findViewById(R.id.next_btn);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:newInstance()의 파라미터로 item 전달하기
                GoLib.getInstance().goFragment(getFragmentManager(),
                        R.id.card_view_content, RecipientInfoFragment.newInstance(mItemInfo));
            }
        });

    }

    /**
     * 이미지를 어떤 방식으로 선택할지에 대해 다이얼로그를 보여준다.
     * @param context 컨텍스트 객체
     */
    public void showImageDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_image_register)
                .setSingleChoiceItems(R.array.camera_album_category, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    getImageFromCamera();
                                } else {
                                    getImageFromAlbum();
                                }

                                dialog.dismiss();
                            }
                        }).show();
    }

    /**
     * 이미지를 촬영하고 그 결과를 받을 수 있는 액티비티를 시작한다.
     */
    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Android 7.0 이상부터 변경
        //Uri uri = Uri.fromFile(mImageFile);
        Uri uri = FileProvider.getUriForFile(getContext(), "io.mobinity.moby.fileprovider", mImageFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /**
     * 앨범으로부터 이미지를 선택할 수 있는 액티비티를 시작한다.
     */
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    /**
     * 사용자가 선택한 이미지와 입력한 메모를 ImageItem 객체에 저장한다.
     */
    private  void setImageItem() {
        String imageMemo = mImageDescriptionEdit.getText().toString();
        if (StringLib.getInstance().isBlank(imageMemo)) {
            imageMemo = "";
        }

        mItemInfo.setDescription(imageMemo);
        mItemInfo.setFile_name(mImageFilename + ".png");
    }

    /**
     * 이미지를 서버에 업로드한다.
     */
    private void saveImage() {
        if (mIsSavingImage) {
            MyToast.s(context, R.string.no_image_ready);
            return;
        }
        MyLog.d(TAG, "imageFile.length() " + mImageFile.length());

        if (mImageFile.length() == 0) {
            MyToast.s(context, R.string.no_image_selected);
            return;
        }

        setImageItem();
        //TODO:서버 연결 후 업로드 기능 구현 완료
//        RemoteLib.getInstance().uploadFoodImage(mRequestSeq,
//                mItemInfo.getDescription(), mImageFile, finishHandler);
        mIsSavingImage = false;
    }


    /**
     * 다른 액티비티를 실행한 결과를 처리하는 메소드
     * @param requestCode 액티비티를 실행하면서 전달한 요청 코드
     * @param resultCode 실행한 액티비티가 설정한 결과 코드
     * @param data 결과 데이터
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {
                Picasso.with(context).load(mImageFile).into(mItemImage);
                mItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else if (requestCode == PICK_FROM_ALBUM && data != null) {
                Uri dataUri = data.getData();

                if (dataUri != null) {
                    Picasso.with(context).load(dataUri).into(mItemImage);
                    mItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Picasso.with(context).load(dataUri).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            BitmapLib.getInstance().saveBitmapToFileThread(imageUploadHandler,
                                    mImageFile, bitmap);
                            mIsSavingImage = true;
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                }
            }
        }
    }
    Handler imageUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mIsSavingImage = false;
            setImageItem();
            //TODO:서버연결 후
            //Picasso.with(context).invalidate(RemoteService.IMAGE_URL + imageItem.fileName);
        }
    };

    Handler finishHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            context.finish();
        }
    };
}
