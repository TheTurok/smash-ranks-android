package com.garpr.android.models;

/**
 * Created by Turok on 9/20/2014.
 */
public class Player {
    private String id;
    private String name;
    private int rank;
    private float rating;

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getRank(){
        return rank;
    }

    public float getRating(){
        return rating;
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Player) {
            final Player p = (Player) o;
            isEqual = id.equals(p.getId());
        } else {
            isEqual = false;
        }

        return isEqual;
    }
}


