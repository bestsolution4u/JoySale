package com.hitasoft.app.joysale;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.model.MyPromotionResponse;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Display Details of a Promotion.
 */

public class PromotionDetail extends BaseActivity implements View.OnClickListener {

    private static final String TAG = PromotionDetail.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    ImageView backBtn, userImg, itemImage;
    TextView username, itemName, promotionType, paidAmount, transactionId, upto, status, repromote;
    LinearLayout dateLay;

    MyPromotionResponse.Result data = new MyPromotionResponse().new Result();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promotion_detail);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        userImg = (ImageView) findViewById(R.id.userImg);
        username = (TextView) findViewById(R.id.username);
        itemImage = (ImageView) findViewById(R.id.imageView);
        itemName = (TextView) findViewById(R.id.itemtitle);
        promotionType = (TextView) findViewById(R.id.addvr);
        paidAmount = (TextView) findViewById(R.id.amount);
        transactionId = (TextView) findViewById(R.id.transid);
        upto = (TextView) findViewById(R.id.date);
        status = (TextView) findViewById(R.id.status);
        repromote = (TextView) findViewById(R.id.promote);
        dateLay = (LinearLayout) findViewById(R.id.dateLay);

        data = (MyPromotionResponse.Result) getIntent().getExtras().get(Constants.TAG_DATA);

        backBtn.setVisibility(View.VISIBLE);
        userImg.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);
        repromote.setVisibility(View.GONE);

        repromote.setOnClickListener(this);

        Picasso.get().load(GetSet.getImageUrl()).into(userImg);
        username.setText(GetSet.getUserName());

        setData();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * set promotion details to elements
     **/
    private void setData() {
        try {
            Picasso.get().load(Constants.SITE_URL + "media/item/" + data.getItemId() + "/" + data.getItemImage()).into(itemImage);
            itemName.setText(data.getItemName());
            paidAmount.setText(data.getFormattedPaidAmount());
            transactionId.setText(data.getTransactionId());

            if (data.getPromotionName().equalsIgnoreCase("urgent")) {
                promotionType.setText(getString(R.string.urgent));
                dateLay.setVisibility(View.GONE);
            } else {
                promotionType.setText(getString(R.string.ad));
                dateLay.setVisibility(View.VISIBLE);
            }

            if (data.getStatus().equalsIgnoreCase("Live")) {
                status.setText(getString(R.string.live));
            } else {
                status.setText(getString(R.string.expired));
            }

            if (LocaleManager.isRTL(PromotionDetail.this)) {
                itemName.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            } else {
                itemName.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            }

            if (data.getUpto().contains("-")) {
                String[] date = data.getUpto().split(" - ");
                long timestamp0 = 0, timestamp1 = 0;
                if (date[0] != null && date[1] != null) {
                    timestamp0 = Long.parseLong(date[0]);
                    timestamp1 = Long.parseLong(date[1]);

                    upto.setText(JoysaleApplication.getDate(PromotionDetail.this, timestamp0) + " - " + JoysaleApplication.getDate(PromotionDetail.this, timestamp1));
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        JoysaleApplication.networkError(PromotionDetail.this, isConnected);
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote:
                Intent i = new Intent(PromotionDetail.this, CreatePromote.class);
                i.putExtra("itemId", data.getId());
                startActivity(i);
                break;
        }
    }
}