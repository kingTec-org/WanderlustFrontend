package eu.wise_iot.wanderlust.services;

import java.util.List;

import eu.wise_iot.wanderlust.models.DatabaseModel.ImageInfo;
import eu.wise_iot.wanderlust.models.DatabaseModel.Poi;
import eu.wise_iot.wanderlust.models.DatabaseModel.PoiType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * FavoriteService:
 * FavoriteController
 * show	            GET	    /poi/:id                | restricted
 * show	            GET	    /poi                    | restricted
 * show             GET     /poitype                | restricted
 * create	        POST	/poi                    | restricted
 * update	        PUT	    /poi/:id                | restricted
 * disable	        DELETE	/poi/:id                | restricted
 * uploadImage	    POST	/poi/:id/img            | restricted
 * deleteImage	    DELETE	/poi/:id/img/:image_id  | restricted
 * downloadImage	GET	    /poi/:id/img/:image_id  | restricted
 *
 * @author Alexander Weinbeck
 */
public interface PoiService {
    @GET("poi/{id}")
    Call<Poi> retrievePoi(@Path("id") long id);

    @GET("poi")
    Call<List<Poi>> retrieveAllPois();

    @GET("poi")
    Call<List<Poi>> retrievePoisByArea(@Query("lat1") double lat1, @Query("long1") double lon1,
                                       @Query("lat2") double lat2, @Query("long2") double lon2);

    @POST("poi")
    Call<Poi> createPoi(@Body Poi poi);

    @PUT("poi/{id}")
    Call<Poi> updatePoi(@Path("id") long id, @Body Poi poi);

    @DELETE("poi/{id}")
    Call<Poi> deletePoi(@Path("id") long id);

    @Multipart
    @POST("poi/{id}/img")
    Call<ImageInfo> uploadImage(@Path("id") long id, @Part MultipartBody.Part image);

    @DELETE("poi/{id}/img/{image_id}")
    Call<ImageInfo> deleteImage(@Path("id") long id, @Path("image_id") long image_id);

    @GET("poi/{id}/img/{image_id}")
    Call<ResponseBody> downloadImage(@Path("id") long id, @Path("image_id") long image_id);

    @GET("poitype")
    Call<List<PoiType>> retrieveAllPoiTypes();
}