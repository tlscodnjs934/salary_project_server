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

    //회원별 월별 달력 조회
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

        if(!MapUtils.containsKey(param, "YEAR_MONTH")){
            result.put("resultCode", "-1");
            result.put("resultMsg", "YEAR_MONTH가 입력되지 않았습니다.");

            return result;
        }

        ArrayList<HashMap<String, Object>> calendar = calendarDAO.selectCalendar(param);

        result.put("resultCode", "0");
        result.put("resultMsg", "회원별 calendar 조회 완료");
        result.put("dataList", calendar);

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

        if(!MapUtils.containsKey(param, "SCHEDULE_DATE")){
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
    public HashMap<String, Object> mergeScheduleList (ArrayList<HashMap<String, Object>> paramList){
        logger.info("*******mergeScheduleList 등록 param : " + paramList);
        HashMap result = new HashMap();
        HashMap eachMap = new HashMap();

        int totalCnt = 0;

        for(int i = 0; i < paramList.size(); i++){
            eachMap.putAll(paramList.get(i));

            if(!MapUtils.containsKey(eachMap, "ID")){
                result.put("resultCode", "-1");
                result.put("resultMsg", "회원 ID가 입력되지 않았습니다.");
                return result;
            }

            if(!MapUtils.containsKey(eachMap, "AUTH_TYPE")){
                result.put("resultCode", "-1");
                result.put("resultMsg", "회원 AUTH_TYPE가 입력되지 않았습니다.");
                return result;
            }

            if(!MapUtils.containsKey(eachMap, "SCHEDULE_DATE")){
                result.put("resultCode", "-1");
                result.put("resultMsg", "SCHEDULE_DATE가 입력되지 않았습니다.");
                return result;
            }

            if(!MapUtils.containsKey(eachMap, "SN")){
                result.put("resultCode", "-1");
                result.put("resultMsg", "SN가 입력되지 않았습니다.");
                return result;
            }

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

    //회원별 일별 캘린더 등록
    public HashMap<String, Object> mergeCalendar(HashMap<String, Object> param){
        logger.info("*******mergeCalendar 등록 param : " + param);
        HashMap result = new HashMap();

        if(!MapUtils.containsKey(param, "ID")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 ID가 입력되지 않았습니다.");
            return param;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 AUTH_TYPE가 입력되지 않았습니다.");
            return param;
        }

        if(!MapUtils.containsKey(param, "SCHEDULE_DATE")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 SCHEDULE_DATE가 입력되지 않았습니다.");
            return param;
        }

        int cnt = calendarDAO.mergeCalendar(param);

        if (cnt >= 1){
            logger.info("달력 업데이트 성공 " + param);
            result.put("resultCode", "0");
            result.put("resultMsg", "달력 업데이트 완료");

            return result;
        }else {
            logger.info("달력 업데이트 실패 " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "달력 업데이트 실패");

            return result;
        }


    }

    //일별 추가근무 조회
    public HashMap<String, Object> selectScheduleExtraWork(HashMap<String, Object> param){
        logger.info("*********selectScheduleExtraWork start***********" + param);
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

        if(!MapUtils.containsKey(param, "SCHEDULE_DATE")){
            result.put("resultCode", "1");
            result.put("resultMsg", "날짜가 입력되지 않았습니다.");

            return result;
        }

        HashMap<String, Object> data = calendarDAO.selectScheduleExtraWork(param);

        result.put("resultCode", "0");
        result.put("resultMsg", "일별 추가근무 조회 완료");
        result.put("data", data);

        return result;
    }

    //현재 달의 첫일부터 현재까지 EXTRAWORK SUM
    public HashMap<String, Object> selectSumExtraWork(HashMap<String, Object> param){
        logger.info("*********selectSumExtraWork start***********" + param);
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

        HashMap<String, Object> data = calendarDAO.selectSumExtraWork(param);

        result.put("resultCode", "0");
        result.put("resultMsg", "일별 추가근무 조회 완료");
        result.put("data", data);

        return result;
    }

    public HashMap<String, Object> mergeExtraWork(HashMap<String, Object> param){
        logger.info("*******mergeExtraWork 등록 param : " + param);
        HashMap result = new HashMap();

        if(!MapUtils.containsKey(param, "ID")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 ID가 입력되지 않았습니다.");
            return param;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 AUTH_TYPE가 입력되지 않았습니다.");
            return param;
        }

        if(!MapUtils.containsKey(param, "SCHEDULE_DATE")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 SCHEDULE_DATE가 입력되지 않았습니다.");
            return param;
        }

        int cnt = calendarDAO.mergeExtraWork(param);

        if (cnt >= 1){
            logger.info("추가근무 업데이트 성공 " + param);
            result.put("resultCode", "0");
            result.put("resultMsg", "추가근무 업데이트 완료");

            return result;
        }else {
            logger.info("추가근무 업데이트 실패 " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "추가근무 업데이트 실패");

            return result;
        }


    }

}
