package eu.wise_iot.wanderlust.controllers;

import eu.wise_iot.wanderlust.models.DatabaseModel.UserTour;
import eu.wise_iot.wanderlust.models.DatabaseObject.UserTourDao;

/**
 * ToursController:
 * handles the toursfragment and its in and output
 *
 * @author Alexander Weinbeck
 * @license MIT
 */
public class TourController {
    public static void getDataViewServer(int tourID, FragmentHandler handler) {
        UserTourDao userTourDao = new UserTourDao();
        userTourDao.retrieve(tourID, handler);
    }

    /**
     * get all tours out of db
     *
     * @return List of tours
     */
    public UserTour getDataView(int tourID) {

        UserTourDao userTourDao = new UserTourDao();
        return userTourDao.find().get(tourID);
    }

}
