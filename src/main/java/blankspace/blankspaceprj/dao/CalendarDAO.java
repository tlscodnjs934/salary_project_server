package blankspace.blankspaceprj.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
@Repository
public interface CalendarDAO {
    //회원별 달력 조회
    HashMap<String, Object> selectCalendar(HashMap<String, Object> param);

    //회원별 일별 스케줄 리스트 조회
    ArrayList<HashMap<String, Object>> selectScheduleList(HashMap<String, Object> param);

    //회원별 일별 스케줄 MERGE
    int mergeSchedule(HashMap<String, Object> param);

    //회원별 일별 달력 MERGE
    int mergeCalendar(HashMap<String, Object> param);
}
