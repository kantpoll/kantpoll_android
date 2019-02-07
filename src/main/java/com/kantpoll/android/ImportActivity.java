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

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ImportActivity extends AppCompatActivity {
    private final static int SAVE_PASSWORD_REQUEST_CODE = 1;

    /**
     * It creates a simple activity to export a vault
     *
     * @param savedInstanceState {Bundle}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        Toolbar importToolbar = findViewById(R.id.import_toolbar);
        importToolbar.setTitle("");
        setSupportActionBar(importToolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        final TextView textView1 = findViewById(R.id.import_label);
        textView1.setText(getString(R.string.import_label));

        final TextInputLayout til1 = findViewById(R.id.username_til);
        til1.setHint(getString(R.string.vault_username));

        final TextInputLayout til2 = findViewById(R.id.password_til);
        til2.setHint(getString(R.string.vault_password));

        final TextInputLayout til3 = findViewById(R.id.vault_til);
        til3.setHint(getString(R.string.vault_content));

        final Button button = findViewById(R.id.import_button);
        button.setText(getString(R.string.import_vault));
    }

    /**
     * Displaying the menu
     *
     * @param menu {Menu}
     * @return {boolean}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * It calls the MainActivity
     *
     * @param item {MenuItem}
     */
    public void goBack(MenuItem item) {
        Intent maIntent = new Intent(this, MainActivity.class);
        startActivity(maIntent);
        finish();
    }

    /**
     * It calls the goBack function
     */
    @Override
    public void onBackPressed() {
        goBack(null);
    }

    /**
     * It stores the username, password and vault content
     *
     * @param view {View}
     */
    public void importVault(View view) {
        showAuthenticationScreen();
    }

    /**
     * It is called after user authentication
     *
     * @param requestCode {int}
     * @param resultCode  {int}
     * @param data        {Intent}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SAVE_PASSWORD_REQUEST_CODE) {
            final EditText editText1 = findViewById(R.id.vault_et);
            String[] parts = editText1.getText().toString().split("\\r?\\n");

            if (parts.length < 2) {
                parts = editText1.getText().toString().split(" 0x");
                if (parts.length < 2) {
                    Toast.makeText(getApplication(), R.string.error_label,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                parts[1] = "0x" + parts[1];
            }
            String words = parts[0];
            String the_data = parts[1];

            final EditText editText2 = findViewById(R.id.username_et);
            String user = editText2.getText().toString();

            final EditText editText3 = findViewById(R.id.password_et);
            String password = editText3.getText().toString();

            if (user.equals("") || password.equals("")) {
                Toast.makeText(getApplication(), R.string.error_label,
                        Toast.LENGTH_LONG).show();
                return;
            }

            UserAuthentication userAuthentication = new UserAuthentication(this);
            try {
                userAuthentication.saveUserPassword(password);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getApplication(), R.string.error_label,
                        Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences.Editor editor =
                    getSharedPreferences("com.kantpoll.android", Context.MODE_PRIVATE).edit();
            editor.putString("words", words);
            editor.putString("data", the_data);
            editor.putString("user", user);
            editor.apply();

            Intent maIntent = new Intent(this, MainActivity.class);
            startActivity(maIntent);
        }
    }

    /**
     * It calls the system's authentication screen
     */
    private void showAuthenticationScreen() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null){
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
            if (intent != null) {
                startActivityForResult(intent, SAVE_PASSWORD_REQUEST_CODE);
            }
        }
    }
}
