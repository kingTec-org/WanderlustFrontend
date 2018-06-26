package eu.wise_iot.wanderlust.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.wise_iot.wanderlust.BuildConfig;
import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.controllers.ImageController;
import eu.wise_iot.wanderlust.models.DatabaseModel.Equipment;
import eu.wise_iot.wanderlust.services.GlideApp;


/**
 * Provides adapter for recycler view which is used to show equipment needed on an equipment
 *
 * @author Alexander Weinbeck
 * @license GPL-3.0
 */
public class EquipmentRVAdapter extends RecyclerView.Adapter<EquipmentRVAdapter.ViewHolder> {

    private static final String TAG = "EquipmentRVAdapter";
    private final List<Equipment> equipment;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final Context context;
    private final ImageController imageController;

    /**
     * data is passed into the constructor, here as a equipment
     * @param context
     * @param equipment
     */
    public EquipmentRVAdapter(Context context, List<Equipment> equipment) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Copy Constructor");
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.equipment = (equipment != null) ? equipment : new ArrayList<>();
        imageController = ImageController.getInstance();
    }

    /**
     * inflates the row layout from xml when needed
     * @param parent
     * @param viewType
     * @return an object which contains the created viewholder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Creating Viewholder");
        View view = mInflater.inflate(R.layout.recyclerview_tour_equipment, parent, false);
        return new ViewHolder(view);
    }

    /**
     * binds the data to the view and textview in each row
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "starting to set Viewholder properties");
        //set properties for each element
        Equipment equipment = this.equipment.get(position);
        //load image
        holder.tvTitle.setText(equipment.getName());

        GlideApp.with(context)
                .load(imageController.getImage(equipment.getImagePath()))
                .error(GlideApp.with(context).load(R.drawable.no_image_found).circleCrop())
                .placeholder(R.drawable.progress_animation)
                .circleCrop()
                .into(holder.ivImage);
//        }
    }

    /**
     * allows clicks events to be caught
     * @param itemClickListener
     */
    public void setClickListener(ItemClickListener itemClickListener) {
        mClickListener = itemClickListener;
    }

    /**
     * return total number of rows
     * @return
     */
    @Override
    public int getItemCount() {
        return equipment.size();
    }

    /**
     * convenience method for getting data at click position
     * @param id
     * @return
     */
    private Equipment getItem(int id) {
        return equipment.get(id);
    }

    /**
     * parent activity will implement this interface to respond to click events
     */
    public interface ItemClickListener {
        void onItemClick(View view, Equipment equipment);
    }

    /**
     * stores and recycles views as they are scrolled off screen
     * @author Alexander Weinbeck
     * @license GPL-3.0
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //properties list for each equipment part
        final TextView tvTitle;
        final ImageView ivImage;

        /**
         * copy constructor for each element which holds the view
         * @param itemView
         */
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.equipmentTitle);
            ivImage = itemView.findViewById(R.id.equipmentImage);
            if (BuildConfig.DEBUG) Log.d("DEBUG", "ViewHolder broken");
            itemView.setOnClickListener(this);
        }

        /**
         * click event handler
         * @param view
         */
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getItem(getAdapterPosition()));
        }
    }

}