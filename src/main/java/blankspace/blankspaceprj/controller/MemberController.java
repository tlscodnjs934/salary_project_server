package blankspace.blankspaceprj.controller;

import blankspace.blankspaceprj.dto.ResultDTO;
import blankspace.blankspaceprj.service.MemberServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@Api(tags = {"회원 관련 API"})
@RequestMapping(value = "/api/member/")
public class MemberController {

    @Autowired
    MemberServiceImpl userService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "findAll", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value="회원 목록 전체 조회", notes="회원 목록을 전체 조회하는 API")
    public ResponseEntity<?> findAllMember() throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        responseDTO.setResultCode("0");
        responseDTO.setResultMsg("아령이 바보");
        responseDTO.setData(userService.findAll());
        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="일반 회원 가입 수행", notes="일반 회원 가입을 수행하는 API")
    @RequestMapping(value = "joinMember", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> joinMember(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();




        param.put("AUTH_TYPE", "normal");
        param.put("AUTH", "normal");
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
    public RedirectView kakao(Model model){
        //model.addAttribute("data", "hello!!!");
        //return  "<a href=\"https://kauth.kakao.com/oauth/authorize?client_id=0a57a2699657f4e2b2e2b760f8e0dc51&redirect_uri=http://127.0.0.1:8080/api/member/receiveKakaoCode&response_type=code\" >카카오 로그인</a>";

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://kauth.kakao.com/oauth/authorize?client_id=0a57a2699657f4e2b2e2b760f8e0dc51&redirect_uri=http://localhost:8080/api/member/receiveKakaoCode&response_type=code");
        return redirectView;

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
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="카카오 회원 가입 수행", notes="카카오 회원 가입을 수행하는 API")
    @RequestMapping(value = "joinMemberKakao", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> joinMemberKakao(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();


        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="일반 회원 가입 수행", notes="일반 회원 가입을 수행하는 API")
    @RequestMapping(value = "nomalLogin", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> nomalLogin(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();

        userService.login(param);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //네이버 로그인 페이지 호출
    @GetMapping("naver")
    public RedirectView naverLogin(Model model){
        String redirect_url="http://127.0.0.1:8080/api/member/receiveNaverCode";
        String state;

        try {
            state = URLEncoder.encode(redirect_url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=3vgi_wT2Yv2Zn_xSuig5&redirect_uri=" + redirect_url + "&state=" + state);
        return redirectView;
    }

    //네이버 로그인 후 CODE 받아오기. 후에 인증 시 필요함
    @RequestMapping(value = "receiveNaverCode", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> receiveNaverCode(@RequestParam("code") String code, @RequestParam("state") String state) throws Exception {
        logger.info("receiveNaverCode 탐"+ code);

        ResultDTO responseDTO = new ResultDTO();
        HashMap<String, Object> resultMap;
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("state", state);
        //네이버 토큰 받기 시작
        resultMap = userService.receiveNaverToken(map);

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //네이버 로그인 후 CODE 받아오기. 후에 인증 시 필요함
    @RequestMapping(value = "receiveGoogleCode", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> receiveGoogleCode(@RequestBody HashMap<String, Object> param) throws Exception {
        logger.info("receiveGoogleCode 탐"+ param);

        ResultDTO responseDTO = new ResultDTO();
        HashMap<String, Object> resultMap;
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", param.get("token"));
        //map.put("state", state);
        //네이버 토큰 받기 시작
        resultMap = userService.receiveNaverToken(map);

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //필터 테스트
    @RequestMapping(value = "makeToken", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> makeToken(@RequestBody HashMap param) throws Exception {
        HashMap result = new HashMap();
        result = userService.login(param);
        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //아이디 까먹었을때 EMAIL로 회원 조회
    @ApiOperation(value="아이디 찾기", notes="아이디 까먹었을때 EMAIL로 회원 조회하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="EMAIL", value = "유저 이메일", required = true)})
    @RequestMapping(value = "findUserByEmail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> findUserByEmail(@RequestBody HashMap param) throws Exception {
        HashMap result = new HashMap();
        result = userService.findUserByEmail(param);
        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //회원가입 시 아이디 중복체크
    @ApiOperation(value="회원가입 시 아이디 중복체크", notes="회원가입 시 아이디 중복체크 조회하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true)})
    @RequestMapping(value = "CheckDuplicateUserByID", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> CheckDuplicateUserByID(@RequestBody HashMap param) throws Exception {
        HashMap result = new HashMap();
        result = userService.findMemberByIDForJoin(param);
        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //회원가입 시 이메일 중복체크
    @ApiOperation(value="회원가입 시 이메일 중복체크", notes="회원가입 시 이메일 중복체크 조회하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="EMAIL", value = "유저 이메일", required = true)})
    @RequestMapping(value = "CheckDuplicateUserByEmail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> CheckDuplicateUserByEmail(@RequestBody HashMap param) throws Exception {
        HashMap result = new HashMap();
        result = userService.findMemberByEmailForJoin(param);
        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    }