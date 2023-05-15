package blankspace.blankspaceprj.controller;

import blankspace.blankspaceprj.dto.ResultDTO;
import blankspace.blankspaceprj.service.CalendarServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@Api(tags = {"캘린더 API"})
@RequestMapping(value = "/api/calendar/")
public class CalendarController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CalendarServiceImpl calendarService;

    @RequestMapping(value = "selectCalendar", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="YEAR_MONTH", value = "년월 (YYYYMM)", required = true, dataType = "String")})
    @ApiOperation(value="회원별 월별 달력 조회", notes="회원별 월별 달력을 조회하는 API (해당 년월에 대한 List 응답)")
    public ResponseEntity<?> selectCalendar(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();

        HashMap<String, Object> result = calendarService.selectCalendar(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());
        responseDTO.setData(result.get("dataList"));

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "selectScheduleList", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="SCHEDULE_DATE", value = "일정날짜 (YYYYMMDD)", required = true, dataType = "String")})
    @ApiOperation(value="회원별 일별 스케줄 리스트 조회", notes="회원별 일별 스케줄 리스트를 조회하는 API")
    public ResponseEntity<?> selectScheduleList(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.selectScheduleList(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());
        responseDTO.setData(result.get("dataList"));

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "mergeScheduleList", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="SCHEDULE_DATE", value = "일정날짜 (YYYYMMDD)", required = true, dataType = "String"),
            @ApiImplicitParam(name="SN", value = "순번 (1씩 증가해서 넘겨주세요)", required = true, dataType = "int"),
            @ApiImplicitParam(name="TO_DO", value = "할 일", required = false, dataType = "String"),
            @ApiImplicitParam(name="CHK_YN", value = "체크여부 (Y 또는 N)", required = false, dataType = "String"),
            @ApiImplicitParam(name="TIME_START", value = "일정 시간 시작 (ex : 1010)", required = false, dataType = "String"),
            @ApiImplicitParam(name="TIME_END", value = "일정 시간 종료 (ex : 1201)", required = false, dataType = "String"),
            @ApiImplicitParam(name="DEL_YN", value = "삭제여부 (Y 또는 N)", required = false, dataType = "String")})
    @ApiOperation(value="회원별 일별 스케줄 등록/업데이트", notes="회원별 일별 스케줄 LIST 등록/업데이트 하는 API")
    public ResponseEntity<?> mergeScheduleList(@RequestBody ArrayList<HashMap<String, Object>> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.mergeScheduleList(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "mergeCalendarList", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="SCHEDULE_DATE", value = "일정날짜 (YYYYMMDD)", required = true, dataType = "String"),
            @ApiImplicitParam(name="STATE", value = "상태", required = false, dataType = "String"),
            @ApiImplicitParam(name="CONTENT", value = "내용", required = false, dataType = "String"),
            @ApiImplicitParam(name="DAY_OFF", value = "휴일 (ex : 1010)", required = false, dataType = "String"),
            @ApiImplicitParam(name="DEL_YN", value = "삭제여부 (Y 또는 N)", required = false, dataType = "String")})
    @ApiOperation(value="회원별 달력 등록/업데이트", notes="회원별 달력 등록/업데이트 하는 API")
    public ResponseEntity<?> mergeCalendarList(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.mergeCalendar(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "selectScheduleExtraWork", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="SCHEDULE_DATE", value = "일정날짜 (YYYYMMDD)", required = true, dataType = "String")})
    @ApiOperation(value="회원별 일별 추가근무 조회", notes="회원별 일별 스케줄 리스트를 조회하는 API")
    public ResponseEntity<?> selectScheduleExtraWork(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.selectScheduleExtraWork(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());
        responseDTO.setData(result.get("data"));

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "selectSumExtraWork", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
          })
    @ApiOperation(value="현재 달의 첫일부터 현재까지 EXTRAWORK SUM", notes="현재 달의 첫일부터 현재일자까지 EXTRAWORK SUM을 조회하는 API")
    public ResponseEntity<?> selectSumExtraWork(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.selectSumExtraWork(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());
        responseDTO.setData(result.get("data"));

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "mergeExtraWork", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="SCHEDULE_DATE", value = "일정날짜 (YYYYMMDD)", required = true, dataType = "String"),
            @ApiImplicitParam(name="SALARY", value = "월급", required = false, dataType = "int"),
            @ApiImplicitParam(name="DEL_YN", value = "삭제여부 (Y 또는 N)", required = false, dataType = "String")})
    @ApiOperation(value="회원별 추가근무 등록/업데이트", notes="회원별 추가근무 등록/업데이트 하는 API")
    public ResponseEntity<?> mergeExtraWork(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.mergeExtraWork(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
