package com.aekan.navya.lpgbooking;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aekan.navya.lpgbooking.utilities.LPG_Utility;

import java.util.HashMap;

/**
 * Created by arunramamurthy on 19/06/17.
 */

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int COLOR_RED = 0xffed0e3a;
    private static final int COLOR_BLACK = Integer.parseInt("343435", 16);
    private Context mContext;
    private HashMap<Integer, Integer> mIconMap;
    private HashMap<Integer, String> mMenuNameMap;
    private HashMap<Integer, Integer> mNavItemTextColor;
    private boolean misPremiumUser = false;
    private HashMap<Integer, View.OnClickListener> mListenerAdapter;

    public NavigationAdapter(final Context context, final ListenerAdapter listenerHashMap) {
        Log.v("Recyclerview", "Constructor");

        //set String map of menu items
        String[] menuName = context.getResources().getStringArray(R.array.navigation_menuitems);
        mMenuNameMap = new HashMap<Integer, String>();
        for (int i = 0; i < menuName.length; ++i) {
            mMenuNameMap.put(Integer.valueOf(i), menuName[i]);
            Log.v("Recyclerview", " Menu Map " + mMenuNameMap.get(Integer.valueOf(i)));
        }

        //Set icon map for navigation items
        mIconMap = new HashMap<Integer, Integer>();
        mIconMap.put(new Integer(0), new Integer(0));
        mIconMap.put(new Integer(1), new Integer(R.drawable.ic_home_black_24dp));
        mIconMap.put(new Integer(2), new Integer(R.drawable.ic_add_circle_outline_black_24dp));
        mIconMap.put(new Integer(3), new Integer(R.drawable.ic_call_black_24dp));
        mIconMap.put(new Integer(4), new Integer(R.drawable.ic_textsms_black_24dp));
        mIconMap.put(new Integer(5), new Integer(R.drawable.ic_collections_bookmark_black_24dp));
        mIconMap.put(new Integer(6), new Integer(R.drawable.ic_share_black_48dp));
        mIconMap.put(new Integer(8 ),new Integer(R.drawable.ic_privacy_policy));
        mIconMap.put(new Integer(7),new Integer(R.drawable.ic_inbox_black_24dp));
        //set listener adapter
        mListenerAdapter = listenerHashMap.getmListenerAdapter();

    }

    public NavigationAdapter(final Context context, final ListenerAdapter listenerHashMap, final boolean isPremiumUser) {
        Log.v("Recyclerview", "Constructor with boolean");
        mContext = context;
        misPremiumUser = isPremiumUser;
        //set String map of menu items
        String[] menuName = context.getResources().getStringArray(R.array.navigation_menuitems);
        mMenuNameMap = new HashMap<Integer, String>();
        for (int i = 0; i < menuName.length; ++i) {
            mMenuNameMap.put(Integer.valueOf(i), menuName[i]);
            Log.v("Recyclerview", " Menu Map " + mMenuNameMap.get(Integer.valueOf(i)));
        }

        //Set icon map for navigation items
        mIconMap = new HashMap<Integer, Integer>();
        mIconMap.put(new Integer(0), new Integer(0));
        mIconMap.put(new Integer(1), new Integer(R.drawable.ic_home_black_24dp));
        if (isPremiumUser) {
            mIconMap.put(new Integer(2), new Integer(R.drawable.ic_add_circle_outline_black_24dp));
        } else {
            mIconMap.put(new Integer(2), new Integer(R.drawable.ic_add_connection_nav_bar_non_premium));
        }

        mIconMap.put(new Integer(3), new Integer(R.drawable.ic_call_black_24dp));
        mIconMap.put(new Integer(4), new Integer(R.drawable.ic_textsms_black_24dp));
        mIconMap.put(new Integer(5), new Integer(R.drawable.ic_collections_bookmark_black_24dp));
        mIconMap.put(new Integer(6), new Integer(R.drawable.ic_share_black_48dp));
        mIconMap.put(new Integer(7),new Integer(R.drawable.ic_inbox_black_24dp));
        mIconMap.put(new Integer(8 ),new Integer(R.drawable.ic_privacy_policy));

        //set listener adapter
        mListenerAdapter = listenerHashMap.getmListenerAdapter();

        //get navigation item text color hashmap;
        mNavItemTextColor = new HashMap<Integer, Integer>();
        for (int i = 0; i < mIconMap.size(); ) {
            mNavItemTextColor.put(new Integer(i++), new Integer(R.color.defaultBlack));
        }
        if (!(isPremiumUser)) {
            mNavItemTextColor.put(new Integer(2), new Integer(R.color.error_red));
        }
    }

    //override get item view type
    @Override
    public int getItemViewType(int position) {
        Log.v("Recyclerview", "Position inside getItemViewtype " + position);
        int itemViewType = 0;
        if ((position) == 0) {
            return LPG_Utility.NAVDRAWER_HEADER;
        }
        return LPG_Utility.NAVDRAWER_LINEITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v("Recyclerview", " View type is " + viewType);
        switch (viewType) {
            case LPG_Utility.NAVDRAWER_HEADER:
                View navHeader = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_header, parent, false);
                return new NavigationAdapter.HeaderViewHolder(navHeader);

            case LPG_Utility.NAVDRAWER_LINEITEM:
                View navLineItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_lineitem, parent, false);
                return new NavigationAdapter.LineItemViewHolder(navLineItem);

            default:
                Log.v("Recyclerview", " No valid view type");
                return null;
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.v("Recyclerview", " On Bind position " + position);
        Log.v("Recyclerview", " On Bind view Type " + holder.getItemViewType());
        switch (holder.getItemViewType()) {
            case LPG_Utility.NAVDRAWER_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                //set header name
                String headerName = new String();
                if (mMenuNameMap.containsKey(Integer.valueOf(position))) {
                    headerName = mMenuNameMap.get(Integer.valueOf(position));
                } else {
                    headerName = "Home";
                }

                headerViewHolder.mTextView.setText(headerName);
                break;
            case LPG_Utility.NAVDRAWER_LINEITEM:
                LineItemViewHolder lineItemViewHolder = (LineItemViewHolder) holder;
                //set line item name
                String lineItemName = new String();
                if (mMenuNameMap.containsKey(Integer.valueOf(position))) {
                    lineItemName = mMenuNameMap.get(new Integer(position));
                } else {
                    lineItemName = "Home";
                }
                lineItemViewHolder.mMenuItem.setText(lineItemName);
                lineItemViewHolder.mMenuItem.setOnClickListener(mListenerAdapter.get(Integer.valueOf(position)));
                //set icon
                int resIconId;
                if (mIconMap.containsKey(Integer.valueOf(position))) {
                    resIconId = mIconMap.get(Integer.valueOf(position)).intValue();
                } else {
                    resIconId = R.drawable.ic_home_black_24dp;
                }
                lineItemViewHolder.mIcon.setImageResource(resIconId);
                //set font color
                if (!(misPremiumUser) && (position == 2)) {
                    lineItemViewHolder.mMenuItem.setTextColor(COLOR_RED);
                }
//                lineItemViewHolder.mMenuItem.setTextColor(mContext.getResources().getColor( mNavItemTextColor.get(Integer.valueOf(position)).intValue(),null));
                lineItemViewHolder.mIcon.setOnClickListener(mListenerAdapter.get(Integer.valueOf(position)));
                //set onclick listener
                
                break;

        }

    }

    @Override
    public int getItemCount() {
        return mMenuNameMap.size();
    }

    public interface ListenerAdapter {
        HashMap<Integer, View.OnClickListener> getmListenerAdapter();

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
