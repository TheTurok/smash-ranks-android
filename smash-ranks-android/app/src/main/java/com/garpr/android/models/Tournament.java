package com.garpr.android.models;

/**
 * Created by Turok on 9/20/2014.
 */
public class Tournament {
    private String id;
    private String name;
    private String date;

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(final Object o){
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Tournament) {
            final Tournament t = (Tournament) o;
            isEqual = id.equals(t.getId());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


}
