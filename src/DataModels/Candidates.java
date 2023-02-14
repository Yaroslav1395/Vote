package DataModels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Candidates {
    private List<Candidate> candidateList = new ArrayList<>();
    private transient boolean userStatus = false;

    public Candidates() {
    }

    public Candidates(List<Candidate> candidateList) {
        this.candidateList = candidateList;
    }

    public Candidates(Candidates candidates) {
        candidateList = candidates.getCandidateList();
    }

    public boolean isUserStatus() {
        return userStatus;
    }

    public void setUserStatus(boolean userStatus) {
        this.userStatus = userStatus;
    }

    public void setCandidateList(List<Candidate> candidateList) {
        this.candidateList = candidateList;
    }

    /**
     * Метод создает копию списка кандитатов
     * @return вернет копи списка
     */
    public List<Candidate> getCandidateList() {
        List<Candidate> copyList = new ArrayList<>();
        candidateList.forEach(candidate -> copyList.add(new Candidate(candidate)));
        return copyList;
    }

    /**
     * Метод определит индекс объекта по его имени
     * Ипользуется в шаблоне
     * @param name - имя для поиска нужного объекта
     * @return - вернет индекс найденного объекта
     */
    public int getIndexByName(String name){
        for (Candidate candidate: candidateList) {
            if(candidate.getName().equals(name)){
                return candidateList.indexOf(candidate);
            }
        }
        return candidateList.size();
    }

    /**
     * Добавит один бал участнику, найдя его по индексу
     * @param index - для поиска
     */
    public void plusPointToCandidateWithIndex(int index){
        candidateList.get(index).setVoteCount(1);
    }

    /**
     * Поиск кандидата по индексу
     * @param index - для поиска
     * @return - вернет найденного кандидата
     */
    public Candidate getCandidateByIndex(int index){
        return candidateList.get(index);
    }

    /**
     * Вычислит процент голосов для каждого кандитата
     */
    public void takeCandidatesPercentage(){
        int votesNumber = candidateList.stream().mapToInt(Candidate::getVoteCount).sum();
        System.out.println(votesNumber);
        candidateList.forEach(candidate -> candidate.setPercent(candidate.getVoteCount() * 100f / votesNumber));
    }

    /**
     * Сортирует список кандидатов по количеству голов
     */
    public void sortCandidatesByVotes(){
        candidateList =  candidateList.stream().sorted(Comparator.comparingInt(Candidate::getVoteCount).reversed()).collect(Collectors.toList());
    }
}
