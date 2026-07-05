package ar.edu.itba.paw.webapp.dto;

import java.util.Objects;

public class RatingsDto {

    private int oneStar;
    private int twoStars;
    private int threeStars;
    private int fourStars;
    private int fiveStars;

    public RatingsDto() {
    }

    public RatingsDto(int oneStar, int twoStars, int threeStars, int fourStars, int fiveStars) {
        this.oneStar = oneStar;
        this.twoStars = twoStars;
        this.threeStars = threeStars;
        this.fourStars = fourStars;
        this.fiveStars = fiveStars;
    }

    public int getOneStar() {
        return oneStar;
    }

    public void setOneStar(int oneStar) {
        this.oneStar = oneStar;
    }

    public int getTwoStars() {
        return twoStars;
    }

    public void setTwoStars(int twoStars) {
        this.twoStars = twoStars;
    }

    public int getThreeStars() {
        return threeStars;
    }

    public void setThreeStars(int threeStars) {
        this.threeStars = threeStars;
    }

    public int getFourStars() {
        return fourStars;
    }

    public void setFourStars(int fourStars) {
        this.fourStars = fourStars;
    }

    public int getFiveStars() {
        return fiveStars;
    }

    public void setFiveStars(int fiveStars) {
        this.fiveStars = fiveStars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingsDto that = (RatingsDto) o;
        return oneStar == that.oneStar && twoStars == that.twoStars && threeStars == that.threeStars &&
                fourStars == that.fourStars && fiveStars == that.fiveStars;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oneStar, twoStars, threeStars, fourStars, fiveStars);
    }
}

