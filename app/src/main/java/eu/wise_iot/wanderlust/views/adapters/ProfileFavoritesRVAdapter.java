package eu.wise_iot.wanderlust.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import eu.wise_iot.wanderlust.BuildConfig;
import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.controllers.EventType;
import eu.wise_iot.wanderlust.controllers.ImageController;
import eu.wise_iot.wanderlust.controllers.TourController;
import eu.wise_iot.wanderlust.models.DatabaseModel.ImageInfo;
import eu.wise_iot.wanderlust.models.DatabaseModel.Tour;
import eu.wise_iot.wanderlust.services.ServiceGenerator;
import eu.wise_iot.wanderlust.views.ProfileFragment;

/**
 * Adapter for the profile UI. Represents all favorites in a custom list view
 *
 * @author Alexander Weinbeck
 * @license MIT
 */
public class ProfileFavoritesRVAdapter extends RecyclerView.Adapter<ProfileFavoritesRVAdapter.ViewHolder> {

    private ItemClickListener mClickListener;
    private final LayoutInflater mInflater;

    private static final String TAG = "PFRVAdapter";
    private final Activity activity;
    private final Context context;
    private final List<Tour> tours;

    private final ImageController imageController;

    public ProfileFavoritesRVAdapter(Context context, List<Tour> tours, Activity activity) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.tours = tours;
        this.activity = activity;
        this.imageController = ImageController.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Creating View Holder");
        View view = mInflater.inflate(R.layout.fragment_profile_list_favorites, parent, false);
        return new ViewHolder(view);
    }

    /**
     * binds the data to the view and textview in each row
     *
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (BuildConfig.DEBUG) Log.d(TAG, "starting set properties");

        //get the item for this row
        Tour fav = this.tours.get(position);

        //set data
        if (fav != null) {
            holder.title.setText(fav.getTitle());
            holder.description.setText(fav.getDescription());

            List<File> imagefiles = imageController.getImages(fav.getImagePaths());
            if (!imagefiles.isEmpty() && imagefiles.get(0).length() != 0) {
                //handler.setIndicatorsEnabled(true);
                String url = ServiceGenerator.API_BASE_URL + "/tour/" + fav.getTour_id() + "/img/" + fav.getImagePaths().get(0).getId();
                imageController.getPicassoHandler(activity)
                        .load(url)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.progress_animation)
                        .into(holder.tripImage);
            } else {
                Picasso.with(context)
                        .load(R.drawable.example_image)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.progress_animation)
                        .into(holder.tripImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.tours.size();
    }

    private Tour getItem(int id) {
        return this.tours.get(id);
    }

    public void setClickListener (ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, Tour tour);
    }

    /**
     * stores and recycles views as they are scrolled off screen
     * @author Alexander Weinbeck
     * @license MIT
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, description;
        private ImageView tripImage, favIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_fav_title);
            description = (TextView) itemView.findViewById(R.id.list_fav_description);
            tripImage = (ImageView) itemView.findViewById(R.id.list_fav_image_view);
            favIcon = (ImageView) itemView.findViewById(R.id.list_fav_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getItem(getAdapterPosition()));
        }
    }
}
    /**
     * Gets the custom representation of one favorite in the list view back
     *
     * @param position    index of favorite
     * @param convertView view of fragment
     * @param parent      view where list element is represented
     * @return view of one favorite
     */
    /*
    @Nonnull
    @Override
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        //get the item for this row
        Tour fav = getItem(position);
        TourController tourController = new TourController(fav);
        //inflate the row layout
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_profile_list_favorites, parent, false);

        //look up the view for elements
        title = (TextView) convertView.findViewById(R.id.list_fav_title);
        description = (TextView) convertView.findViewById(R.id.list_fav_description);
        tripImage = (ImageView) convertView.findViewById(R.id.list_fav_image_view);
        favIcon = (ImageView) convertView.findViewById(R.id.list_fav_icon);

        //set data
        if (fav != null) {
            title.setText(fav.getTitle());
            description.setText(fav.getDescription());

            List<ImageInfo> imageinfos = fav.getImagePaths();
            List<File> imagefiles = imageController.getImages(imageinfos);
            if (!imagefiles.isEmpty() && imagefiles.get(0).length() != 0) {
                Picasso.with(context)
                        .load(imagefiles.get(0)).placeholder(R.drawable.progress_animation)
                        .fit()
                        .centerCrop()
                        .into(tripImage);
            } else {
                tripImage.setImageResource(R.drawable.example_image);
            }

            favIcon.setOnClickListener(e -> tourController.unsetFavorite(controllerEvent -> {
                switch (controllerEvent.getType()) {
                    case OK:
                        View v = profileFragment.getView();
                        profileFragment.setupFavorites();
                        break;
                    default:
                        Toast.makeText(context, R.string.connection_fail, Toast.LENGTH_SHORT).show();
                        break;
                }
            }));

        }
        return convertView;
    }*/

