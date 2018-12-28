package io.mobinity.moby.item;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class ItemInfoItem implements Parcelable {
    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReg_date() {
        return reg_date;
    }

    public void setReg_date(String reg_date) {
        this.reg_date = reg_date;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    private int seq;

    public int getRequest_seq() {
        return request_seq;
    }

    public void setRequest_seq(int request_seq) {
        this.request_seq = request_seq;
    }

    private int request_seq;
    private String file_name;
    private String description;
    private String reg_date;

    public ItemInfoItem(){
        //TODO:DB연동 후에 seq 관리 필요
        this.seq = 0;
    }
    protected ItemInfoItem(Parcel in) {
        seq = in.readInt();
        file_name = in.readString();
        description = in.readString();
        reg_date = in.readString();
    }

    public static final Creator<ItemInfoItem> CREATOR = new Creator<ItemInfoItem>() {
        @Override
        public ItemInfoItem createFromParcel(Parcel in) {
            return new ItemInfoItem(in);
        }

        @Override
        public ItemInfoItem[] newArray(int size) {
            return new ItemInfoItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(file_name);
        dest.writeString(description);
        dest.writeString(reg_date);
    }
}
