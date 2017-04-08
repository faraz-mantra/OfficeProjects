package com.nowfloats.Analytics_Screen.Search_Query_Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nowfloats.Analytics_Screen.SearchRankingActivity;
import com.nowfloats.Analytics_Screen.model.SearchRankModel;
import com.nowfloats.util.Methods;
import com.thinksity.R;

import java.util.List;

/**
 * Created by NowFloats on 15-03-2017.
 */

public class SearchRankingRvAdapter extends RecyclerView.Adapter<SearchRankingRvAdapter.SearchRankingViewHolder> {

    private List<SearchRankModel> mSearchRankList;
    private int mFilter;
    private Context mContext;


    public SearchRankingRvAdapter(List<SearchRankModel> list, int filter, Context context){
        this.mSearchRankList = list;
        this.mFilter = filter;
        mContext = context;
    }

    @Override
    public SearchRankingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_query_item_layout, parent, false);
        return new SearchRankingViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        if((position % 2)==0){
            return 0;
        }
        return 1;
    }

    @Override
    public void onBindViewHolder(SearchRankingViewHolder holder, int position) {

        SearchRankModel data = mSearchRankList.get(position);
        if(null!=data) {
            holder.tvSearchQuery.setText(data.getKeyword());
            if (data.getOldRank() == -1) {
                holder.tvOldPage.setText("-");
            } else {
                holder.tvOldPage.setText(getPage(data.getOldRank()));
            }
            if (data.getNewRank() == -1) {
                holder.tvNewPage.setText("-");
            } else {
                holder.tvNewPage.setText(getPage(data.getNewRank()));
            }
            switch (mFilter){
                case SearchRankingActivity.FILTER_INCREASED:
                    holder.ivArrow.setVisibility(View.VISIBLE);
                    holder.ivArrow.setColorFilter(ContextCompat.getColor(mContext, R.color.green));
                    break;
                case SearchRankingActivity.FILTER_SAME:
                    holder.ivArrow.setVisibility(View.INVISIBLE);
                    break;
                case SearchRankingActivity.FILTER_DECREASED:
                    holder.ivArrow.setVisibility(View.VISIBLE);
                    holder.ivArrow.setColorFilter(Color.parseColor("#E02200"));
                    break;
                case SearchRankingActivity.FILTER_LOST:
                    holder.ivArrow.setVisibility(View.INVISIBLE);
                    break;
                case SearchRankingActivity.FILTER_NEW:
                    holder.ivArrow.setVisibility(View.INVISIBLE);

            }

        }
    }

    private String getPage(int rank){
        rank = ((rank-1)/10)+1;
        switch (rank){
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
            default:
                return String.valueOf(rank)+"th";
        }
    }

    @Override
    public int getItemCount() {
        return mSearchRankList.size();
    }

    class SearchRankingViewHolder extends RecyclerView.ViewHolder{

        TextView tvSearchQuery, tvOldPage, tvNewPage;
        ImageView ivArrow;

        public SearchRankingViewHolder(View itemView) {
            super(itemView);

            tvSearchQuery = (TextView) itemView.findViewById(R.id.tv_search_query);
            tvOldPage = (TextView) itemView.findViewById(R.id.tv_old_page_rank);
            tvNewPage = (TextView) itemView.findViewById(R.id.tv_new_rank);
            ivArrow = (ImageView) itemView.findViewById(R.id.iv_arrow);
        }
    }
}