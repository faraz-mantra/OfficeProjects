package com.nowfloats.NavigationDrawer;

import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.nowfloats.BusinessProfile.UI.UI.Business_Logo_Activity;
import com.nowfloats.BusinessProfile.UI.UI.FaviconImageActivity;
import com.nowfloats.BusinessProfile.UI.UI.FeaturedImageActivity;
import com.nowfloats.Image_Gallery.BackgroundImageGalleryActivity;
import com.nowfloats.Image_Gallery.ImageGalleryActivity;
import com.nowfloats.Store.Model.OnItemClickCallback;
import com.nowfloats.Store.SimpleImageTextListAdapter;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.MixPanelController;
import com.thinksity.R;
import com.thinksity.databinding.ActivityImageMenuBinding;

public class ImageMenuActivity extends AppCompatActivity
{
    private ActivityImageMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_menu);

        setSupportActionBar(binding.layoutToolbar.toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle("");
        }

        binding.layoutToolbar.toolbarTitle.setText("All Images");
        initMenuRecyclerView(binding.menuList);
    }

    /**
     * Initialize pickup address list adapter
     * @param mRecyclerView
     */
    private void initMenuRecyclerView(RecyclerView mRecyclerView)
    {
        final String[] adapterTexts = getResources().getStringArray(R.array.images_content_list_items);
        final TypedArray imagesArray = getResources().obtainTypedArray(R.array.image_list_icons);
        int[] adapterImages = new int[adapterTexts.length];

        for (int i = 0; i<adapterTexts.length;i++)
        {
            adapterImages[i] = imagesArray.getResourceId(i,-1);
        }

        imagesArray.recycle();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        SimpleImageTextListAdapter adapter = new SimpleImageTextListAdapter(this, new OnItemClickCallback() {

            @Override
            public void onItemClick(int pos)
            {
                Intent intent = null;

                switch(adapterTexts[pos])
                {
                    case "Business Logo":

                        MixPanelController.track(EventKeysWL.LOGO, null);
                        intent = new Intent(ImageMenuActivity.this, Business_Logo_Activity.class);
                        break;

                    case "Featured Image":

                        intent = new Intent(ImageMenuActivity.this, FeaturedImageActivity.class);
                        break;

                    case "Image Gallery":

                        intent = new Intent(ImageMenuActivity.this, ImageGalleryActivity.class);
                        break;

                    case "Background Images":

                        intent = new Intent(ImageMenuActivity.this, BackgroundImageGalleryActivity.class);
                        break;

                    case "Favicon":

                        intent = new Intent(ImageMenuActivity.this, FaviconImageActivity.class);
                        break;
                }

                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        adapter.setItems(adapterImages,adapterTexts);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:

                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}