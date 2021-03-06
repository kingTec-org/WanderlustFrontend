package eu.wise_iot.wanderlust.views.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.controllers.PoiController;
import eu.wise_iot.wanderlust.models.DatabaseModel.Poi;
import eu.wise_iot.wanderlust.models.DatabaseModel.ViolationType;
import eu.wise_iot.wanderlust.models.DatabaseObject.ViolationTypeDao;

/**
 * TourReportDialog:
 *
 * @author Rilind Gashi
 * @license MIT
 */

public class PoiReportDialog extends DialogFragment {

    private PoiController poiController;
    private Poi poi;

    private static final String TAG = "TourRatingDialog";
    private ImageButton buttonSave, buttonCancel;
    private RadioButton rbHarassment, rbViolence, rbHatespeech, rbSpam, rbOther, rbNudity;

    private final ViolationTypeDao violationTypeDao = ViolationTypeDao.getInstance();


    public PoiReportDialog newInstance(Poi poi, PoiController poiController) {
        PoiReportDialog dialog = new PoiReportDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.poi = poi;
        dialog.poiController = poiController;
        //this.violationTypeDao = ViolationTypeDao.getDialog();
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_report_poi, container, false);

        buttonCancel = view.findViewById(R.id.violationPoiCancelButton);
        buttonSave = view.findViewById(R.id.violationPoiSaveButton);
        rbHarassment = view.findViewById(R.id.violationPoiHarassment);
        rbHatespeech = view.findViewById(R.id.violationPoiHateSpeech);
        rbNudity = view.findViewById(R.id.violationPoiNudity);
        rbOther = view.findViewById(R.id.violationPoiOther);
        rbSpam = view.findViewById(R.id.violationPoiSpam);
        rbViolence = view.findViewById(R.id.violationPoiViolence);

        buttonCancel.setOnClickListener(v -> getDialog().dismiss());
        buttonSave.setOnClickListener(v -> reportTour());

        return view;
    }

    private void reportTour(){
        String violationName = null;
        if(rbHarassment.isChecked()) violationName = rbHarassment.getTag().toString();
        else if(rbHatespeech.isChecked()) violationName = rbHatespeech.getTag().toString();
        else if(rbNudity.isChecked()) violationName = rbNudity.getTag().toString();
        else if(rbOther.isChecked()) violationName = rbOther.getTag().toString();
        else if(rbSpam.isChecked()) violationName = rbSpam.getTag().toString();
        else if(rbViolence.isChecked()) violationName = rbViolence.getTag().toString();
        else if(violationName == null) violationName = "other";

        ViolationType violationType = violationTypeDao.getViolationTypebyName(violationName);

        poiController.reportViolation(poiController.new Violation(poi.getPoi_id(), violationType.getViolationt_id()), controllerEvent -> {
            switch (controllerEvent.getType()){
                case OK:
                    getDialog().dismiss();
                    Toast.makeText(getActivity().getApplicationContext(),getResources().getText(R.string.report_submit), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    getDialog().dismiss();
                    Toast.makeText(getActivity().getApplicationContext(),getResources().getText(R.string.msg_no_internet) + " " + controllerEvent.getType().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

