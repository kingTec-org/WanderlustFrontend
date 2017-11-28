package eu.wise_iot.wanderlust;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import eu.wise_iot.wanderlust.models.DatabaseModel.Poi;
import eu.wise_iot.wanderlust.models.DatabaseModel.Poi_;
import eu.wise_iot.wanderlust.models.DatabaseModel.MyObjectBox;
import eu.wise_iot.wanderlust.models.DatabaseObject.PoiDao;
import eu.wise_iot.wanderlust.views.MainActivity;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;


/**
 * @author Rilind Gashi
 */

@RunWith(AndroidJUnit4.class)
public class PoiDaoTest {

    BoxStore boxStore;
    Box<Poi> poiBox;
    QueryBuilder<Poi> poiQueryBuilder;
    Poi testPoi;
    PoiDao poiDao;

    @Before
    public void setUpBefore(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        boxStore = MyObjectBox.builder().androidContext(appContext).build();
        poiDao = new PoiDao(boxStore, appContext);
        poiBox = boxStore.boxFor(Poi.class);
        poiQueryBuilder = poiBox.query();
        testPoi = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
    }

    @Test
    public void createTest(){
        poiDao.create(testPoi);
        assertEquals("Matterhorn", poiQueryBuilder.equal(Poi_.title, "Matterhorn")
                .build().findFirst().getTitle());

    }

    @Test
    public void updateTest(){
        poiBox.put(testPoi);
        testPoi.setTitle("Matterhorn (VS)");
        poiBox.put(testPoi);
        assertEquals("Matterhorn (VS)", poiQueryBuilder.
                equal(Poi_.poi_id, testPoi.getPoi_id()).build().findFirst().getTitle());
    }

    @Test
    public void findOneTest(){
        poiBox.put(testPoi);
        try {
            assertEquals("Matterhorn", poiDao.findOne("name", "Matterhorn").getTitle());
        }catch (NoSuchFieldException | IllegalAccessException e){

        }
    }

    @Test
    public void findTest(){
        Poi poiOne = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiTwo = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);

        poiBox.put(poiOne);
        poiBox.put(poiTwo);

        assertEquals(2, poiDao.find().size());
    }

    @Test
    public void findDetailedTest(){
        Poi poiOne = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiTwo = new Poi(0, "Zugspitze", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiThree = new Poi(0, "Mount Everest", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiFour = new Poi(0, "Mount Everest", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);

        poiBox.put(poiOne);
        poiBox.put(poiTwo);
        poiBox.put(poiThree);
        poiBox.put(poiFour);

        try {
            assertEquals(2, poiDao.find("name", "Mount Everest").size());
        }catch (NoSuchFieldException | IllegalAccessException e){

        }
    }

    @Test
    public void countTest(){
        Poi poiOne = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiTwo = new Poi(0, "Zugspitze", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiThree = new Poi(0, "Mount Everest", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiFour = new Poi(0, "Mount Everest", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);

        poiBox.put(poiOne);
        poiBox.put(poiTwo);
        poiBox.put(poiThree);
        poiBox.put(poiFour);

        assertEquals(4, poiDao.count());
    }

    @Test
    public void countTestTwo() throws NoSuchFieldException, IllegalAccessException {
        Poi poiOne = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiTwo = new Poi(0, "Zugspitze", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiThree = new Poi(0, "Mount Everest", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiFour = new Poi(0, "Mount Everest", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);


        poiBox.put(poiOne);
        poiBox.put(poiTwo);
        poiBox.put(poiThree);
        poiBox.put(poiFour);

        assertEquals(2, poiDao.count("name", "Mount everest"));
    }

    @Test
    public void deleteTest(){
        Poi poiOne = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiTwo = new Poi(0, "Zugspitze", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);

        poiBox.put(poiOne);
        poiBox.put(poiTwo);

        try {
            poiDao.delete("name", "Matterhorn");
        }catch (NoSuchFieldException | IllegalAccessException e){

        }

        assertEquals(1, poiDao.find().size());
    }

    @Test
    public void deleteAllTest(){
        Poi poiOne = new Poi(0, "Matterhorn", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);
        Poi poiTwo = new Poi(0, "Zugspitze", "Berg", "picturePath", 53.53f, 53.53f, 1, 1, false);

        poiBox.put(poiOne);
        poiBox.put(poiTwo);

        poiDao.deleteAll();
        assertEquals(0, poiDao.count());
    }

    @After
    public void after() {
        poiBox.removeAll();
        boxStore.close();
    }
}