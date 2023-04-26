package blankspace.blankspaceprj.controller;

import blankspace.blankspaceprj.dto.ResultDTO;
import blankspace.blankspaceprj.service.CalendarServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @ApiOperation(value="회원별 달력 조회", notes="회원별 달력을 조회하는 API")
    public ResponseEntity<?> selectCalendar(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        HashMap result = new HashMap();

        result = calendarService.selectCalendar(param);

        responseDTO.setResultCode(result.get("resultCode").toString());
        responseDTO.setResultMsg(result.get("resultMsg").toString());
        responseDTO.setData(result);

        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "selectScheduleList", method = RequestMethod.POST)
    @ResponseBody
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

}
