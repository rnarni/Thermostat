package com.espthermostat.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by raghuramn on 12/20/15.
 */
public class DeviceList extends ArrayList<Device> implements Parcelable {

    private static final long serialVersionUID = 663585476779879096L;

    public DeviceList() {

    }

    public DeviceList(Parcel in) {

        readFromParcel(in);

    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public DeviceList createFromParcel(Parcel in) {

            return new DeviceList(in);

        }

        public Object[] newArray(int arg0) {

            return null;

        }

    };

    private void readFromParcel(Parcel in) {

        this.clear();

        // First we have to read the list size

        int size = in.readInt();

        // Reading remember that we wrote first the Name and later the Phone
        // Number.

        // Order is fundamental

        for (int i = 0; i < size; i++) {

            Device c = new Device();

            c.setName(in.readString());

            c.setId(in.readInt());

            this.add(c);

        }

    }

    public int describeContents() {

        return 0;

    }

    public void writeToParcel(Parcel dest, int flags) {

        int size = this.size();

        // We have to write the list size, we need him recreating the list

        dest.writeInt(size);

        // We decided arbitrarily to write first the Name and later the Phone
        // Number.

        for (int i = 0; i < size; i++) {

            Device c = this.get(i);

            dest.writeString(c.getName());

            dest.writeInt(c.getId());

        }

    }
}
