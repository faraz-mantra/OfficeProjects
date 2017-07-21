package com.nowfloats.swipecard.service;

import android.util.Log;

import com.nowfloats.swipecard.models.MessageDO;
import com.nowfloats.swipecard.models.SMSSuggestions;
import com.nowfloats.util.Constants;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by NowFloats on 04-11-2016.
 */

public class SuggestionsApi {
    Bus mBus;

    public SuggestionsApi(Bus bus) {
        this.mBus = bus;
    }

    public void getMessages(HashMap<String, String> data) {
        SuggestionsInterface suggestionsInterface = Constants.pluginSuggestionsAdapter.create(SuggestionsInterface.class);
        suggestionsInterface.processSMSData(data, new Callback<SMSSuggestions>() {
            @Override
            public void success(SMSSuggestions suggestions, Response response) {
                mBus.post(suggestions);
            }

            @Override
            public void failure(RetrofitError error) {
                mBus.post(new SMSSuggestions());
            }
        });

    }

    public void updateMessage(List<MessageDO> data) {
        SuggestionsInterface suggestionsInterface = Constants.pluginSuggestionsAdapter.create(SuggestionsInterface.class);
        suggestionsInterface.saveCallData(data, new Callback<String>() {
            @Override
            public void success(String suggestions, Response response) {
//                mBus.post(suggestions);
                Log.e("suggestions", "suggestions=" + suggestions);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("suggestions", "suggestions=" + error.toString());
            }
        });

    }

    public void updateRating(HashMap<String, String> data) {
        SuggestionsInterface suggestionsInterface = Constants.pluginSuggestionsAdapter.create(SuggestionsInterface.class);
        suggestionsInterface.updateRating(data, new Callback<String>() {
            @Override
            public void success(String suggestions, Response response) {
                Log.e("suggestions", "suggestions=" + suggestions);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("suggestions", "suggestions=" + error.toString());
            }
        });

    }
}
