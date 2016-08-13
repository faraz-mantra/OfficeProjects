package com.nowfloats.NavigationDrawer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.widgets.SnackBar;
import com.nowfloats.Login.Model.FloatsMessageModel;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.viewHolder.MyViewHolder;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.Constants;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.squareup.picasso.Picasso;
import com.thinksity.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NowFloatsDev on 09/03/2015.
 */
public class CardAdapter_V3 extends RecyclerView.Adapter<MyViewHolder> {

    private final int VIEW_TYPE_WELCOME 		= 0;
    private final int VIEW_TYPE_IMAGE_TEXT 		= 1;
    String imageUri = "";

    private LayoutInflater mInflater;
    public Activity appContext ;
    FloatsMessageModel data;
    String msg = "", date = "";
    private boolean imagePresent;
    UserSessionManager session;

    static ProgressDialog pd ;

    public CardAdapter_V3(Activity appContext, UserSessionManager session)
    {
        Log.d("CardAdapter_V3","Constructor");
        this.appContext = appContext ;
        this.session = session;
        mInflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = null;
        MyViewHolder.WelcomeViewHolder welcomeViewHolder = null ;
        MyViewHolder.Image_Text_ViewHolder image_text_viewHolder = null ;

        if(viewType == VIEW_TYPE_WELCOME)
        {
              if (convertView == null ) {
                  convertView = mInflater.inflate(R.layout.card_welcome, parent, false);
              }
              welcomeViewHolder = new MyViewHolder.WelcomeViewHolder(convertView);
            if (Home_Main_Fragment.emptyMsgLayout!=null)
                Home_Main_Fragment.emptyMsgLayout.setVisibility(View.GONE);
              return welcomeViewHolder ;
        }
        else
        {
            convertView = mInflater.inflate(R.layout.cards_layout,parent,false);
            image_text_viewHolder = new MyViewHolder.Image_Text_ViewHolder(convertView);

            convertView.setOnClickListener(Home_Main_Fragment.myOnClickListener);
            return image_text_viewHolder ;
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Log.d("CardAdapter_V3","onBindViewHolder");
        if( holder instanceof MyViewHolder.WelcomeViewHolder)
        {
            TextView welcomeTextView = holder.welcomeTextView;
            TextView congratsTextView = holder.congratsTitleTextView ;
            Typeface robotoMedium = Typeface.createFromAsset(appContext.getAssets(),"Roboto-Medium.ttf");
            Typeface robotoLight = Typeface.createFromAsset(appContext.getAssets(),"Roboto-Light.ttf");

            ImageView welcomeScreenShowWebsite = holder.welcomeScreenCreateWebsiteImage;
            PorterDuffColorFilter whiteLabelFilter = new PorterDuffColorFilter(appContext.getResources().getColor(R.color.primaryColor), PorterDuff.Mode.SRC_OUT);
            welcomeScreenShowWebsite.setColorFilter(whiteLabelFilter);

            welcomeTextView.setTypeface(robotoMedium);
            congratsTextView.setTypeface(robotoLight);

            ImageView cancelImageView = holder.cancelCardImageView;
            final CardView initialCard = holder.initialCardView ;
            if (Home_Main_Fragment.emptyMsgLayout!=null)
                Home_Main_Fragment.emptyMsgLayout.setVisibility(View.GONE);

            // Button showWebSiteButton = holder.showWebSiteButton;
            cancelImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MixPanelController.track(EventKeysWL.SHOW_MOBILE_WEB_SITE_CANCEL, null);
                    Constants.isWelcomScreenToBeShown = false;
                    initialCard.setVisibility(View.GONE);

                    if(HomeActivity.StorebizFloats!=null && HomeActivity.StorebizFloats.size()==0){
                        if (Home_Main_Fragment.emptyMsgLayout!=null)
                            Home_Main_Fragment.emptyMsgLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            Button showWebSiteButton = holder.showWebSiteButton ;
            showWebSiteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MixPanelController.track(EventKeysWL.SHOW_MOBILE_WEB_SITE,null);
                    String url = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ROOTALIASURI);
                    if (!Util.isNullOrEmpty(url)) {
                        url = url.toLowerCase();
                    }else{
                        url = "http://"+ session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG).toLowerCase()
                                + appContext.getResources().getString(R.string.tag_for_partners);
                    }

                    Constants.isWelcomScreenToBeShown = false;
                    initialCard.setVisibility(View.GONE);
                    Intent showWebSiteIntent = new Intent(appContext,Mobile_Site_Activity.class);
                    showWebSiteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    showWebSiteIntent.putExtra("WEBSITE_NAME", url);
                    appContext.startActivity(showWebSiteIntent);

                    if(HomeActivity.StorebizFloats!=null && HomeActivity.StorebizFloats.size()==0){
                        if (Home_Main_Fragment.emptyMsgLayout!=null)
                            Home_Main_Fragment.emptyMsgLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            final TextView textView1 = holder.textView ;
            TextView dateText = holder.dateText ;
            ImageView imageView = holder.imageView;
            ImageView shareImageView = holder.shareImageView;

            final String imageShare = HomeActivity.StorebizFloats.get(position).imageUri;


            shareImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MixPanelController.track("SharePost",null);
                   // final Intent shareIntent = null;
                    appContext.runOnUiThread(new Runnable() {
                        public void run() {
                            pd = ProgressDialog.show(appContext, "", "Sharing . . .");
                            pd.setCancelable(true);
                        }
                    });

                    Thread mThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                final Intent shareIntent = new Intent();
                                if (!imageShare.contains("/Tile/deal.png") && !Util.isNullOrEmpty(imageShare)) {
                                    if(Methods.isOnline(appContext)) {
                                        URL url = null;
                                        Uri uri;


                                        try {

                                            if (imageShare.contains("BizImages")) {
                                                url = new URL(Constants.NOW_FLOATS_API_URL + "" + imageShare);
                                            } else {
                                                url = new URL(imageShare);
                                            }

                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }


                                        HttpURLConnection connection = null;
                                        try {
                                            connection = (HttpURLConnection) url.openConnection();


                                            connection.setDoInput(true);

                                            connection.connect();

                                            InputStream input = connection.getInputStream();


                                            Bitmap immutableBpm = BitmapFactory.decodeStream(input);

                                            Bitmap mutableBitmap = immutableBpm.copy(Bitmap.Config.ARGB_8888, true);


                                            View view = new View(appContext);

                                            view.draw(new Canvas(mutableBitmap));

                                            String path = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), mutableBitmap, "Nur", null);

                                            uri = Uri.parse(path);
                                            shareIntent.setType("image/png");

                                            //  Uri uri = Uri.parse("https://api.withfloats.com/" +imageShare);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                                            //   shareIntentToPackages("image/png",HomeActivity.StorebizFloats.get(position).message,uri);

                                            //}

                                            //}
                                        } catch (IOException e) {
                                            appContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Methods.showSnackBarNegative(appContext, "Error in Sharing");
                                                }
                                            });
                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            appContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Methods.showSnackBarNegative(appContext, "Error in Sharing");
                                                }
                                            });
                                            e.printStackTrace();
                                        }
                                    }else {
                                        Methods.showSnackBarNegative(appContext, "Can't Share image content in Offline Mode");
                                    }

                                } else {
                                    shareIntent.setType("text/plain");
                                    // shareIntentToPackages("text/plain",HomeActivity.StorebizFloats.get(position).message,null);
                                }

                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, HomeActivity.StorebizFloats.get(position).message);

                                appContext.startActivityForResult(Intent.createChooser(shareIntent, "Share your message"), 1);
                            }catch (Exception e) {e.printStackTrace();}
                        }
                    };
                    mThread.start();
                }

            });
          //  }

            if(Constants.isWelcomScreenToBeShown == true) {
                Constants.isWelcomScreenToBeShown = false ;
                data = HomeActivity.StorebizFloats.get(position - 1);
            } else {
                data = HomeActivity.StorebizFloats.get(position);
            }

            try {
                if (data != null) {
                    msg = data.message;
                    date = Methods.getFormattedDate(data.createdOn);
                    imageUri = data.tileImageUri;

                    String baseName = "";
                    textView1.setText(msg);
                    dateText.setText(date);

                    if(Util.isNullOrEmpty(imageUri) || imageUri.contains("deal.png"))
                    {
                        imagePresent = false ;
                        imageView.setVisibility(View.GONE);
                    }
                    else if(imageUri.contains("BizImages") )
                    {
                        imagePresent = true ;
                        imageView.setVisibility(View.VISIBLE);
                        baseName = Constants.BASE_IMAGE_URL + imageUri;
                        Picasso.with(appContext).load(baseName).placeholder(R.drawable.default_product_image).into(imageView);
//                        imageLoader.displayImage(baseName,imageView,options);
                    }
                    else if(imageUri.contains("/storage/emulated") || imageUri.contains("/mnt/sdcard") )
                    {
                        imagePresent = true;

                        imageView.setVisibility(View.VISIBLE);
                        Bitmap bmp = Util.getBitmap(imageUri.toString(),appContext);
                        imageView.setImageBitmap(bmp);
                    }
                    else
                    {
                        imagePresent = true ;
                        imageView.setVisibility(View.VISIBLE);
                        baseName = imageUri ;
                        Picasso.with(appContext).load(baseName).placeholder(R.drawable.default_product_image).into(imageView);
//                        imageLoader.displayImage(baseName,imageView,options);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }


    private void shareIntentToPackages(String type, String subject, Uri uri) {
        List<Intent> targetShareIntents = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(type);
        List<ResolveInfo> resInfos = appContext.getPackageManager().queryIntentActivities(shareIntent, 0);
        if (!resInfos.isEmpty()) {
            System.out.println("Have packages");
            for (ResolveInfo resInfo : resInfos) {
                String packageName = resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);
                if (!packageName.contains("com.twitter.android") && !packageName.contains("com.facebook.katana") )
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType(type);
                    intent.putExtra(Intent.EXTRA_TEXT, subject);
                    if(uri != null)
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }
            if (!targetShareIntents.isEmpty()) {
                System.out.println("Have Intent");
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                appContext.startActivity(chooserIntent);
            }
        }
    }


    public static void filterByPackageName(Context context, Intent intent, String prefix) {
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(prefix)) {
                intent.setPackage(info.activityInfo.packageName);
                return;
            }
        }
    }


    private List<String> getShareApplication(){
        List<String> mList=new ArrayList<String>();
        mList.add("com.facebook.katana");
        mList.add("com.twitter.android");
        mList.add("com.google.android.gm");
        return mList;

    }
    private void Share(List<String> PackageName,String Text) {
        try {
            List<Intent> targetedShareIntents = new ArrayList<Intent>();
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            List<ResolveInfo> resInfo = appContext.getPackageManager().queryIntentActivities(share, 0);
            if (!resInfo.isEmpty()) {
                for (ResolveInfo info : resInfo) {
                    Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                    targetedShare.setType("text/plain"); // put here your mime type
                    if (PackageName.contains(info.activityInfo.packageName.toLowerCase())) {
                        targetedShare.putExtra(Intent.EXTRA_TEXT, Text);
                        targetedShare.setPackage(info.activityInfo.packageName.toLowerCase());
                        targetedShareIntents.add(targetedShare);
                    }
                }
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                appContext.startActivity(chooserIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
        @Override
    public int getItemViewType(int position) {
        if(Constants.isWelcomScreenToBeShown == true) {
            return VIEW_TYPE_WELCOME;
        } else {
            return VIEW_TYPE_IMAGE_TEXT;
        }
    }

    @Override
    public int getItemCount() {
        if(Constants.isWelcomScreenToBeShown == true)
        {
            return 1;
        }
        else {
            return HomeActivity.StorebizFloats.size();
        }
    }
}