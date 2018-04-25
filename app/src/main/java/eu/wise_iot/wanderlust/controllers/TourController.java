package eu.wise_iot.wanderlust.controllers;

import android.app.FragmentManager;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.models.DatabaseModel.DifficultyType;
import eu.wise_iot.wanderlust.models.DatabaseModel.DifficultyType_;
import eu.wise_iot.wanderlust.models.DatabaseModel.Favorite;
import eu.wise_iot.wanderlust.models.DatabaseModel.Favorite_;
import eu.wise_iot.wanderlust.models.DatabaseModel.Rating;
import eu.wise_iot.wanderlust.models.DatabaseModel.Rating_;
import eu.wise_iot.wanderlust.models.DatabaseModel.Tour;
import eu.wise_iot.wanderlust.models.DatabaseObject.CommunityTourDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.DifficultyTypeDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.FavoriteDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.RatingDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.UserDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.UserTourDao;
import eu.wise_iot.wanderlust.views.WanderlustMapView;
import io.objectbox.Property;


/**
 * TourController:
 * handles the tourfragment and its in and output
 *
 * @author Alexander Weinbeck, Rilind Gashi, Simon Kaspar
 */
public class TourController {

    public static String convertToStringDistance(long distance) {
        if (distance >= 1000) {
            return Math.round((float)distance / 10.0) / 100.0 + "km ";
        }
        else return distance + "m";
    }

    public static String convertToStringDuration(long time) {
        int hours = (int) Math.floor(time / 60);
        int minutes = (int) time % 60;

        String text = "";
        if (hours != 0) text += hours + "h ";
        text += minutes + "min";
        return text;
    }

    private final FavoriteDao favoriteDao;
    private RatingDao ratingDao;
    private UserDao userDao;
    private Tour tour;
    private UserTourDao userTourDao;
    private CommunityTourDao communityTourDao;
    private DifficultyTypeDao difficultyTypeDao;
    private ImageController imageController;

    private static final String TAG = "Tourcontroller";

    public TourController(Tour tour){
        this.tour = tour;
        userDao = UserDao.getInstance();
        userTourDao = UserTourDao.getInstance();
        communityTourDao = CommunityTourDao.getInstance();
        favoriteDao = FavoriteDao.getInstance();
        difficultyTypeDao = DifficultyTypeDao.getInstance();
        ratingDao = RatingDao.getInstance();
        imageController = ImageController.getInstance();

    }


    /**
     * True if Favorite is set, otherwise false
     */
    public boolean isFavorite(){
        Favorite fav = favoriteDao.findOne(Favorite_.tour, tour.getTour_id());
        return fav != null;
    }

    /**
     * set favorite
     * @param handler Fragment handler
     */
    public void setFavorite(FragmentHandler handler){
        favoriteDao.create(tour, handler);
    }

    /**
     * unset favorite
     * @param handler Fragment handler
     */
    public boolean unsetFavorite(FragmentHandler handler){
        Favorite fav = favoriteDao.findOne(Favorite_.tour, tour.getTour_id());
        if (fav != null){
            favoriteDao.delete(fav.getFav_id(), handler);
            return true;
        }
        return false;
    }

    public boolean isSaved(){
        for(Tour t : communityTourDao.find()){
            if(t.getTour_id() == tour.getTour_id()){
                return true;
            }
        }
        return false;
    }

