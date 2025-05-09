package com.example.ticketexpress;

import android.content.ContentUris;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<ShoppingItem> mShoppingItemData;
    private ArrayList<ShoppingItem> mShoppingItemDataAll;
    private Context mContext;
    private int lastPosition = -1;

    private boolean isCartView = false;

    public ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemsData, boolean isCartView) {
        this.mShoppingItemData = itemsData;
        this.mShoppingItemDataAll = itemsData;
        this.mContext = context;
        this.isCartView = isCartView;
    }

    public ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemsData) {
        this(context, itemsData, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ShoppingItemAdapter.ViewHolder holder, int position) {
        ShoppingItem currentItem = mShoppingItemData.get(position);

        holder.bindTo(currentItem);

        if (holder.getAdapterPosition() > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mShoppingItemData.size();
    }

    @Override
    public Filter getFilter() {
        return shoppingFilter;
    }

    private Filter shoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ShoppingItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (charSequence == null || charSequence.length() == 0){
                results.count = mShoppingItemData.size();
                results.values = mShoppingItemDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (ShoppingItem item : mShoppingItemDataAll){
                    if (item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mShoppingItemData = (ArrayList) filterResults.values;
            notifyDataSetChanged();
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////


    //ViewHolder osztály
    class ViewHolder extends  RecyclerView.ViewHolder {
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private RatingBar mRatingBar;
        private TextView quantityText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mPriceText = itemView.findViewById(R.id.price);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);

            quantityText = itemView.findViewById(R.id.quantityText);
        }

        public void bindTo(ShoppingItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mRatingBar.setRating(currentItem.getRatedInfo());

            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);

            View addToCartBtn = itemView.findViewById(R.id.add_to_cart);
            View deleteBtn = itemView.findViewById(R.id.delete);

            if (isCartView) {
                addToCartBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
                quantityText.setVisibility(View.VISIBLE);
                quantityText.setText("Mennyiség: " + currentItem.getCartedCount() + " db");
            } else {
                addToCartBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
                quantityText.setVisibility(View.GONE);

                addToCartBtn.setOnClickListener(view -> {
                    CartManager.getInstance().addItem(currentItem);
                    ((ShopListActivity) mContext).updateAlertIcon(currentItem);
                    Toast.makeText(mContext, currentItem.getName() + " hozzáadva a kosárhoz", Toast.LENGTH_SHORT).show();
                });

                deleteBtn.setOnClickListener(view -> ((ShopListActivity) mContext).deleteItem(currentItem));
            }
        }
    };
}

