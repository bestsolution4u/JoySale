package com.hitasoft.app.joysale;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hitasoft.app.external.TouchImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hitasoft on 1/7/16.
 * <p>
 * This class is for Provide a Single Product Image from a List of Images in a Product.
 */

public class SingleView extends BaseActivity {

    /**
     * Declare Layout Elements
     **/
    ImageView back;
    ViewPager viewPager;
    TextView title;


    //Declare variables
    String TAG = "SingleView";

    ViewPagerAdapter pagerAdapter;
    ArrayList<String> newary;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_view);

        viewPager = (ViewPager) findViewById(R.id.pager);
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        newary = (ArrayList<String>) getIntent().getExtras().get("mimages");

        back.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.photos));
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SingleView.this.finish();
            }
        });

        //To initialize and set Adapter
        pagerAdapter = new ViewPagerAdapter(getApplicationContext(), newary);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(getIntent().getExtras().getInt("position"));
        viewPager.setPageMargin(dpToPx(1));

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = SingleView.this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(SingleView.this, isConnected);
    }

    /**
     * Adapter for swiping the images
     **/

    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        public Object instantiateItem(ViewGroup collection, int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.single_image,
                    collection, false);

            final TouchImageView image = itemView.findViewById(R.id.imgDisplay);
            final ProgressBar progressBar = itemView.findViewById(R.id.progress);
            progressBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

            Picasso.get().load(temp.get(position)).into(image);
            ((ViewPager) collection).addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
