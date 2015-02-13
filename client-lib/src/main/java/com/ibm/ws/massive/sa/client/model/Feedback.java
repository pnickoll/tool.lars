/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ibm.ws.massive.sa.client.model;

public class Feedback extends AbstractJSON {

    private int downloads;
    private int favorites;
    private int likes;
    private int rating;
    private int reviews;
    private int tweets;

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public int getTweets() {
        return tweets;
    }

    public void setTweets(int tweets) {
        this.tweets = tweets;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + downloads;
        result = prime * result + favorites;
        result = prime * result + likes;
        result = prime * result + rating;
        result = prime * result + reviews;
        result = prime * result + tweets;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Feedback other = (Feedback) obj;
        if (downloads != other.downloads)
            return false;
        if (favorites != other.favorites)
            return false;
        if (likes != other.likes)
            return false;
        if (rating != other.rating)
            return false;
        if (reviews != other.reviews)
            return false;
        if (tweets != other.tweets)
            return false;
        return true;
    }

}
