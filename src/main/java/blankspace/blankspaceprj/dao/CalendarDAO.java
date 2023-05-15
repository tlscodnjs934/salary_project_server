package blankspace.blankspaceprj.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
@Repository
public interface CalendarDAO {
    //회원별 월별 달력 조회
    ArrayList<HashMap<String, Object>> selectCalendar(HashMap<String, Object> param);

    //회원별 일별 스케줄 리스트 조회
    ArrayList<HashMap<String, Object>> selectScheduleList(HashMap<String, Object> param);

    //회원별 일별 스케줄 MERGE
    int mergeSchedule(HashMap<String, Object> param);

    //회원별 일별 달력 MERGE
    int mergeCalendar(HashMap<String, Object> param);

    //추가근무 조회
    HashMap<String, Object> selectScheduleExtraWork(HashMap<String, Object> param);

    //현재 달의 첫일부터 현재까지 EXTRAWORK SUM
    HashMap<String, Object> selectSumExtraWork(HashMap<String, Object> param);

    //회원별 일별 추가근무 MERGE
    int mergeExtraWork(HashMap<String, Object> param);

}
