package com.Bieling.CrapChat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.Bieling.CrapChat.api.Snaphax;
import com.google.analytics.tracking.android.EasyTracker;

import java.io.File;

public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;

	// UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private Snaphax hax;

    public enum LoginResult {
        SUCCES,
        NETWORKERROR,
        NOUSERNAME,
        NOPASSWORD,
        INVALIDUSERNAME,
        INVALIDPASSWORD,
        INVALIDUSERORPASS,
        FAIL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        new Globals(getApplicationContext());
        File f = (new File(Globals.SnapsDir));
        if(!f.exists())
            f.mkdir();
        f.setWritable(true, false);
        f.setReadable(true, false);


        mUserView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        // Set defaults
 		SharedPreferences settings = getSharedPreferences(Globals.Prefs_name, 0);
 		mUserView.setText(settings.getString("Username", ""));
 		mPasswordView.setText(settings.getString("Password",""));

        mUsername = mUserView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        // Create listeners
        findViewById(R.id.sign_in_button).setOnClickListener(   //Login button
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });

        // Attempt login if Username/Password is already known
        if (mUsername.length() != 0 && mPassword.length() != 0)
            attemptLogin();

    }

    private void convertOld() {
        Context context = getApplicationContext(); // or activity.getApplicationContext()
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String versionName = ""; // initialize String

        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {}

        if(context.getSharedPreferences(Globals.Prefs_name, 0).getString("", "asdasd") != versionName) {
            //Stuff
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        mUsername = mUserView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username.
        if (mUsername.length() == 0) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        // Check for a valid password.
        else if (mPassword.length() == 0) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);

        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Snaphax.LoginResponse> {
        @Override
        protected Snaphax.LoginResponse doInBackground(Void... params) {
            // Hide keyboard
            final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findViewById(R.id.login_form).getWindowToken(), 0);

            hax = new Snaphax(mUsername, mPassword);
            return hax.Login();
        }


        protected void onPostExecute(final Snaphax.LoginResponse response) {
            mAuthTask = null;
            showProgress(false);

            if(response == Snaphax.LoginResponse.OK) {
                finish();
            } else if(response == Snaphax.LoginResponse.INVALIDCREDS) {
                mUserView.setError(getString(R.string.error_invalid_user_or_pass));
                mUserView.requestFocus();
            } else if(response == Snaphax.LoginResponse.NETWORKERROR) {

                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        protected void finish() {
            SharedPreferences settings = getSharedPreferences(Globals.Prefs_name, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Username", mUserView.getText().toString());
            editor.putString("Password", mPasswordView.getText().toString());

            // Commit the edits!
            editor.commit();
            Intent intent = new Intent(LoginActivity.this, ListingActivity.class);
            startActivity(intent);
        }

    }
    
}
