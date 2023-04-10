package blankspace.blankspaceprj.controller;

import blankspace.blankspaceprj.dto.ResultDTO;
import blankspace.blankspaceprj.service.MemberServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@Api(tags = {"회원가입 API"})
@RequestMapping(value = "/api/member/")
public class MemberController {

    @Autowired
    MemberServiceImpl userService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "findAll", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value="회원 목록 전체 조회", notes="회원 목록을 전체 조회하는 API")
    public ResponseEntity<?> findAllMember() throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        responseDTO.setResultCode("0");
        responseDTO.setData(userService.findAll());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="일반 회원 가입 수행", notes="일반 회원 가입을 수행하는 API")
    @RequestMapping(value = "joinMember", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> joinMember(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();

        //회원가입 서비스 호출
        HashMap<String, Object> resultMap = userService.joinMember(param);

        //결과 코드 및 데이터 세팅
        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //카카오 로그인 페이지 호출
    @GetMapping("kakao")
    public String hello(Model model){
        model.addAttribute("data", "hello!!!");
        return  "<a href=\"https://kauth.kakao.com/oauth/authorize?client_id=0a57a2699657f4e2b2e2b760f8e0dc51&redirect_uri=http://127.0.0.1:8080/api/member/receiveKakaoCode&response_type=code\" >카카오 로그인</a>";
    }

    //카카오 페이지 로그인 후 CODE 받아오기. 후에 인증 시 필요함
    @RequestMapping(value = "receiveKakaoCode", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> receiveKakaoCode(@RequestParam("code") String code) throws Exception {
        logger.debug("receiveKakaoCode 탐"+ code);
        System.out.println("receiveKakaoCode 탐"+ code);
        ResultDTO responseDTO = new ResultDTO();
        HashMap<String, Object> resultMap;
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);
        //카카오 토큰 받기 시작
        resultMap = userService.receiveKakaoToken(map);

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="카카오 회원 가입 수행", notes="카카오 회원 가입을 수행하는 API")
    @RequestMapping(value = "joinMemberKakao", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> joinMemberKakao(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();


        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}