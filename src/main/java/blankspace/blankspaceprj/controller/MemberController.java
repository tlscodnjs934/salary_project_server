package blankspace.blankspaceprj.controller;

import blankspace.blankspaceprj.dto.ResultDTO;
import blankspace.blankspaceprj.service.MemberServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.util.MapUtils;

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
        responseDTO.setResultMsg("회원 목록 전체 조회 완료");
        responseDTO.setData(userService.findAll());
        logger.info("responseDTO : " + responseDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="일반 회원 가입 수행", notes="일반 회원 가입을 수행하는 API")
    @RequestMapping(value = "joinMember", method = RequestMethod.POST)
    @ApiImplicitParams({@ApiImplicitParam(name = "ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name = "AUTH_TYPE", value = "인증타입", required = true, dataType = "String"),
            @ApiImplicitParam(name = "PASSWORD", value = "비밀번호 (AUTH_TYPE = normal (일반회원가입)일 경우에만 필수)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "NAME", value = "이름", required = false, dataType = "String"),
            @ApiImplicitParam(name = "SALARY", value = "월급", required = false, dataType = "int"),
            @ApiImplicitParam(name = "NICKNAME", value = "닉네임", required = false, dataType = "String"),
            @ApiImplicitParam(name = "WORKING_DAY_CNT", value = "근무일수", required = false, dataType = "int"),
            @ApiImplicitParam(name = "PHONE", value = "폰번호", required = false, dataType = "String"),
            @ApiImplicitParam(name = "EMAIL", value = "이메일", required = false, dataType = "String"),
            @ApiImplicitParam(name = "DAY", value = "근무요일 (ex:목,금,월,화,수)", required = false, dataType = "String"),
    })
    @ResponseBody
    public ResponseEntity<?> joinMember(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();

        param.put("AUTH", "normal");
        //회원가입 서비스 호출
        HashMap<String, Object> resultMap = userService.joinMember(param);

        //결과 코드 및 데이터 세팅
        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());

        //비밀번호 응답 제거
        resultMap.remove("PASSWORD");

        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //카카오 로그인 페이지 호출
    @GetMapping("kakao")
    public RedirectView kakao(Model model){
        //model.addAttribute("data", "hello!!!");
        //return  "<a href=\"https://kauth.kakao.com/oauth/authorize?client_id=0a57a2699657f4e2b2e2b760f8e0dc51&redirect_uri=http://127.0.0.1:8080/api/member/receiveKakaoCode&response_type=code\" >카카오 로그인</a>";

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://kauth.kakao.com/oauth/authorize?client_id=0a57a2699657f4e2b2e2b760f8e0dc51&redirect_uri=http://43.201.134.176:8080/api/member/receiveKakaoCode&response_type=code");
        return redirectView;

    }



    //카카오 페이지 로그인 후 CODE 받아오기. 후에 인증 시 필요함
    @RequestMapping(value = "receiveKakaoCode", method = RequestMethod.POST)
    @ApiOperation(value="카카오 회원 가입 수행", notes="카카오 회원 가입을 수행하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name = "code", value = "코드", required = true, dataType = "String")
    })
    @ResponseBody
    public ResponseEntity<?> receiveKakaoCode(@RequestBody HashMap<String, Object> param) throws Exception {
        logger.info("receiveKakaoCode 탐"+ param);

        ResultDTO responseDTO = new ResultDTO();
        HashMap<String, Object> resultMap;
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", param.get("code"));
        //카카오 토큰 받기 시작
        resultMap = userService.receiveKakaoToken(map);

        resultMap.remove("AUTH");

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="로그인 수행", notes="로그인을 수행하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name = "ID", value = "회원 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name = "PASSWORD", value = "회원 비밀번호 (normal 로그인일 경우에만 필수)", required = true, dataType = "String")
    })
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO responseDTO = new ResultDTO();

        HashMap<String, Object> resultMap = userService.login(param);

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap.get("data"));

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //네이버 로그인 페이지 호출
    @GetMapping("naver")
    public RedirectView naverLogin(Model model){
        String redirect_url="http://43.201.134.176:8080/api/member/receiveNaverCode";
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
    @RequestMapping(value = "receiveNaverCode", method = RequestMethod.POST)
    @ApiOperation(value="네이버 회원 가입 수행", notes="네이버 회원 가입을 수행하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name = "code", value = "토큰", required = true, dataType = "String")
    })
    @ResponseBody
    public ResponseEntity<?> receiveNaverCode(//@RequestParam("code") String code
                                              @RequestBody HashMap<String, Object> param
            //, @RequestParam("state") String state
    ) throws Exception {
        logger.info("receiveNaverCode 탐"+ param);

        ResultDTO responseDTO = new ResultDTO();
        HashMap<String, Object> resultMap;
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", param.get("code"));
        //map.put("state", state);
        //네이버 토큰 받기 시작
        resultMap = userService.receiveNaverToken(map);

        resultMap.remove("AUTH");

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //구글 로그인 페이지 호출
    @GetMapping("google")
    public RedirectView google(Model model){

        RedirectView redirectView = new RedirectView();

        redirectView.setUrl("https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/userinfo.email&client_id=512350550459-e79v38hs0r6k6pvlcj8ke15ntf5fdcl9.apps.googleusercontent.com&response_type=code&redirect_uri=http://localhost:8080/api/member/receiveGoogleCode");

        //redirectView.setUrl("https://oauth2.googleapis.com?client_id=512350550459-e79v38hs0r6k6pvlcj8ke15ntf5fdcl9.apps.googleusercontent.com&redirect_uri=http://localhost:8080/api/member/receiveGoogleCode&grant_type=authorization_code&client_secret=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");
        return redirectView;

    }

    //네이버 로그인 후 CODE 받아오기. 후에 인증 시 필요함
    @RequestMapping(value = "receiveGoogleCode", method = RequestMethod.GET)
    @ApiOperation(value="구글 회원 가입 수행", notes="구글 회원 가입을 수행하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name = "code", value = "토큰", required = true, dataType = "String")
    })
    @ResponseBody
    public ResponseEntity<?> receiveGoogleCode(@RequestParam String code) throws Exception {
        logger.info("receiveGoogleCode 탐"+ code);

        ResultDTO responseDTO = new ResultDTO();
        HashMap<String, Object> resultMap;
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);

        //구글 토큰 받기 시작
        resultMap = userService.receiveGoogleToken(map);

        resultMap.remove("AUTH");

        responseDTO.setResultCode(resultMap.get("resultCode").toString());
        responseDTO.setResultMsg(resultMap.get("resultMsg").toString());
        responseDTO.setData(resultMap);

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //필터 테스트
    @RequestMapping(value = "makeToken", method = RequestMethod.GET)
    @ApiOperation(value="필터 테스트", notes="필터 테스트")
    @ResponseBody
    public ResponseEntity<?> makeToken(@RequestBody HashMap param) throws Exception {
        HashMap result = new HashMap();
        result = userService.login(param);
        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //아이디 까먹었을때 EMAIL로 회원 조회
    @ApiOperation(value="아이디 찾기", notes="아이디 까먹었을때 EMAIL로 회원 조회하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="EMAIL", value = "유저 이메일", required = true, dataType = "String")})
    @RequestMapping(value = "findUserByEmail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> findUserByEmail(@RequestBody HashMap param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();
        result = userService.findUserByEmail(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());
        resultDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //회원가입 시 아이디 중복체크
    @ApiOperation(value="회원가입 시 아이디 중복체크", notes="회원가입 시 아이디 중복체크 조회하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String")})
    @RequestMapping(value = "CheckDuplicateUserByID", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> CheckDuplicateUserByID(@RequestBody HashMap param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();
        result = userService.findMemberByIDForJoin(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());
        resultDTO.setData(result);

        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //회원가입 시 이메일 중복체크
    @ApiOperation(value="회원가입 시 이메일 중복체크", notes="회원가입 시 이메일 중복체크 조회하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="EMAIL", value = "유저 이메일", required = true, dataType = "String")})
    @RequestMapping(value = "CheckDuplicateUserByEmail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> CheckDuplicateUserByEmail(@RequestBody HashMap param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();
        result = userService.findMemberByEmailForJoin(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());

        return new ResponseEntity<>(result, HttpStatus.OK) ;
    }

    //회원정보 수정
    @ApiOperation(value="회원정보 수정", notes="회원정보 수정하는 API")
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name = "PASSWORD", value = "비밀번호 (AUTH_TYPE = normal (일반회원가입)일 경우에만 필수)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "PASSWORD_MODIFY", value = "PASSWORD 변경하는 경우에만 필수(Y로 세팅)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "AUTH", value = "상태", required = false, dataType = "String"),
            @ApiImplicitParam(name = "NAME", value = "이름", required = false, dataType = "String"),
            @ApiImplicitParam(name = "SALARY", value = "월급", required = false, dataType = "int"),
            @ApiImplicitParam(name = "NICKNAME", value = "닉네임", required = false, dataType = "String"),
            @ApiImplicitParam(name = "WORKING_DAY_CNT", value = "근무일수", required = false, dataType = "int"),
            @ApiImplicitParam(name = "PHONE", value = "폰번호", required = false, dataType = "String"),
            @ApiImplicitParam(name = "EMAIL", value = "이메일", required = false, dataType = "String"),
            @ApiImplicitParam(name = "DAY", value = "근무요일 (ex:목,금,월,화,수)", required = false, dataType = "String"),
            @ApiImplicitParam(name = "회원 탈퇴 수행할 경우 WITHDRAWAL=Y 로 세팅", value = "회원 탈퇴", required = false, dataType = "String")})
    @RequestMapping(value = "updateMemberInfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> updateMemberInfo(@RequestBody HashMap param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();
        result = userService.updateMemberInfo(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());

        return new ResponseEntity<>(resultDTO, HttpStatus.OK) ;
    }

    //비밀번호 찾기
    @ApiOperation(value="일반 회원 비밀번호 찾기", notes="비밀번호 찾기 API (난수로 비밀번호 업데이트 후 회원정보에 등록된 이메일로 메일 발송)")
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            })
    @RequestMapping(value = "findNormalMemberPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> findNormalMemberPassword(@RequestBody HashMap param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();

        param.put("PASSWORD_FORGOT", "Y");
        result = userService.sendMail(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());

        return new ResponseEntity<>(resultDTO, HttpStatus.OK) ;
    }

    //이메일 변경
    @ApiOperation(value="회원 이메일 변경", notes="이메일 변경 API (난수로 EMAIL_AUTH_CODE 업데이트 후 메일 발송)")
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="EMAIL", value = "이메일", required = true, dataType = "String")})
    @RequestMapping(value = "modifyMemberEmail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> modifyMemberEmail(@RequestBody HashMap param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();

        param.put("EMAIL_MODIFY", "Y");
        result = userService.sendMail(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());

        return new ResponseEntity<>(resultDTO, HttpStatus.OK) ;
    }

    //이메일 변경 인증
    @ApiOperation(value="회원 이메일 변경 인증", notes="변경한 이메일 인증 API (발송된 EMAIL_AUTH_CODE 와 DB 비교 후 인증여부 업데이트)")
    @ApiImplicitParams({@ApiImplicitParam(name="ID", value = "유저 아이디", required = true, dataType = "String"),
            @ApiImplicitParam(name="AUTH_TYPE", value = "유저 타입", required = true, dataType = "String"),
            @ApiImplicitParam(name="EMAIL", value = "이메일", required = true, dataType = "String"),
            @ApiImplicitParam(name="EMAIL_AUTH_CODE", value = "이메일 인증코드", required = true, dataType = "String")
    })
    @RequestMapping(value = "authenticateEmailAuthCode", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> authenticateEmailAuthCode(@RequestBody HashMap<String, Object> param) throws Exception {
        ResultDTO resultDTO = new ResultDTO();
        HashMap result = new HashMap();

        param.put("EMAIL_AUTHENTICATE", "Y");
        result = userService.updateMemberInfo(param);

        resultDTO.setResultCode(result.get("resultCode").toString());
        resultDTO.setResultMsg(result.get("resultMsg").toString());

        return new ResponseEntity<>(resultDTO, HttpStatus.OK) ;
    }




}