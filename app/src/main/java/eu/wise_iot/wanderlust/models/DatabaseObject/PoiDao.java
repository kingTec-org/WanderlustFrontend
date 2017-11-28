package eu.wise_iot.wanderlust.models.DatabaseObject;


import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;

import eu.wise_iot.wanderlust.models.DatabaseModel.Poi;
import eu.wise_iot.wanderlust.models.DatabaseModel.Poi_;
import eu.wise_iot.wanderlust.services.PoiService;
import eu.wise_iot.wanderlust.services.ServiceGenerator;
import eu.wise_iot.wanderlust.views.MainActivity;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.Property;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PoiDao
 *
 * @author Rilind Gashi
 * @license MIT
 */

public class PoiDao extends DatabaseObjectAbstract {
    private Box<Poi> poiBox;
    private Query<Poi> poiQuery;
    private QueryBuilder<Poi> poiQueryBuilder;
    private Context context;
    Property columnProperty;

    /**
     * Constructor.
     *
     * @param boxStore (required) delivers the connection to the frontend database
     */

    public PoiDao(BoxStore boxStore, Context context) {
        poiBox = boxStore.boxFor(Poi.class);
        poiQueryBuilder = poiBox.query();
        this.context = context;
    }

    public long count() {
        return poiBox.count();
    }

    public long count(String searchedColumn, String searchPattern) throws NoSuchFieldException, IllegalAccessException {
        Field searchedField = Poi_.class.getDeclaredField(searchedColumn);
        searchedField.setAccessible(true);

        columnProperty = (Property) searchedField.get(Poi_.class);
        poiQueryBuilder.equal(columnProperty, searchPattern);
        poiQuery = poiQueryBuilder.build();
        return poiQuery.find().size();
    }

    /**
     * Update an existing user in the database.
     *
     * @param poi (required).
     */
    public Poi update(final Poi poi) {

        PoiService service = ServiceGenerator.createService(PoiService.class);

        Call<Poi> call = service.postPoi(poi);
        call.enqueue(new Callback<Poi>() {
             @Override
             public void onResponse(Call<Poi> call, Response<Poi> response) {
                 if(response.isSuccessful()) {
                     poiBox.put(poi);
                 } else{

                 }
             }

             @Override
             public void onFailure(Call<Poi> call, Throwable t) {

             }
        });

        return poi;
    }

    /**
     * Insert an user into the database.
     *
     * @param poi (required).
     */
    public void create(final Poi poi) {


        PoiService service = ServiceGenerator.createService(PoiService.class);
        Call<Poi> call = service.postPoi(poi);
        call.enqueue(new Callback<Poi>() {
            @Override
            public void onResponse(Call<Poi> call, Response<Poi> response) {
                if(response.isSuccessful()) {
                    poiBox.put(poi);
                    Toast.makeText(context, "Poi saved!", Toast.LENGTH_LONG).show();

                    MainActivity main = (MainActivity) context;
                    main.makeToast("hello");
                } else{
                    Toast.makeText(context, "Failed to save Poi", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Poi> call, Throwable t) {
                    Toast.makeText(context, "Network error", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Return a list with all poi
     *
     * @return List<Poi>
     */
    public List<Poi> find() {
        return poiBox.getAll();
    }

    /**
     * Searching for a single user with a search pattern in a column.
     *
     * @param searchedColumn (required) the column in which the searchPattern should be looked for.
     * @param searchPattern  (required) contain the search pattern.
     * @return User who match to the search pattern in the searched columns
     */
    public Poi findOne(String searchedColumn, String searchPattern) throws NoSuchFieldException, IllegalAccessException {
        Field searchedField = Poi_.class.getDeclaredField(searchedColumn);
        searchedField.setAccessible(true);

        columnProperty = (Property) searchedField.get(Poi_.class);
        poiQueryBuilder.equal(columnProperty, searchPattern);
        poiQuery = poiQueryBuilder.build();
        return poiQuery.findFirst();
    }

    /**
     * Searching for user matching with the search pattern in a the selected column.
     *
     * @param searchedColumn (required) the column in which the searchPattern should be looked for.
     * @param searchPattern  (required) contain the search pattern.
     * @return List<Poi> which contains the users, who match to the search pattern in the searched columns
     */
    public List<Poi> find(String searchedColumn, String searchPattern) throws NoSuchFieldException, IllegalAccessException {
        Field searchedField = Poi_.class.getDeclaredField(searchedColumn);
        searchedField.setAccessible(true);

        columnProperty = (Property) searchedField.get(Poi_.class);
        poiQueryBuilder.equal(columnProperty, searchPattern);
        poiQuery = poiQueryBuilder.build();
        return poiQuery.find();
    }

    public Poi delete(String searchedColumn, String searchPattern) throws NoSuchFieldException, IllegalAccessException {
        poiBox.remove(findOne(searchedColumn, searchPattern));

        return null;
    }

    public void deleteAll() {
        poiBox.removeAll();
    }

}