package com.nowfloats.signup.UI.UI;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.API.GetVisitorsAndSubscribersCountAsyncTask;
import com.nowfloats.NavigationDrawer.HomeActivity;
import com.nowfloats.NavigationDrawer.Mobile_Site_Activity;
import com.nowfloats.signup.UI.API.Download_Facebook_Image;
import com.nowfloats.signup.UI.API.Signup_Descriptinon;
import com.nowfloats.signup.UI.Model.Create_Store_Event;
import com.nowfloats.signup.UI.Model.Get_FP_Details_Event;
import com.nowfloats.signup.UI.Service.Create_Tag_Service;
import com.nowfloats.signup.UI.Service.Get_FP_Details_Service;
import com.nowfloats.signup.UI.Service.Verify_Tag_Service;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.BusProvider;
import com.nowfloats.util.Constants;
import com.nowfloats.util.DataBase;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.MixPanelController;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.thinksity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class WebSiteAddressActivity extends AppCompatActivity  {
    private Toolbar toolbar;
    Button createButton ;
    boolean domainCheck, firstCheck = true;
    String data_businessName,data_businessCategory,data_city,data_country,data_email,data_phone, websiteTag;
    JSONObject businessDetails_jsonData;
    EditText webSiteTextView ;
    CardView webSiteCardView ;
    public static ProgressDialog pd ;
    TextView label;
    Set<String> rTags, xTags;
    private String contactName = "contact";
    String aTag, mtag, aName, val, beforeEdit, intialValue;
    ImageView domainCheckStatus;
    CustomRunnable r;
    Handler handler;
    int regex;
    boolean addressTagValid = false;
    private String countrycodeTag;
    Bus bus;
    UserSessionManager session ;
    private DataBase dataBase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_site_address);
        dataBase = new DataBase(WebSiteAddressActivity.this);
        createButton = (Button) findViewById(R.id.createButton);
        webSiteTextView = (EditText) findViewById(R.id.websiteTitleTextView);

        webSiteCardView = (CardView) findViewById(R.id.websiteTitleCardView);

        session = new UserSessionManager(getApplicationContext(),WebSiteAddressActivity.this);

        bus = BusProvider.getInstance().getBus();

