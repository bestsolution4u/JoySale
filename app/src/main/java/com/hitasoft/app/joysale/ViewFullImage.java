package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.hitasoft.app.external.TouchImageView;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hitasoft.
 * <p>
 * This class is for View a Downloaded Image in Chat and Exchange Chat.
 */

public class ViewFullImage extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    TouchImageView imageView;
    ImageView back, btnMenu;
    TextView title;
    RelativeLayout mainLay;

    /**
     * Declare Variables
     **/
    String imgPath = "", imgType = "";
    static final String TAG = "ViewFullImage";
    DisplayMetrics displayMetrics;
    ArrayList<String> values = new ArrayList<>();
    ImageStorage imageStorage;
    String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_image);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageStorage = new ImageStorage(this);

        mainLay = findViewById(R.id.mainLay);
        imageView = (TouchImageView) findViewById(R.id.imageView);
        back = (ImageView) findViewById(R.id.backbtn);
        btnMenu = (ImageView) findViewById(R.id.settingbtn);
        title = (TextView) findViewById(R.id.title);

        back.setVisibility(View.VISIBLE);
        values.add(getString(R.string.save_to_gallery));
        title.setText(getString(R.string.photos));

        back.setOnClickListener(this);
        btnMenu.setOnClickListener(this);

        imgPath = getIntent().getExtras().getString(Constants.KEY_IMAGE);
        imgType = getIntent().getExtras().getString(Constants.IMAGETYPE);
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        if (from == null) from = "";

        if (imgType.equals("local")) {
            btnMenu.setVisibility(View.GONE);
            File file = new File(imgPath);

            if (!file.exists()) {
                showImageErrorDialog();
            }

            ViewCompat.setTransitionName(imageView, imgPath);
            Picasso.get().load(file).into(imageView);

        } else {//Remote Path
            Log.v(TAG, "imgPathRemote=" + imgPath);
            btnMenu.setVisibility(View.VISIBLE);
            ViewCompat.setTransitionName(imageView, imgPath);
            Picasso.get().load(AppUtils.getValidUrl(imgPath)).into(imageView);
        }
    }

    private void showImageErrorDialog() {
        final Dialog dialog = new Dialog(ViewFullImage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView subTxt = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView yes = (TextView) dialog.findViewById(R.id.alert_button);
        TextView no = (TextView) dialog.findViewById(R.id.cancel_button);

        subTxt.setText(getString(R.string.sorry_media_file_doesnt_exit));
        no.setText(getString(R.string.ok));

        yes.setVisibility(View.GONE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ViewFullImage.this, isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.settingbtn:
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.share_new, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.share, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ViewFullImage.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(displayMetrics.widthPixels * 60 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                popup.showAtLocation(mainLay, Gravity.TOP | Gravity.END, 0, 20);

                final ListView lv = (ListView) layout.findViewById(R.id.lv);
                lv.setAdapter(adapter);
                popup.showAsDropDown(v);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        openAction(values.get(position));
                    }
                });
                break;
        }
    }

    public void openAction(String menuAction) {
        if (menuAction.equals(getString(R.string.save_to_gallery))) {
            if (imgType.equals("local")) {
                new SaveToGalleryTask().execute();
            } else {
                new DownloadAndStoreImg(AppUtils.getValidUrl(imgPath), "" + System.currentTimeMillis(), from)
                        .execute();
            }
        }
    }

    /**
     * Function for Download Image from Server and Store in External Storage
     **/

    @SuppressLint("StaticFieldLeak")
    class DownloadAndStoreImg extends AsyncTask<Void, Void, Void> {
        String mImageUrl, mTimeStamp, from;
        boolean fileSaved = false;

        public DownloadAndStoreImg(String imageUrl, String timeStamp, String from) {
            mImageUrl = imageUrl;
            mTimeStamp = timeStamp;
            this.from = from;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mImageUrl != null && !mImageUrl.equals("")) {
                Bitmap image = imageStorage.downloadImage(mImageUrl);
                if (image != null) {
                    String imageName = AppUtils.getImageName(mImageUrl);
                    //Store Images outside a Sent Folder
                    if (!imageStorage.checkIfImageExists(from, imageName)) {
                        imageStorage.saveToAppDir(image, from, imageName, mTimeStamp);
                    }

                    try {
                        fileSaved = imageStorage.saveToGallery(image, imageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (fileSaved) {
                Toast.makeText(getApplicationContext(), R.string.image_saved_to_gallery, Toast.LENGTH_SHORT).show();
                btnMenu.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SaveToGalleryTask extends AsyncTask<Void, Void, Boolean> {
        boolean fileSaved = false;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String imageName = AppUtils.getImageName(imgPath);
                Bitmap myBitmap = BitmapFactory.decodeFile(new File(imgPath).getAbsolutePath());
                fileSaved = imageStorage.saveToGallery(myBitmap, imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return fileSaved;
        }

        @Override
        protected void onPostExecute(Boolean fileSaved) {
            super.onPostExecute(fileSaved);
            if (fileSaved) btnMenu.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), R.string.image_saved_to_gallery, Toast.LENGTH_SHORT).show();
        }

    }
}
