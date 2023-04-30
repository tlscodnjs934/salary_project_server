package blankspace.blankspaceprj.service;

import blankspace.blankspaceprj.dao.CalendarDAO;
import org.apache.ibatis.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;


@Service
public class CalendarServiceImpl {
    @Autowired
    CalendarDAO calendarDAO;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //회원별 달력 조회
    public HashMap<String, Object> selectCalendar(HashMap<String, Object> param){
        logger.info("*********selectCalendar start***********" + param);
        HashMap<String, Object> result = new HashMap<>();

        if(!MapUtils.containsKey(param, "ID")){
            result.put("resultCode", "-1");
            result.put("resultMsg", "ID가 입력되지 않았습니다.");

            return result;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            result.put("resultCode", "-1");
            result.put("resultMsg", "가입경로가 입력되지 않았습니다.");

            return result;
        }

        if(!MapUtils.containsKey(param, "DATE")){
            result.put("resultCode", "1");
            result.put("resultMsg", "날짜가 입력되지 않았습니다.");

            return result;
        }

        result = calendarDAO.selectCalendar(param);

        result.put("resultCode", "0");
        result.put("resultMsg", "회원별 calendar 조회 완료");

        return result;
    }

    //회원별 일별 스케줄 리스트 조회
    public HashMap<String, Object> selectScheduleList(HashMap<String, Object> param){
        logger.info("*********selectScheduleList start***********" + param);
        HashMap<String, Object> result = new HashMap<String, Object>();

        if(!MapUtils.containsKey(param, "ID")){
            result.put("resultCode", "-1");
            result.put("resultMsg", "ID가 입력되지 않았습니다.");

            return result;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            result.put("resultCode", "-1");
            result.put("resultMsg", "가입경로가 입력되지 않았습니다.");

            return result;
        }

        if(!MapUtils.containsKey(param, "DATE")){
            result.put("resultCode", "1");
            result.put("resultMsg", "날짜가 입력되지 않았습니다.");

            return result;
        }

        ArrayList<HashMap<String, Object>> dataList = calendarDAO.selectScheduleList(param);

        result.put("resultCode", "0");
        result.put("resultMsg", "회원별 calendar 조회 완료");
        result.put("dataList", dataList);

        return result;
    }

    //회원별 일별 스케줄 리스트 Merge
    public HashMap<String, Object> mergeScheduleList(ArrayList<HashMap<String, Object>> paramList){
        logger.info("*******mergeScheduleList 등록 param : " + paramList);
        HashMap result = new HashMap();
        HashMap eachMap = new HashMap();

        int totalCnt = 0;

        for(int i = 0; i < paramList.size(); i++){
            eachMap.putAll(paramList.get(i));

            int cnt = calendarDAO.mergeSchedule(eachMap);

            if(cnt == 1){
                logger.info("스케줄 업데이트 성공 " + eachMap);
                totalCnt = totalCnt + cnt;
            }else {
                logger.info("스케줄 업데이트 실패 " + eachMap);
            }


            eachMap.clear();
        }

        if(totalCnt == paramList.size()){
            result.put("resultCode", "0");
            result.put("resultMsg", "스케줄 List 업데이트 완료");
        }else{
            result.put("resultCode", "-1");
            result.put("resultMsg", "스케줄 List 업데이트 실패");
        }

        return result;
    }

    //회원별 일별 스케줄 리스트 등록
    public HashMap<String, Object> mergeCalendar(HashMap<String, Object> param){
        logger.info("*******mergeScheduleList 등록 param : " + param);
        HashMap result = new HashMap();

        int cnt = calendarDAO.mergeSchedule(param);

        if(cnt == 1){
            logger.info("달력 업데이트 성공 " + param);
        }else {
            logger.info("달력 업데이트 실패 " + param);
        }

        if(cnt == 1){
            result.put("resultCode", "0");
            result.put("resultMsg", "달력 업데이트 완료");
        }else{
            result.put("resultCode", "-1");
            result.put("resultMsg", "달력 업데이트 실패");
        }

        return result;
    }
}
