import java.util.List;

public interface DiaryService {
    List<DiaryEntry> getMyDiaries();
    List<DiaryEntry> getSharedDiaries();
    void addDiary(DiaryEntry entry);
} 