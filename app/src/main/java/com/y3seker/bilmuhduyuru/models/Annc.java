package com.y3seker.bilmuhduyuru.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;

import com.y3seker.bilmuhduyuru.Utils;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Yunus Emre Şeker on 03.03.2015.
 * -
 */
public class Annc implements Parcelable {

    private static final String rootContUrl = "http://egeduyuru.ege.edu.tr/icerikgoster_5.php?id=";

    Date date, created;
    String title, url, shortUrl, content, dateString;
    int index;

    // RestClient should call this
    public Annc(String title, String url, Date date, Date created, String content, int index) {
        this.title = title;
        this.url = url;
        this.date = date;
        this.created = created;
        this.content = content;
        this.index = index;
        this.dateString = getDate();
        this.shortUrl = rootContUrl + index;
    }

    public Annc(String title, String date, String url, String content, int index) throws ParseException {
        this(title, url, Utils.dbFormat.parse(date), null, content, index);
    }

    public Annc(String title, Date date, String content, int index) {
        this(title, rootContUrl + index, date, null, content, index);
    }

    protected Annc(Parcel in) {
        title = in.readString();
        url = in.readString();
        shortUrl = in.readString();
        content = in.readString();
        dateString = in.readString();
        index = in.readInt();
    }

    public static final Creator<Annc> CREATOR = new Creator<Annc>() {
        @Override
        public Annc createFromParcel(Parcel in) {
            return new Annc(in);
        }

        @Override
        public Annc[] newArray(int size) {
            return new Annc[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return Utils.dateFormat.format(date);
    }

    public String getDateString() {
        return dateString;
    }

    public String getDbDate() {
        return Utils.dbFormat.format(date);
    }

    public String getFullDate() {
        return Utils.dateFullFormat.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isContains(String input, boolean inContent) {

        String parsedIcerik = Html.fromHtml(content).toString();
        String inputLower = input.toLowerCase(Utils.trLoc);
        if (inContent) {
            return title.toLowerCase(Utils.trLoc).contains(inputLower) || parsedIcerik.contains(inputLower);
        } else {
            return title.toLowerCase(Utils.trLoc).contains(inputLower);
        }
    }

    public boolean isContains(String input) {
        String parsedIcerik = Html.fromHtml(content).toString();
        return title.toLowerCase(Utils.trLoc).contains(input.toLowerCase(Utils.trLoc)) ||
                parsedIcerik.contains(input.toLowerCase(Utils.trLoc));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(shortUrl);
        dest.writeString(content);
        dest.writeString(dateString);
        dest.writeInt(index);
    }
}
