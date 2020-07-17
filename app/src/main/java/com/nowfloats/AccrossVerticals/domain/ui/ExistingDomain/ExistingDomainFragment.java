package com.nowfloats.AccrossVerticals.domain.ui.ExistingDomain;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinksity.R;

public class ExistingDomainFragment extends Fragment {

    private ExistingDomainViewModel mViewModel;

    public static ExistingDomainFragment newInstance() {
        return new ExistingDomainFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(ExistingDomainViewModel.class);
        return inflater.inflate(R.layout.existing_domain_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setheader
        setHeader(view);
    }

    public void setHeader(View view){
        LinearLayout backButton;
        TextView title;

        title = view.findViewById(R.id.title);
        backButton = view.findViewById(R.id.back_button);
        title.setText("Use Existing Domain");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }
}