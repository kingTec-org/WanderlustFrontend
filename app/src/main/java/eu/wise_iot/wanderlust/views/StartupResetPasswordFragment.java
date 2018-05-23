package eu.wise_iot.wanderlust.views;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.wise_iot.wanderlust.BuildConfig;
import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.constants.Constants;
import eu.wise_iot.wanderlust.controllers.ControllerEvent;
import eu.wise_iot.wanderlust.controllers.EventType;
import eu.wise_iot.wanderlust.controllers.FragmentHandler;
import eu.wise_iot.wanderlust.controllers.LoginController;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.os.Process.setThreadPriority;


public class StartupResetPasswordFragment extends Fragment {

    private Context context;
    private static final String TAG = "ResetPWFragment";

    private Button btnForgotPassword;
    private EditText editTextForgotPassword;
    private TextInputLayout textInputForgotPassword;
    private TextView redirectToLogin;

    private final LoginController loginController;

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,5}$", Pattern.CASE_INSENSITIVE);


    public StartupResetPasswordFragment() {
        loginController = new LoginController();
    }

    public static StartupResetPasswordFragment newInstance() {
        Bundle args = new Bundle();
        StartupResetPasswordFragment fragment = new StartupResetPasswordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final FragmentHandler fragmentHandler = new FragmentHandler() {
        @Override
        public void onResponse(ControllerEvent event) {
            EventType eventType = event.getType();
            switch (eventType) {
                case OK:
                    Toast.makeText(context, R.string.forgot_password_reset_mail_success, Toast.LENGTH_LONG).show();
                    Fragment startupLoginFragment = getFragmentManager().findFragmentByTag(Constants.LOGIN_FRAGMENT);
                    if (startupLoginFragment == null)startupLoginFragment = StartupLoginFragment.newInstance();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, startupLoginFragment, Constants.LOGIN_FRAGMENT)
                            .commit();

                    break;
                default:
                    Toast.makeText(context, R.string.forgot_password_reset_mail_fail, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_startup_reset_password, container, false);
        btnForgotPassword = (Button) view.findViewById(R.id.btn_forgot_pw);
        editTextForgotPassword = (EditText) view.findViewById(R.id.input_forgot_pw);
        textInputForgotPassword = (TextInputLayout) view.findViewById(R.id.text_input_forgot_pw);
        redirectToLogin = (TextView) view.findViewById(R.id.link_login);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActionControls();

        //handle keyboard closing
        view.findViewById(R.id.rootLayout).setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            return true;
        });
    }

    private void initActionControls() {
        btnForgotPassword.setOnClickListener(v -> {
            String inputMail = editTextForgotPassword.getText().toString();
            if (!validateEmail(inputMail)) {
                textInputForgotPassword.setError(getString(R.string.registration_email_invalid));
            } else {
                new AsyncResetPassword(inputMail, getActivity()).execute();
                //loginController.resetPassword(inputMail, fragmentHandler);
            }
        });

        redirectToLogin.setOnClickListener(v -> {
            Fragment startupLoginFragment = getFragmentManager().findFragmentByTag(Constants.LOGIN_FRAGMENT);
            if (startupLoginFragment == null) startupLoginFragment = StartupLoginFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, startupLoginFragment, Constants.LOGIN_FRAGMENT)
                    .commit();
        });
    }


    private boolean validateEmail(String mail) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(mail);
        return matcher.find();
    }

    /**
     * handles async backend request for performing a password reset
     * this will keep the UI responsive
     *
     * @author Alexander Weinbeck
     */
    private class AsyncResetPassword extends AsyncTask<Void, Void, Void> {
        final ProgressDialog pdLoading;
        private final Activity activity;
        private String email;
        private ControllerEvent event;

        AsyncResetPassword(String email, Activity activity){
            this.activity = activity;
            this.email = email;
            pdLoading = new ProgressDialog(this.activity);
        }
        @Override
        protected void onPreExecute() {
            //this method will be running on UI thread
            pdLoading.setMessage("\t" + getResources().getString(R.string.msg_processing_open_tour));
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            setThreadPriority(-10);
            event = loginController.resetPasswordSequential(email);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            switch(event.getType()) {
                case OK:
                    Toast.makeText(context, R.string.forgot_password_reset_mail_success, Toast.LENGTH_LONG).show();
                    Fragment startupLoginFragment = getFragmentManager().findFragmentByTag(Constants.LOGIN_FRAGMENT);
                    if (startupLoginFragment == null)startupLoginFragment = StartupLoginFragment.newInstance();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, startupLoginFragment, Constants.LOGIN_FRAGMENT)
                            .commit();
                    break;
                case NOT_FOUND:
                    if (BuildConfig.DEBUG) Log.d(TAG,"ERROR: Server Response arrived -> Email was not found");
                    Toast.makeText(getActivity().getApplicationContext(),getResources().getText(R.string.msg_email_not_found), Toast.LENGTH_LONG).show();
                    break;
                case SERVER_ERROR:
                    if (BuildConfig.DEBUG) Log.d(TAG,"ERROR: Server Response arrived -> SERVER ERROR");
                    Toast.makeText(getActivity().getApplicationContext(),getResources().getText(R.string.msg_server_error), Toast.LENGTH_LONG).show();
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getActivity().getApplicationContext(),getResources().getText(R.string.msg_no_internet), Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) Log.d(TAG,"ERROR: Server Response arrived -> NETWORK ERROR");
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.d(TAG,"ERROR: Server Response arrived -> UNDEFINED ERROR");
                    Toast.makeText(getActivity().getApplicationContext(),getResources().getText(R.string.forgot_password_reset_mail_fail), Toast.LENGTH_LONG).show();
            }

            if (pdLoading.isShowing()) pdLoading.dismiss();
        }
    }
}
