package com.nowfloats.on_boarding;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nowfloats.Login.AuthLoginInterface;
import com.nowfloats.Login.LoginManager;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.on_boarding.models.OnBoardingModel;
import com.nowfloats.util.BoostLog;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.thinksity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 16-03-2018.
 */

public class OnBoardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private String[] titles, descriptions, buttonText;
    private LinearLayout.LayoutParams linearLayoutParams;
    private int leftSpace, topSpace;
    private OnBoardingModel mOnBoardingModel;
    private RecyclerView mRecyclerView;
    private DisplayMetrics metrics;
    private SharedPreferences sharedPreferences;
    public OnBoardingAdapter(Context context, OnBoardingModel onBoardingModel){
        mContext = context;
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        mOnBoardingModel = onBoardingModel;
        metrics = mContext.getResources().getDisplayMetrics();
        leftSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        topSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        linearLayoutParams = new LinearLayout.LayoutParams(metrics.widthPixels * 80 / 100,
                metrics.heightPixels * 55 / 100);
        linearLayoutParams.setMargins(leftSpace, topSpace, leftSpace, topSpace);
        descriptions = mContext.getResources().getStringArray(R.array.on_boarding_description);
        titles = mContext.getResources().getStringArray(R.array.on_boarding_titles);
        buttonText = mContext.getResources().getStringArray(R.array.on_boarding_btnTexts);

        addAuthScreens();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_onboarding_item,parent,false);
        return new MyViewHolder(view);
    }

    private void addAuthScreens() {

        ArrayList<OnBoardingModel.ScreenData> data = new ArrayList<>();
        OnBoardingModel.ScreenData facebookData = new OnBoardingModel.ScreenData();
        facebookData.key = "FACEBOOK";
        facebookData.setIsComplete(sharedPreferences.getBoolean(facebookData.getKey(), false));

        data.add(facebookData);

        OnBoardingModel.ScreenData googleData = new OnBoardingModel.ScreenData();
        googleData.key = "GOOGLE";
        googleData.setIsComplete(sharedPreferences.getBoolean(googleData.getKey(), false));

        data.add(googleData);

        OnBoardingModel.ScreenData otpData = new OnBoardingModel.ScreenData();
        otpData.key = "OTP";
        otpData.setIsComplete(sharedPreferences.getBoolean(otpData.getKey(), false));

        data.add(otpData);

        data.addAll(mOnBoardingModel.getScreenDataArrayList());
        mOnBoardingModel.setScreenDataArrayList(data);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder){
            ((MyViewHolder) holder).setData(position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void refreshAfterComplete(){
        mOnBoardingModel.setToBeCompletePos(-1);
        int  i = -1;
        for (OnBoardingModel.ScreenData data : mOnBoardingModel.getScreenDataArrayList()){
            i++;
            switch (i){
                case 3:
                    data.setIsComplete(sharedPreferences.getInt(Key_Preferences.SITE_HEALTH,0)>=80);
                    data.setValue(String.valueOf(sharedPreferences.getInt(Key_Preferences.SITE_HEALTH,0)));
                    break;
                case 4:
                    data.setIsComplete(sharedPreferences.getInt(Key_Preferences.CUSTOM_PAGE,0)>0);
                    break;
                case 5:
                    data.setIsComplete(sharedPreferences.getInt(Key_Preferences.PRODUCTS_COUNT,0)>0);
                    break;
                case 6:
                    data.setIsComplete(true);
                    break;
                case 7:
                    data.setIsComplete(sharedPreferences.getBoolean(Key_Preferences.WEBSITE_SHARE,false));
                    break;
                default:
                    data.setIsComplete(sharedPreferences.getBoolean(data.getKey(), false));
            }
            if (!data.isComplete() && mOnBoardingModel.getToBeCompletePos() == -1){
                mOnBoardingModel.setToBeCompletePos(i);
            }
        }
        if (mOnBoardingModel.getToBeCompletePos() == -1){
            mOnBoardingModel.setToBeCompletePos(5);
            sharedPreferences.edit().putBoolean(Key_Preferences.ON_BOARDING_STATUS,true).apply();
            ((ItemClickListener)mContext).onBoardingComplete();
        }
        if(mRecyclerView != null){
            mRecyclerView.scrollToPosition(mOnBoardingModel.getToBeCompletePos());
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return descriptions.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView titleTv, cardNumberTv, descriptionTv, btnTv;
        private CardView itemView;
        private ImageView completeImg;
        ConstraintLayout layout;
        public MyViewHolder(final View itemView) {
            super(itemView);
            this.itemView = (CardView) itemView;
            itemView.setLayoutParams(linearLayoutParams);
            layout = itemView.findViewById(R.id.card_layout);
            titleTv = itemView.findViewById(R.id.tv_title);
            cardNumberTv = itemView.findViewById(R.id.tv_card_number);
            descriptionTv = itemView.findViewById(R.id.tv_description);
            completeImg = itemView.findViewById(R.id.img_complete);
            btnTv = itemView.findViewById(R.id.btn_tv);
            btnTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(titles[getAdapterPosition()].contains("via Google") || titles[getAdapterPosition()].contains("via Facebook") || titles[getAdapterPosition()].contains("via mobile number")) {

                        if(titles[getAdapterPosition()].contains("via Google")) {
                            LoginManager.getInstance().getListener().onGoogleLogin();
                        }else if(titles[getAdapterPosition()].contains("via Facebook")){
                            LoginManager.getInstance().getListener().onFacebookLogin();
                        }else{
                            LoginManager.getInstance().getListener().onOTPLogin();
                        }

                    }else if (mOnBoardingModel.getScreenDataArrayList().get(getAdapterPosition()).isComplete()
                            || getAdapterPosition() == mOnBoardingModel.getToBeCompletePos()){

                        ((ItemClickListener)mContext).onItemClick(getAdapterPosition(), mOnBoardingModel.getScreenDataArrayList().get(getAdapterPosition()));

                    }else if(mRecyclerView != null){
                        Toast.makeText(mContext, "Finish Previous Step!", Toast.LENGTH_SHORT).show();
                        mRecyclerView.scrollToPosition(mOnBoardingModel.getToBeCompletePos());
                    }
                }
            });
        }

        void setData(int position){

            boolean authInfoDisplay = titles[getAdapterPosition()].contains("via Google") || titles[getAdapterPosition()].contains("via Facebook") || titles[getAdapterPosition()].contains("via mobile number");

            titleTv.setText(String.format(titles[position],mOnBoardingModel.getScreenDataArrayList().get(position).getValue()+"%"));
            cardNumberTv.setText(String.valueOf(position+1));
            descriptionTv.setText(descriptions[position]);
            btnTv.setText(buttonText[position]);

            if(mOnBoardingModel.getScreenDataArrayList().get(position).isComplete()){
                completeImg.setVisibility(View.VISIBLE);
                layout.setAlpha(1f);
                itemView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                btnTv.setBackgroundResource(R.drawable.btn_bg);
            }else if(position != mOnBoardingModel.getToBeCompletePos() && !authInfoDisplay){
                layout.setAlpha(.4f);
                completeImg.setVisibility(View.GONE);
                itemView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.light_gray));
                btnTv.setBackgroundResource(R.color.gray);
            }else{
                layout.setAlpha(1f);
                completeImg.setVisibility(View.GONE);
                itemView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                btnTv.setBackgroundResource(R.drawable.btn_bg);
            }


            btnTv.setVisibility(position == 4?View.INVISIBLE:View.VISIBLE);

            int padd = Methods.dpToPx(10,mContext);
            btnTv.setPadding(padd,padd,padd,padd);
        }

    }
    public interface ItemClickListener{
        void onItemClick(int pos, OnBoardingModel.ScreenData data);
        void onBoardingComplete();
    }
}