    public void setSaved(FragmentHandler handler,Context context ,FragmentManager fragmentManager){
        userTourDao.retrieve(tour.getTour_id(), new FragmentHandler() {
            @Override
            public void onResponse(ControllerEvent controllerEvent) {
                switch (controllerEvent.getType()){
                    case OK:
                        Tour data = (Tour) controllerEvent.getModel();
                        tour.setPolyline(data.getPolyline());
                        communityTourDao.create(tour);

                        ArrayList<GeoPoint> points = PolyLineEncoder.decode(getPolyline(), 10);
                        Road road = new Road(points);
                        Polyline polyline = RoadManager.buildRoadOverlay(road);
                        polyline.setColor(R.color.highlight_main_transparent75);

                        WanderlustMapView mapView = new WanderlustMapView(context);
                        mapView.getOverlayManager().add(polyline);

                        MapTileProviderBase providerBase = mapView.getTileProvider();

                        Log.d("OOFFLIINE2", providerBase.getTileSource().toString());
                        ITileSource tileSource = new XYTileSource("OpenTopoMap", 0, 20, 256, ".png",
                                new String[]{"https://opentopomap.org/"});
                        mapView.setTileSource(tileSource);

                        double north = -9000;
                        double south = 9000;
                        double east = -9000;
                        double west = 9000;

                        for(GeoPoint geoPoint : points){
                            if(geoPoint.getLatitude() > north)
                                north = geoPoint.getLatitude();
                            if(geoPoint.getLatitude() < south)
                                south = geoPoint.getLatitude();
                            if(geoPoint.getLongitude() > east)
                                east = geoPoint.getLongitude();
                            if(geoPoint.getLongitude() < west)
                                west = geoPoint.getLongitude();
                        }

                        BoundingBox boundingBox = new BoundingBox(north, east, south, west);

                        CacheManager manager = new CacheManager(mapView);
                        if(manager != null){
                            Log.d("OOFFLIINE", "exists");
                        }else{
                            Log.d("OOFFFLIIINE", "doesnt exists");
                        }

                        manager.downloadAreaAsync(context, boundingBox, 10, 20, new CacheManager.CacheManagerCallback() {
                            @Override
                            public void onTaskComplete() {
                                Toast.makeText(context, "Download done.", Toast.LENGTH_SHORT).show();
                                Log.d("OFFFFLINE", "done");
                            }

                            @Override
                            public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {

                            }

                            @Override
                            public void downloadStarted() {
                                Log.d("OFFFLINE", "started..");
                            }

                            @Override
                            public void setPossibleTilesInArea(int total) {

                            }

                            @Override
                            public void onTaskFailed(int errors) {
                                Toast.makeText(context, String.valueOf(errors), Toast.LENGTH_SHORT).show();
                                Log.d("OOFFFLIINEE", "failed");
                            }
                        });

                        Log.d(TAG, "Is saved");
                        handler.onResponse(new ControllerEvent(EventType.OK));
                        break;
                    default:
                        handler.onResponse(new ControllerEvent(EventType.CONFLICT));
                }
            }
        });
    }

    public boolean setSaved(){
        communityTourDao.create(tour);
        Log.d(TAG, "Is saved");
        return true;
    }

    public boolean unsetSaved(){
        communityTourDao.delete(tour);
        Log.d(TAG, "Is deleted");
        return true;
    }

    public boolean setRating(Tour tour, int starRating, FragmentHandler handler){
        if(starRating > 0){
            Rating tourRating = new Rating(0, 0, starRating, tour.getTour_id(),
                    userDao.getUser().getUser_id());
            ratingDao.create(tourRating, handler);
            return true;
        }
        return false;
    }

    public long alreadyRated(long tour_id){
        Property property = Rating_.tour;
        Rating rating = null;
        rating = ratingDao.findOne(property, tour_id, userDao.getUser().getUser_id());
        if(rating != null)
            return rating.getRate();
        else
            return 0;

    }

    //Todo: Tour should be a parameter
    public void getRating(Tour tour, FragmentHandler handler){
        ratingDao.retrieve(tour.getTour_id(), handler);
    }
    public void getRating(FragmentHandler handler){
        ratingDao.retrieve(tour.getTour_id(), handler);
    }
    public Number[] getElevationProfileXAxis(){
        ArrayList<GeoPoint> polyList  = PolyLineEncoder.decode(tour.getPolyline(),10);
        Number[] xAxis = new Number[polyList.size()];
        Iterator<GeoPoint> iter = polyList.iterator();
        GeoPoint first = iter.next();
        double totalDistance = 0.0D;
        xAxis[0] = totalDistance;
        int ct = 1;
        while (iter.hasNext()) {
            GeoPoint next = iter.next();
            double distance = first.distanceTo(next);
            xAxis[ct++] = Math.round(100.0 * ((totalDistance + distance) / 1000.0)) / 100.0;
            totalDistance += distance;
            first = next;
        }
        xAxis[ct - 1] = Math.round(100.0 * (tour.getDistance() / 1000.0)) / 100.0;
        return xAxis;
    }
    public Number[] getElevationProfileYAxis(){
        float[] elevations = elevationDecode(tour.getElevation());
        Number[] elevationObj = new Number[elevations.length];
        for (int i = 0; i < elevations.length; i++){
            elevationObj[i] = Math.round(elevations[i]);
        }
        return elevationObj;
    }

