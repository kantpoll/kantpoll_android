/*
 * Kantpoll Project
 * https://github.com/kantpoll
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kantpoll.android;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableMessage implements Parcelable {
    /** After implementing the `Parcelable` interface, we need to create the
     * `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
     * Notice how it has our class specified as its type.
     */
    public static final Creator<ParcelableMessage> CREATOR
            = new Creator<ParcelableMessage>() {

        /**
         * This simply calls our new constructor (typically private) and
         * passes along the unmarshalled `Parcel`, and then returns the new object!
         * @param in {Parcel}
         * @return {ParcelableMessage}
         */
        @Override
        public ParcelableMessage createFromParcel(Parcel in) {
            return new ParcelableMessage(in);
        }

        /**
         * We just need to copy this and change the type to match our class.
         * @param size {int}
         * @return {ParcelableMessage[]}
         */
        @Override
        public ParcelableMessage[] newArray(int size) {
            return new ParcelableMessage[size];
        }
    };
    // You can include parcel data types
    private final String data;

    /** Using the `in` variable, we can retrieve the values that
     * we originally wrote into the `Parcel`.  This constructor is usually
     * private so that only the `CREATOR` field can access.
     */
    private ParcelableMessage(Parcel in) {
        data = in.readString();
    }

    /**
     * It is used to export the vault (and password)
     * @param str {String}
     */
    protected ParcelableMessage(String str) {
        data = str;
    }

    /** This is where you write the values you want to save to the `Parcel`.
     * The `Parcel` class has methods defined to help you save all of your values.
     * Note that there are only methods defined for simple values, lists, and other Parcelable objects.
     * You may need to make several classes Parcelable to send the data you want.
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(data);
    }

    /**
     * It is used to display the password on the MessageActivity
     */
    public String data() {
        return data;
    }

    /**
     * In the vast majority of cases you can simply return 0 for this.
     * @return {int}
     */
    @Override
    public int describeContents() {
        return 0;
    }
}