//        webSiteCardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(Util.isNetworkStatusAvialable(WebSiteAddressActivity.this))
//                {
//                    domainCheck();
//
//                }
//                else{
//                    Toast.makeText(WebSiteAddressActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


        label = (TextView) findViewById(R.id.domainAvailable);
        domainCheckStatus = (ImageView)findViewById(R.id.domainCheckStatus);
        getEditTextBundle();
        if(Util.isNetworkStatusAvialable(WebSiteAddressActivity.this))
        {
            domainCheck();
        }
        else{
            Toast.makeText(WebSiteAddressActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
        }
        rTags = new HashSet<String>();
        xTags = new HashSet<String>();

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webSiteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                domainCheck = true;
                if(Util.isNetworkStatusAvialable(WebSiteAddressActivity.this))
                {
                    domainCheck();

                }
                else{
                    Toast.makeText(WebSiteAddressActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                }

                if (firstCheck) {
                    intialValue = webSiteTextView.getText().toString();
                }

            }
        });



        TextView policyTextView = (TextView) findViewById(R.id.policyTextView);
        policyTextView.setPaintFlags( Paint.UNDERLINE_TEXT_FLAG);
        policyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url =   "http://nowfloats.com/privacy/";   ;//"https://www.facebook.com/thinksity";
                Intent showWebSiteIntent = new Intent(WebSiteAddressActivity.this,Mobile_Site_Activity.class);
                // showWebSiteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                showWebSiteIntent.putExtra("WEBSITE_NAME", url);
                startActivity(showWebSiteIntent);
            }
        });
        createButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    //MixPanelController.track(EventKeysWL.WEBSITE_ADDRESS_SCREEN_EDIT_DOMAIN,null);
                    // getEditTextBundle();

                    if(addressTagValid){
                        MixPanelController.track("CreateMyWebsite", null);
                         createStore_retrofit(WebSiteAddressActivity.this,getJSONData(),bus);
//                        CreateStoreTask createStore = new CreateStoreTask(getJSONData(),WebSiteAddressActivity.this);
//                        createStore.setCreateTaskInterfaceListener(WebSiteAddressActivity.this);
//                        createStore.execute();


                    }



                }

                return false;
            }
        });
    }

    private void createStore_retrofit(WebSiteAddressActivity webSiteAddressActivity, HashMap<String, String> jsonData, Bus bus) {

        pd = ProgressDialog.show(WebSiteAddressActivity.this, "", "Creating Website ...");
        pd.setCancelable(false);
        new Create_Tag_Service(webSiteAddressActivity,jsonData,bus);
    }

    @Subscribe
    public void put_createStore(Create_Store_Event response)
    {
        final String fpId = response.fpId ;

        dataBase.insertLoginStatus(fpId);
        UserSessionManager session = new UserSessionManager(getApplicationContext(),WebSiteAddressActivity.this);
        session.storeFPID(fpId);
        session.storeSourceClientId(Constants.clientId);
        //thinksity check
        if(Constants.clientId.equals(Constants.clientIdThinksity)){
              session.storeIsThinksity("true");
        }

        // Give a Delay of 4 Seconds and Call this method
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over Start your app main activity
                getFPDetails(WebSiteAddressActivity.this,fpId,Constants.clientId,bus);
            }
        }, 8000);

        // Store it in Database
        // Store it in Shared pref
    }

    private void getFPDetails(WebSiteAddressActivity activity, String fpId, String clientId, Bus bus) {
        new Get_FP_Details_Service(activity,fpId,clientId,bus);
    }

    @Subscribe
    public void post_getFPDetails(Get_FP_Details_Event response)
    {
        // Close of Progress Bar
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(pd!=null){
                            pd.dismiss();
                        }
                    }
                });
        //VISITOR and SUBSCRIBER COUNT API
        GetVisitorsAndSubscribersCountAsyncTask visit_subcribersCountAsyncTask = new GetVisitorsAndSubscribersCountAsyncTask(WebSiteAddressActivity.this,session);
        visit_subcribersCountAsyncTask.execute();
        if(session.getIsSignUpFromFacebook().contains("true")) {
            if (session.getFacebookName()!=null && session.getFacebookAccessToken()!=null){
                dataBase.updateFacebookNameandToken(session.getFacebookName(),session.getFacebookAccessToken());
            }
            if (session.getFacebookPage()!=null && session.getFacebookPageID()!=null &&session.getPageAccessToken()!=null){
                dataBase.updateFacebookPage(session.getFacebookPage(), session.getFacebookPageID(), session.getPageAccessToken());
            }
            new Download_Facebook_Image().execute(session.getFacebookPageURL(), session.getFPID());
            Signup_Descriptinon descriptinon = new Signup_Descriptinon(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG),
                    session.getFacebookProfileDescription(),
                    session.getFacebookPageID(),
                    response.model.FPWebWidgets
                 );
            descriptinon.execute();
        }

        Intent webIntent = new Intent(WebSiteAddressActivity.this, HomeActivity.class);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle loginBundle = new Bundle();
        loginBundle.putBoolean("fromLogin", true);
        Constants.isWelcomScreenToBeShown = true;
        webIntent.putExtras(loginBundle);
        startActivity(webIntent);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void getEditTextBundle() {
        Intent signUpIntent = getIntent();
        businessDetails_jsonData = new JSONObject();
        data_businessName = signUpIntent.getStringExtra("signup_business_name");
        data_businessCategory = signUpIntent.getStringExtra("signup_business_category");
        data_city = signUpIntent.getStringExtra("signup_city");
        data_country = signUpIntent.getStringExtra("signup_country");
        data_email = signUpIntent.getStringExtra("signup_email");
        data_phone = signUpIntent.getStringExtra("signup_phone");
        websiteTag = signUpIntent.getStringExtra("tag");
        countrycodeTag = signUpIntent.getStringExtra("signup_country_code");

        beforeEdit = websiteTag.toLowerCase();
        webSiteTextView.setText(websiteTag.toLowerCase());
        webSiteTextView.setSelection(webSiteTextView.getText().length());
    }


    private HashMap<String,String> getJSONData()
    {
        HashMap<String, String> store = new HashMap<String, String>();
        try {
            store.put("clientId", Constants.clientId);
            store.put("tag",websiteTag);
            store.put("contactName",contactName);
            store.put("name",data_businessName);
            store.put("desc","");
            store.put("address",data_city);
            store.put("city",data_city);
            store.put("pincode","");
            store.put("country",data_country);
            store.put("primaryNumber",data_phone);
            store.put("email",data_email);
            store.put("primaryNumberCountryCode",countrycodeTag);
            store.put("Uri","");
            store.put("fbPageName","");
            store.put("primaryCategory",data_businessCategory);
            store.put("lat","0");
            store.put("lng","0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return store ;

        // {"clientId":"138440FB190A4CDA998274F52952B0657465B03A177C499DA495372764CE185F",
        //  "tag":"ANUPAMRETAIL",
        // "contactName":"Anupam Retail Ltd.",
        // "name":"Anupam Retail Ltd.",
        // "desc":"Signed for BSP with Infomedia",
        // "address":"Delhi",
        // "city":"Delhi",
        // "pincode":"110028",
        // "country":"India",
        // "primaryNumber":"1125893883",
        // "email":"kavita@anupamsinks.com",
        // "primaryNumberCountryCode":"+91",
        // "Uri":"",
        // "fbPageName":"",
        // "primaryCategory":"OTHER RETAIL",
        // "lat":0,
        // "lng":0}
    }

    public void createStore()
    {

        String error = uploadStore();
        if(error.equals("OK"))
        {
            //ok
        }
        else
        {
            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                    this);
            builderInner.setMessage("Please try again ."+error);
            builderInner.setTitle("Error!");
            builderInner.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            dialog.dismiss();

                        }
                    });
            builderInner.show();
        }
        //	}

    }


    public void verifyTag(String tagname) {
        RequestQueue queue = Volley.newRequestQueue(WebSiteAddressActivity.this);
        String url = Constants.NOW_FLOATS_API_URL+"/Discover/v1/floatingPoint/verifyUniqueTag";
        JSONObject obj = new JSONObject();
        try {
            obj.put("fpTag", tagname);
            obj.put("fpName", tagname);
            obj.put("clientId", Constants.clientId);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST,
                url, obj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String mesg = error.getMessage();
                if (mesg!=null && mesg.contains("org.json.JSONException: End of input at character 0 of")) {
                    // invalid tag
                    xTags.add(mtag);
                    label.setText("Chosen website address is not available!");
                    domainCheck = false;
                    addressTagValid = false;

                    domainCheckStatus.setVisibility(View.VISIBLE);
                    domainCheckStatus.setImageDrawable(getResources().getDrawable(R.drawable.domain_not_available));
                } else if (mesg!=null && mesg
                        .contains("type java.lang.String cannot be converted to JSONObject")) {
                    mesg = mesg.replace(
                            "org.json.JSONException: Value ", "");
                    mesg = mesg
                            .replace(
                                    " of type java.lang.String cannot be converted to JSONObject",
                                    "");
                    mesg = mesg.trim();
                    rTags.add(mtag);
                    if (mtag.equals(mesg)) {
                        // valid tag
                        aTag = mesg;
                        websiteTag = mesg;
                        addressTagValid = true;

                        label.setText("Chosen website address is available!");
                        domainCheck = false;
                        //domainCheckPD.setVisibility(View.GONE);
                        domainCheckStatus.setVisibility(View.VISIBLE);
                        domainCheckStatus.setImageDrawable(getResources().getDrawable(R.drawable.domain_available));
                    } else {
                        rTags.add(mesg);
                        aTag = mesg;
                        addressTagValid = false;
                        label.setText("Chosen website address is not available!");
                        domainCheck = false;
                        // domainCheckPD.setVisibility(View.GONE);
                        domainCheckStatus.setVisibility(View.VISIBLE);
                        domainCheckStatus
                                .setImageDrawable(getResources().getDrawable(R.drawable.domain_not_available));
                    }
                }
            }
        });

        queue.add(jsObjRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    public boolean validate(String text) {
        regex = R.string.signup_subd;
        if (text.matches(WebSiteAddressActivity.this.getResources().getString(regex))) {
            return true;
        } else {
            return false;
        }
    }

    public void domainCheck() {
        if (firstCheck) {
            //domainCheckPD.setVisibility(View.VISIBLE);
            domainCheckStatus.setVisibility(View.GONE);
            label.setText("Checking if chosen website Address is Available.");
            if (handler == null) {
                handler = new Handler();
                r = new CustomRunnable(beforeEdit) {
                    public void run() {
                        val = webSiteTextView.getText().toString();
                        if (beforeEdit.equals(val)) {
                            if (validate(val)) {
                                String ttag = val.toUpperCase();
                                if (rTags.contains(ttag)) {
                                    aTag = ttag;
                                    label.setText("Chosen website address is available!");
                                    domainCheck = false;
                                    // domainCheckPD.setVisibility(View.GONE);
                                    domainCheckStatus.setVisibility(View.VISIBLE);
                                    domainCheckStatus.setBackgroundResource(R.drawable.domain_available);
                                } else if (xTags.contains(ttag)) {
                                    // invalid tag
                                    mtag = ttag;
                                    label.setText("Chosen website address is not available!");
                                    domainCheck = false;
                                    //domainCheckPD.setVisibility(View.GONE);
                                    domainCheckStatus.setVisibility(View.VISIBLE);
                                    domainCheckStatus.setImageDrawable(getResources().getDrawable(R.drawable.domain_not_available));
                                } else {
                                    mtag = ttag;

                                   // verifyTag_RetroFit(WebSiteAddressActivity.this,mtag,bus);
                                    verifyTag(ttag);
                                }
                            } else {
                                label.setText("Please enter a valid website Name");
                                domainCheck = false;
                                //domainCheckPD.setVisibility(View.GONE);
                                domainCheckStatus.setVisibility(View.VISIBLE);
                                domainCheckStatus.setBackgroundResource(R.drawable.domain_not_available);
                            }
                        } else {
                        }

                    }
                };
                beforeEdit = webSiteTextView.getText().toString();
                handler.postDelayed(r, 1000);

            } else {
                handler.removeCallbacks(r);
                beforeEdit = webSiteTextView.getText().toString();
                handler.postDelayed(r, 1000);
            }
        }
    }

    private void verifyTag_RetroFit(WebSiteAddressActivity webSiteAddressActivity, String mtag, Bus bus) {
        new Verify_Tag_Service(webSiteAddressActivity,mtag,"",Constants.clientId,bus);
    }


//    @Subscribe
//    public void post_VerifyTag(Verifty_Unique_Tag_Event response)
//    {
//        fpTag = response.uniqueFpTag ;
//    }


    private String uploadStore() {

        try {
            String error = "";
            JSONObject store = new JSONObject();
            if(webSiteTextView != null)
            {
                store.put("tag",webSiteTextView.toString());
            }

            if(data_businessName != null)
            {
                store.put("name",data_businessName.toString());
            }
            if(data_city != null)
            {
                store.put("city",data_city);
            }
            if(data_country != null)
            {
                store.put("country",data_country);
            }
            if(data_phone != null )
            {
                store.put("phone",data_phone);
            }
            if(data_email != null)
            {
                store.put("email",data_email);
            }

            if(data_businessCategory != null)
            {
                store.put("businessCategory",data_businessCategory);
            }

//            CreateStoreTask task = new CreateStoreTask(this,store);
//            //task.onObserverListener = this;
//              task.execute();



        } catch(Exception e)
        {
            return "error-csj001";
        }


        return null ;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_site_address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }
}
