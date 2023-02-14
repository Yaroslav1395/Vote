package DataModels;

public class Candidate {
    private String name;
    private final String photo;
    private int voteCount;
    private transient double percent;
    public Candidate(Candidate candidate){
        name = candidate.getName();
        photo = candidate.getPhoto();
        voteCount = candidate.getVoteCount();
        percent = candidate.getPercent();
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    public double getPercent() {
        return percent;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount += voteCount;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", photo='" + photo + '\'' +
                ", voteCount=" + voteCount +
                ", percent=" + percent +
                '}';
    }
}
