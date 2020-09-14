package com.nowfloats.NavigationDrawer;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nowfloats.AccrossVerticals.Testimonials.TestimonialsActivity;
import com.nowfloats.CustomPage.CustomPageActivity;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.businessApps.FragmentsFactoryActivity;
import com.nowfloats.ProductGallery.ProductCatalogActivity;
import com.nowfloats.Store.Model.OnItemClickCallback;
import com.nowfloats.Store.SimpleImageTextListAdapter;
import com.nowfloats.education.batches.BatchesActivity;
import com.nowfloats.education.faculty.FacultyActivity;
import com.nowfloats.education.toppers.ToppersActivity;
import com.nowfloats.hotel.placesnearby.PlacesNearByActivity;
import com.nowfloats.hotel.seasonalOffers.SeasonalOffersActivity;
import com.nowfloats.hotel.tripadvisor.TripAdvisorActivity;
import com.nowfloats.manufacturing.digitalbrochures.DigitalBrochuresActivity;
import com.nowfloats.manufacturing.projectandteams.ui.home.ProjectAndTermsActivity;
import com.nowfloats.util.Utils;
import com.nowfloats.util.WebEngageController;
import com.thinksity.R;

/**
 * Created by Admin on 29-01-2018.
 */

public class ManageContentFragment extends Fragment {
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upgrade, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isAdded()) return;

        UserSessionManager session = new UserSessionManager(getContext(), getActivity());
        String experience_code = session.getFP_AppExperienceCode();

        int content_tabs_resource, image_tabs_resource;
        switch (experience_code) {
            case "HOT":
                content_tabs_resource = R.array.manage_content_tab_items_hot;
                image_tabs_resource = R.array.manage_content_sidepanel_hot;
                break;
            case "MFG":
                content_tabs_resource = R.array.manage_content_tab_items_mfg;
                image_tabs_resource = R.array.manage_content_sidepanel_mfg;
                break;
            case "EDU":
                content_tabs_resource = R.array.manage_content_tab_items_edu;
                image_tabs_resource = R.array.manage_content_side_panel_edu;
                break;
            default:
                content_tabs_resource = R.array.manage_content_tab_items;
                image_tabs_resource = R.array.manage_content_sidepanel;
                break;
        }
        final String[] adapterTexts = getResources().getStringArray(content_tabs_resource);
        adapterTexts[0] = Utils.getProductCatalogTaxonomyFromServiceCode(session.getFP_AppExperienceCode());
        adapterTexts[1] = Utils.getLatestUpdatesTaxonomyFromServiceCode(session.getFP_AppExperienceCode());

        final TypedArray imagesArray = getResources().obtainTypedArray(image_tabs_resource);
        int[] adapterImages = new int[adapterTexts.length];
        for (int i = 0; i < adapterTexts.length; i++) {
            adapterImages[i] = imagesArray.getResourceId(i, -1);
        }
        imagesArray.recycle();
        RecyclerView mRecyclerView = view.findViewById(R.id.rv_upgrade);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        SimpleImageTextListAdapter adapter = new SimpleImageTextListAdapter(mContext, new OnItemClickCallback() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = null;
                switch (pos) {
                    case 0:
                        WebEngageController.trackEvent("MANAGE CONTENT", "Clicked on products catalogue", session.getFpTag());
                        intent = new Intent(mContext, ProductCatalogActivity.class);
                        break;
                    case 1:
                        ((SidePanelFragment.OnItemClickListener) mContext).onClick(getString(R.string.update));
                        return;
                    case 2:
                        intent = new Intent(mContext, ImageMenuActivity.class);
                        break;
                    case 3:
                        intent = new Intent(mContext, FragmentsFactoryActivity.class);
                        intent.putExtra("fragmentName", "Business_Profile_Fragment_V2");
                        break;
                    case 4:
                        if (experience_code.equals("EDU")) intent = new Intent(mContext, CustomPageActivity.class);
                        else intent = new Intent(mContext, TestimonialsActivity.class);
                        break;
                    case 5:
                        if (experience_code.equals("EDU")) {
                            intent = new Intent(mContext, ToppersActivity.class);
                        } else intent = new Intent(mContext, CustomPageActivity.class);
                        break;
                    case 6:
                        if (experience_code.equals("HOT")) {
                            intent = new Intent(mContext, PlacesNearByActivity.class);
                        } else if (experience_code.equals("MFG")) {
                            intent = new Intent(mContext, ProjectAndTermsActivity.class);
                        } else if (experience_code.equals("EDU")) {
                            intent = new Intent(mContext, BatchesActivity.class);
                        }
                        break;
                    case 7:
                        if (experience_code.equals("HOT")) {
                            intent = new Intent(mContext, TripAdvisorActivity.class);
                        } else if (experience_code.equals("MFG")) {
                            intent = new Intent(mContext, DigitalBrochuresActivity.class);
                        } else if (experience_code.equals("EDU")) {
                            intent = new Intent(mContext, FacultyActivity.class);
                        }
                        break;
                    case 8:
                        if (experience_code.equals("HOT")) {
                            intent = new Intent(mContext, SeasonalOffersActivity.class);
                        }
                        break;
                    default:
                        return;
                }
                if (intent != null) {
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        adapter.setItems(adapterImages, adapterTexts);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext instanceof HomeActivity && HomeActivity.headerText != null) {
            HomeActivity.headerText.setText(getString(R.string.manage_content));
        }
    }
}
