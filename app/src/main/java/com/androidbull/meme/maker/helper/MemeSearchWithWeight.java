package com.androidbull.meme.maker.helper;

import com.androidbull.meme.maker.model.Meme2;

public class MemeSearchWithWeight implements Comparable<MemeSearchWithWeight> {
    private Meme2 meme;
    private Integer weight;

    public MemeSearchWithWeight(final Meme2 meme) {
        this.meme = meme;
        this.weight = 0;
    }

    @Override
    public int compareTo(final MemeSearchWithWeight memeSearchWithWeight) {
        return memeSearchWithWeight.getWeight().compareTo(getWeight());
    }

    public Meme2 getMeme() {
        return this.meme;
    }

    /* public String getNameForSearchLowerCase() {
         return this.meme.getNameForSearchLowerCase();
     }

     public String[] getSearchTagsLowerCase() {
         return this.meme.getSearchTagsLowerCase();
     }
     */
    public Integer getWeight() {
        return this.weight;
    }

    public void incrementWeight() {
        synchronized (this) {
            final Integer weight = this.weight;
            ++this.weight;
        }
    }

    public void incrementWeightForNameMatch() {
        synchronized (this) {
            this.weight += 25;
        }
    }

    public void incrementWeightForNameMatch(int weight) {
        synchronized (this) {
            this.weight += weight;
        }
    }

    public void setMeme(final Meme2 meme) {
        this.meme = (Meme2) meme;
    }
}
