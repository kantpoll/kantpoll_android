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
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {
    static final String MESSAGE_EXTRA = "MESSAGE_EXTRA";

    /**
     * It creates an extremely simple activity to display messages
     *
     * @param savedInstanceState {Bundle}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar messageToolbar = findViewById(R.id.message_toolbar);
        messageToolbar.setTitle("");
        setSupportActionBar(messageToolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        final ParcelableMessage message = getIntent().getParcelableExtra(MESSAGE_EXTRA);
        final TextView messageView = findViewById(R.id.message);
        if (messageView != null) {
            if (message != null) {
                messageView.setText(message.data());
            } else {
                messageView.setText(R.string.na);
            }
        }
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
     * It closes the MessageActivity
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
}
