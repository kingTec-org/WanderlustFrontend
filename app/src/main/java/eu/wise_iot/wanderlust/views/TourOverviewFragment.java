package eu.wise_iot.wanderlust.views;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.constants.Constants;
import eu.wise_iot.wanderlust.controllers.ControllerEvent;
import eu.wise_iot.wanderlust.controllers.FragmentHandler;
import eu.wise_iot.wanderlust.controllers.TourOverviewController;
import eu.wise_iot.wanderlust.models.DatabaseModel.User;
import eu.wise_iot.wanderlust.models.DatabaseModel.UserTour;
import eu.wise_iot.wanderlust.models.DatabaseObject.UserTourDao;
import eu.wise_iot.wanderlust.models.Old.Tour;
import eu.wise_iot.wanderlust.views.adapters.MyRecyclerViewAdapter;
import okhttp3.ResponseBody;


/**
 * TourOverviewFragment:
 *
 * @author Fabian Schwander
 * @license MIT
 */
public class TourOverviewFragment extends Fragment {
    private static final String TAG = "TourOverviewFragment";
    private Tour tour;
    private Context context;
    List<UserTour> userTours = new ArrayList<>();
    private static List<ResponseBody> userTourImages = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();

        super.onCreate(savedInstanceState);
        //fetch data from database here
    }
    /**
     * Static instance constructor.
     *
     * @return Fragment: TourOverviewFragment
     */
    public static TourOverviewFragment newInstance() {

        Bundle args = new Bundle();
        TourOverviewFragment fragment = new TourOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * retrieve all images from the database
     * @param userTours
     * @return
     */
    public static void getImages(List<UserTour> userTours){
        TourOverviewController toc = new TourOverviewController();
        for(UserTour ut : userTours){
            toc.downloadThumbnail(ut.getTour_id(), 1, new FragmentHandler() {
                @Override
                public void onResponse(ControllerEvent controllerEvent) {
                    switch (controllerEvent.getType()) {
                        case OK:
                            Log.d("Tours","Server response thumbnail downloading OK: " + controllerEvent.getType().name());

                        default:
                            Log.d("Tours","Server response ERROR: " + controllerEvent.getType().name());
                    }

                }
            });
        }
    }
    public static void preserveImages(ResponseBody rb){
        userTourImages.add(rb);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TourOverviewController toc = new TourOverviewController();

        View view = inflater.inflate(R.layout.fragment_toursoverview, container, false);

        UserTour testTour = new UserTour(0, 0, "Tour1", "TourDescription", "picturePath", "polyline", 1, 1, true);

        //UserTourDao userTours = new UserTourDao();

        userTours.add(testTour);
        userTours.add(testTour);


        Log.d("Tours","Opened");

        //fetch from db actual tours to feed recyclerview
        toc.getAllTours(new FragmentHandler() {
            @Override
            public void onResponse(ControllerEvent event) {
                switch (event.getType()) {
                    case OK:
                        //get all needed information from server db
                        List<UserTour> listTours = (List<UserTour>) event.getModel();
                        Log.d("Tours","Server response arrived");

                        //get all the images needed and save them on the device
                        getImages(listTours);

                        Log.d("Tours","Images preserved: " + userTourImages);

                        // set up the RecyclerView 1
                        RecyclerView rvTouren = (RecyclerView) view.findViewById(R.id.rvTouren);
                        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        rvTouren.setLayoutManager(horizontalLayoutManager);
                        MyRecyclerViewAdapter adapterRoutes = new MyRecyclerViewAdapter(context, listTours);
                        adapterRoutes.setClickListener(this::onItemClickImages);
                        rvTouren.setAdapter(adapterRoutes);

                        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
                        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
                        rvTouren.addItemDecoration(itemDecorator);


                        // set up the RecyclerView 2
                        //TODO: to be implemented (Favorites)
//                        RecyclerView rvFavorites = (RecyclerView) view.findViewById(R.id.rvFavorites);
//                        LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                        rvFavorites.setLayoutManager(horizontalLayoutManager2);
//                        MyRecyclerViewAdapter adapterTitles = new MyRecyclerViewAdapter(context, listTours);
//                        adapterTitles.setClickListener(this::onItemClickFavorites);
//                        rvFavorites.setAdapter(adapterTitles);


                        //tvDescend.setText(ut.getTitle());
                        break;
                    default:
                        Log.d("Tours","Server response ERROR: " + event.getType().name());
                        //do nothing
                }
            }

            public void onItemClickImages(View view, int routeID, UserTour tour) {
                Log.d("Tours","Tour Clicked and event triggered ");
                //TODO go to selected tour linkage on merge
                //TourFragment fragment = TourFragment.newInstance(tour);
//                String fragmentTag = Constants.TOUROVERVIEW_FRAGMENT;
//
//                FragmentManager fragmentManager = getFragmentManager();
//                fragmentManager.beginTransaction().addToBackStack(fragment.toString()).replace(R.id.content_frame, fragment).commit();
            }
            //TODO: favorites
//            public void onItemClickFavorites(View view, int favoriteID) {
//                Bundle bundle = new Bundle();
//                //pass id to fragment
//                bundle.putString(Constants.CLICKED_TOUR, Integer.toString(favoriteID));
//
//                Fragment fragment = new TourFragment();
//                fragment.setArguments(bundle);
//
//                FragmentManager fragmentManager = getFragmentManager();
//                fragmentManager.beginTransaction().addToBackStack(fragment.toString()).replace(R.id.content_frame, fragment).commit();
//            }
        });


        // data to populate the RecyclerView with
        //testdata
//        ArrayList<String> viewImages = new ArrayList<>();
//        viewImages.add(testTour.getImagePath());
//        viewImages.add("test1");
//        viewImages.add("test2");
//        viewImages.add("test3");
//        viewImages.add("test4");
//
//        ArrayList<String> viewText = new ArrayList<>();
//        viewText.add(testTour.getTitle());
//        viewText.add("Cow");
//        viewText.add("Camel");
//        viewText.add("Sheep");
//        viewText.add("Goat");





        return view;
        //return inflater.inflate(R.layout.fragment_tour, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        TextView nameView = (TextView) view.findViewById(R.id.tour_title);
//        nameView.setText(tour.getName());
//
//        TextView difficultyView = (TextView) view.findViewById(R.id.difficulty_view);
//        difficultyView.setText(tour.getDifficultyWithExplainingText());
//
//        ImageView teaserImageView = (ImageView) view.findViewById(R.id.teaser_image);
//        int imageId = context.getResources().getIdentifier(tour.getTeaserImageWithoutSuffix(), "drawable", context.getPackageName());
//        Picasso.with(context).load(imageId).fit().into(teaserImageView);
//
//        TextView durationView = (TextView) view.findViewById(R.id.duration_view);
//        durationView.setText(tour.getDuration());
//
//        TextView distanceUpView = (TextView) view.findViewById(R.id.distanzeUp_view);
//        distanceUpView.setText(tour.getDistanceUpInMeters());
//
//        TextView distanceDownView = (TextView) view.findViewById(R.id.distanzeDown_view);
//        distanceDownView.setText(tour.getDistanceDownInMeters());
//
//        TextView trackSegmentView = (TextView) view.findViewById(R.id.track_segment_view);
//        trackSegmentView.setText(tour.getTrackSegment());
//
//        TextView descriptionView = (TextView) view.findViewById(R.id.description_view);
//        descriptionView.setText(tour.getDescription());
//
//        TextView linkSourceView = (TextView) view.findViewById(R.id.link_source_view);
//        linkSourceView.setText(tour.getLinkSource());
//        linkSourceView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = tour.getLinkSource();
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse(url));
//                startActivity(intent);
//            }
//        });
//
//        // todo: add on click listener here and open MapFragment on specified start location
//        Button goToStartLocationButton = (Button) view.findViewById(R.id.go_to_start_location_button);
//        goToStartLocationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, R.string.msg_no_action_defined_on_go_to_map_button, Toast.LENGTH_LONG).show();
//                getActivity().getFragmentManager().beginTransaction()
//                        .add(R.id.content_frame, MapFragment.newInstance())
//                        .commit();
//            }
//        });

    }

}
