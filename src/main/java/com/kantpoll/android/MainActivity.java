/*
 * Kantpoll Project
 * https://github.com/kantpoll
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kantpoll.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int FRAMEWORK_REQUEST_CODE = 1;
    private final Map<Integer, OnCompleteListener> permissionsListeners = new HashMap<>();
    private int nextPermissionsRequestCode = 4000;

    /**
     * It gets the link and checks whether the user has already been set, if so, it starts the HomeActivity
     *
     * @param savedInstanceState {Bundle}
     */
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("com.kantpoll.android", Context.MODE_PRIVATE);
        String user = sharedPref.getString("user", null);

        if (user != null) {
            Intent haIntent = new Intent(this, HomeActivity.class);
            startActivity(haIntent);
        }
    }

    /**
     * It checks whether the user has already been set, then displays the buttons accordingly.
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = getSharedPreferences("com.kantpoll.android", Context.MODE_PRIVATE);
        String user = sharedPref.getString("user", null);

        Button homeButton = findViewById(R.id.home_button);
        Button importButton = findViewById(R.id.import_button);
        Button emailButton = findViewById(R.id.email_button);
        Button phoneButton = findViewById(R.id.phone_button);

        if (user != null) {
            homeButton.setEnabled(true);
            importButton.setEnabled(false);
            emailButton.setEnabled(false);
            phoneButton.setEnabled(false);
        } else {
            homeButton.setEnabled(false);
            importButton.setEnabled(true);
            emailButton.setEnabled(true);
            phoneButton.setEnabled(true);
        }
    }

    /**
     * Registering vault with email
     *
     * @param view {View}
     */
    public void onLoginEmail(final View view) {
        onLogin(LoginType.EMAIL);
    }

    /**
     * Registering vault with phone
     *
     * @param view {View}
     */
    public void onLoginPhone(final View view) {
        onLogin(LoginType.PHONE);
    }

    /**
     * Start the HomeActivity
     *
     * @param view {View}
     */
    public void onHomeButton(final View view) {
        startActivity(new Intent(this, HomeActivity.class));
    }

    /**
     * Start the ImportActivity
     *
     * @param view {View}
     */
    public void onImportButton(final View view) {
        startActivity(new Intent(this, ImportActivity.class));
    }

    /**
     * Try to get the user and open the Home Activity
     *
     * @param requestCode {int}
     * @param resultCode  {int}
     * @param data        {Intent}
     */
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FRAMEWORK_REQUEST_CODE) {
            return;
        }

        final AccountKitLoginResult loginResult = AccountKit.loginResultWithIntent(data);
        if (loginResult == null || loginResult.wasCancelled()) {
            Toast.makeText(getApplication(), R.string.login_cancelled,
                    Toast.LENGTH_LONG).show();
        } else if (loginResult.getError() != null) {
            showMessage(loginResult.getError().toString());
        } else {
            final AccessToken accessToken = loginResult.getAccessToken();
            if (accessToken != null) {
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    /**
                     * It gets the user from the AccountKit, then stores it in the shared preferences
                     * @param account {Account}
                     */
                    @Override
                    public void onSuccess(final Account account) {
                        String user = account.getEmail();
                        if (user == null || user.equals("")) {
                            user = account.getPhoneNumber().getCountryCode() + "-" +
                                    account.getPhoneNumber().getPhoneNumber();
                        }
                        SharedPreferences.Editor editor = getSharedPreferences("com.kantpoll.android",
                                Context.MODE_PRIVATE).edit();
                        editor.putString("token", accessToken.getToken());
                        editor.putString("user", user);
                        editor.putString("registered", "false");
                        editor.apply();

                        final Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(final AccountKitError error) {
                        showMessage(error.toString());
                    }
                });

            } else {
                showMessage(getString(R.string.unknown_response_type));
            }
        }
    }

    /**
     * It displays a message in a new Message Activity
     *
     * @param message {String}
     */
    private void showMessage(String message) {
        final Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.MESSAGE_EXTRA, message);
        startActivity(intent);
    }

    /**
     * Accountkit functions
     *
     * @param loginType {LoginType}
     */
    private void onLogin(final LoginType loginType) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        final AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType,
                AccountKitActivity.ResponseType.TOKEN);

        configurationBuilder.setReadPhoneStateEnabled(false);
        configurationBuilder.setReceiveSMS(false);
        final AccountKitConfiguration configuration = configurationBuilder.build();
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
        OnCompleteListener completeListener = new OnCompleteListener() {
            @Override
            public void onComplete() {
                startActivityForResult(intent, FRAMEWORK_REQUEST_CODE);
            }
        };

        completeListener.onComplete();
    }

    private interface OnCompleteListener {
        void onComplete();
    }

    /**
     * Minimize the app on back pressed
     */
    @Override
    public void onBackPressed() {
       minimizeApp();
    }

    /**
     * Minimize the app on back pressed
     */
    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}
