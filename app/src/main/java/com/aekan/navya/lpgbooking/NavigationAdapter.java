package com.aekan.navya.lpgbooking;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.aekan.navya.lpgbooking.utilities.LPG_Utility;

import java.util.HashMap;

/**
 * Created by arunramamurthy on 19/06/17.
 */

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private HashMap<Integer, Integer> mIconMap;
    private HashMap<Integer, String> mMenuNameMap;
    private HashMap<Integer, AdapterView.OnItemClickListener> mListenerAdapter;

    public NavigationAdapter(final Context context, final ListenerAdapter listenerHashMap) {
        //set String map of menu items
        String[] menuName = context.getResources().getStringArray(R.array.navigation_menuitems);
        mMenuNameMap = new HashMap<Integer, String>();
        for (int i = 0; i < mMenuNameMap.size(); ++i) {
            mMenuNameMap.put(new Integer(i), menuName[i]);
        }

        //Set icon map for navigation items
        mIconMap = new HashMap<Integer, Integer>();
        mIconMap.put(new Integer(0), new Integer(0));
        mIconMap.put(new Integer(1), new Integer(R.drawable.ic_home_black_24dp));
        mIconMap.put(new Integer(2), new Integer(R.drawable.ic_add_circle_outline_black_24dp));
        mIconMap.put(new Integer(3), new Integer(R.drawable.ic_call_black_36dp));
        mIconMap.put(new Integer(4), new Integer(R.drawable.ic_textsms_black_24dp));
        mIconMap.put(new Integer(5), new Integer(R.drawable.ic_collections_bookmark_black_24dp));
        //set listener adapter
        mListenerAdapter = listenerHashMap.getmListenerAdapter();

    }

    public void setmListenerAdapter(HashMap<Integer, AdapterView.OnItemClickListener> listener) {
        mListenerAdapter = listener;
    }

    //override get item view type
    @Override
    public int getItemViewType(int position) {
        int itemViewType = 0;
        if ((position - 1) == 0) {
            return LPG_Utility.NAVDRAWER_HEADER;
        }
        return LPG_Utility.NAVDRAWER_LINEITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case LPG_Utility.NAVDRAWER_HEADER:
                View navHeader = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_header, parent);
                return new NavigationAdapter.HeaderViewHolder(navHeader);

            case LPG_Utility.NAVDRAWER_LINEITEM:
                View navLineItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_lineitem, parent);
                return new NavigationAdapter.LineItemViewHolder(navLineItem);

            default:
                return null;
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case LPG_Utility.NAVDRAWER_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                //set header name
                String headerName = new String();
                if ((position - 1) >= 0) {
                    headerName = mMenuNameMap.get(new Integer(position - 1));
                } else {
                    headerName = "Home";
                }

                headerViewHolder.mTextView.setText(headerName);
                break;
            case LPG_Utility.NAVDRAWER_LINEITEM:
                LineItemViewHolder lineItemViewHolder = (LineItemViewHolder) holder;
                //set line item name
                String lineItemName = new String();
                if ((position - 1) >= 0) {
                    lineItemName = mMenuNameMap.get(new Integer(position - 1));
                } else {
                    lineItemName = "Home";
                }
                lineItemViewHolder.mMenuItem.setText(lineItemName);
                //set icon
                int resIconId;
                if (mIconMap.containsKey(new Integer(position - 1))) {
                    resIconId = mIconMap.get(new Integer(position - 1)).intValue();
                } else {
                    resIconId = R.drawable.ic_home_black_24dp;
                }
                lineItemViewHolder.mIcon.setImageResource(resIconId);
                //set onclick listener
                
                break;

        }

    }

    @Override
    public int getItemCount() {
        return mMenuNameMap.size();
    }

    public interface ListenerAdapter {
        HashMap<Integer, AdapterView.OnItemClickListener> getmListenerAdapter();

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public HeaderViewHolder(View view) {
            super(view);

            mTextView = (TextView) view.findViewById(R.id.navdrawer_header);
        }
    }

    public static class LineItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIcon;
        private TextView mMenuItem;

        public LineItemViewHolder(View v) {
            super(v);

            mIcon = (ImageView) v.findViewById(R.id.navdrawer_icon_lineitem);
            mMenuItem = (TextView) v.findViewById(R.id.navdrawer_menuitem);

        }
    }
}