    public void loadGeoData(FragmentHandler handler){
        userTourDao.retrieve(tour.getTour_id(), controllerEvent -> {
            if (controllerEvent.getType() ==  EventType.OK){
                Tour TourWithGeoData = (Tour) controllerEvent.getModel();
                tour.setPolyline(TourWithGeoData.getPolyline());
                tour.setElevation(TourWithGeoData.getElevation());
                handler.onResponse(new ControllerEvent(EventType.OK, tour));
            }else{
                handler.onResponse(new ControllerEvent(controllerEvent.getType(), tour));
            }
        });
    }

    /**
     * Calculate duration string from absolut minute value
     * @return string with format HH h MM min
     */
    public String getDurationString(){
        if (tour != null){
            return convertToStringDuration(tour.getDuration());
        }else{
            return convertToStringDuration(0);
        }
    }

    /**
     * Calculate the duration to a specific point on a tour which is divided by 5
     * @param point n/5th point on a tour
     * @return string with format HH h MM min
     */
    public String getDurationStringSpecificPoint(long point){
        if(tour != null){
            return convertToStringDuration((tour.getDuration() * point) / 5);
        }else{
            return convertToStringDuration(0);
        }
    }

    /**
     * Calculate distance string from absolut meter value
     * @return string with format 0.9 km
     */
    public String getDistanceString(){
        if (tour != null){
            return convertToStringDistance(tour.getDistance());
        }else{
            return convertToStringDistance(0);
        }
    }
    /**
     * Get distance in meter
     * @return long value in meter
     */
    public long getDistance(){
        if (tour != null){
            return tour.getDistance();
        }else{
            return 0;
        }
    }
    /**
     * Difficulty mark
     * @return mark
     */
    public String getDifficultyMark(){
        DifficultyType difficultyType =  difficultyTypeDao.findOne(DifficultyType_.difft_id, tour.getDifficulty());
        if (difficultyType == null){
            return "T1";
        }else{
            return difficultyType.getMark();
        }
    }
    /**
     * Difficulty level
     * @return level
     */
    public long getLevel(){
        DifficultyType difficultyType =  difficultyTypeDao.findOne(DifficultyType_.difft_id, tour.getDifficulty());
        if (difficultyType == null){
            return 1L;
        }else{
            return difficultyType.getLevel();
        }
    }
    private float[] elevationDecode(String elevation){
        byte[] decodedByteArray;
        // Base64 decode of string
        try {
            decodedByteArray = Base64.decode(elevation.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            return new float[0];

        }
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(decodedByteArray))) {
            BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                line = line.replaceAll("\"", "");
                sb.append(line);
            }
            Gson gson = new Gson();
            Type type = new TypeToken<float[]>() {}.getType();
            br.close();
            return gson.fromJson(sb.toString(), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new float[0];
    }
    public String getCreatedAtString(){
        DateTimeFormatter encodef = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTime dt = encodef.parseDateTime(tour.getCreatedAt());
        DateTimeFormatter decodef = DateTimeFormat.forPattern("dd. MMMMM yyyy");
        return dt.toString(decodef);
    }
    public long getAscent(){ return tour.getAscent(); }
    public long getDescent() { return tour.getDescent(); }
    public String getDescription(){ return tour.getDescription(); }
    public String getTitle(){ return tour.getTitle(); }
    public String getPolyline(){ return tour.getPolyline(); }
    public List<File> getImages(){
        return imageController.getImages(tour.getImagePaths());
    }
    public Tour getCurrentTour(){ return tour; }
    }

