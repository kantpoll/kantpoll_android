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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ExportActivity extends AppCompatActivity {
    static final String USERNAME_EXTRA = "USERNAME_EXTRA";
    static final String PASSWORD_EXTRA = "PASSWORD_EXTRA";
    static final String VAULT_EXTRA = "VAULT_EXTRA";

    /**
     * It creates a simple activity to export a vault
     *
     * @param savedInstanceState {Bundle}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        Toolbar exportToolbar = findViewById(R.id.export_toolbar);
        exportToolbar.setTitle("");
        setSupportActionBar(exportToolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        final TextView textView0 = findViewById(R.id.export_label1);
        textView0.setText(getString(R.string.vault_username));

        final ParcelableMessage username = getIntent().getParcelableExtra(USERNAME_EXTRA);
        final TextView textView1 = findViewById(R.id.username_text);
        textView1.setText(username.data());

        final TextView textView2 = findViewById(R.id.export_label2);
        textView2.setText(getString(R.string.copy_password));

        final ParcelableMessage password = getIntent().getParcelableExtra(PASSWORD_EXTRA);
        final TextView textView3 = findViewById(R.id.password_text);
        textView3.setText(password.data());

        final TextView textView4 = findViewById(R.id.vault_label);
        textView4.setText(getString(R.string.vault_content));

        final ParcelableMessage vault = getIntent().getParcelableExtra(VAULT_EXTRA);
        final TextView textView5 = findViewById(R.id.vault_content);
        textView5.setText(vault.data());

        final Button button = findViewById(R.id.export_button);
        button.setText(getString(R.string.export));
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
     * It calls the HomeActivity
     *
     * @param item {MenuItem}
     */
    public void goBack(MenuItem item) {
        Intent haIntent = new Intent(this, HomeActivity.class);
        startActivity(haIntent);
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
     * It calls the action send intent
     *
     * @param view {View}
     */
    public void export(View view) {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        final ParcelableMessage vault = getIntent().getParcelableExtra(VAULT_EXTRA);
        sendIntent.putExtra(Intent.EXTRA_TEXT, vault.data());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
