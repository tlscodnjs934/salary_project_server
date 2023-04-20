package blankspace.blankspaceprj.service;

import blankspace.blankspaceprj.dao.MemberDAO;
import blankspace.blankspaceprj.exception.CustomException;
import blankspace.blankspaceprj.jwt.JwtTokenProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.ibatis.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.thymeleaf.util.MapUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class MemberServiceImpl {

    @Autowired
    MemberDAO memberDAO;

    @Autowired
    HttpSession httpSession;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    HttpServletResponse httpServletResponse;

    @Value("${kakao.rest.api.token.url}")
    String kakaoRestApiTokenUrl;

    @Value("${kakao.rest.api.key}")
    String kakaoRestApiKey;

    @Value("${kakao.rest.redirect.uri}")
    String kakaoRestRedirectUri;

    @Value("${kakao.rest.user.info.uri}")
    String kakaoRestUserInfoUri;

    @Value("${naver.rest.api.token.url}")
    String naverRestApiTokenUrl;

    @Value("${naver.rest.api.key}")
    String naverRestApiKey;

    @Value("${naver.client.secret}")
    String naverClientSecret;

    @Value("${jwt.secret.key}")
    String jwtSecretKey;


    private final Logger logger = LoggerFactory.getLogger(getClass());




    public ArrayList<HashMap<String, Object>> findAll(){
        logger.debug("****findAll start****");
        return memberDAO.findAll();
    }

    //회원가입 서비스
    public HashMap<String, Object> joinMember(HashMap<String, Object> param){
        logger.info("****joinMember start****  param : " + param.toString());

        if(!MapUtils.containsKey(param, "ID")){
            param.put("resultCode", "");
            param.put("resultMsg", "회원 ID가 입력되지 않았습니다.");
            //return param;
            throw new CustomException("1", "회원 ID가 입력되지 않았습니다.");
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            param.put("resultCode", "1");
            param.put("resultMsg", "인증타입이 입력되지 않았습니다.");
            return param;
        }

        //1.기존 회원 존재하는지 조회
        HashMap<String, Object> member;
        member = memberDAO.findMemberByID(param);

        if(!MapUtils.isEmpty(member)){
            logger.info("****이미 가입된 회원입니다. 회원 ID : " + member.get("ID"));
            param.put("resultCode", "1");
            param.put("resultMsg", "이미 가입된 회원입니다. 회원 ID :" + member.get("ID"));
            return param;
        }


        //2.일반회원가입일 경우만 비번 암호화
        if("normal".equals(param.get("AUTH_TYPE"))) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String encode = bCryptPasswordEncoder.encode((String) param.get("PASSWORD"));
            param.put("PASSWORD", encode);
        }

        //3.회원 등록
        int cnt = memberDAO.joinMember(param);
        
        //회원가입 성공
        if (cnt == 1){
            logger.debug("****joinMember 등록 완료****  param : " );

            param.put("resultCode", "0");
            param.put("resultMsg", "회원 등록 완료. 회원 ID :" + param.get("ID"));
            return param;
        }else {
            logger.debug("****회원가입 실패****  param : " + param.get("ID"));

            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 등록 실패. 회원 ID :" + param.get("ID"));
            return param;
        }


    }

    //카카오 토큰 받기
    public HashMap<String, Object> receiveKakaoToken(HashMap<String, Object> param){
        logger.debug("****receiveKakaoToken 카카오 토큰 받기 시작****  param : " + param.get("code"));
        HashMap receivedData = new HashMap();

        String kakaoTokenUrl = kakaoRestApiTokenUrl + "?client_id=" + kakaoRestApiKey + "&redirect_uri=" + kakaoRestRedirectUri + "&grant_type=authorization_code&code=" + param.get("code");

        String accessToken;
        String refreshToken;
        String nickname="";
        String email="";

        try {
            URL url = new URL(kakaoTokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            System.out.println("response body : " + result);
            logger.info("토큰 받은 response body : " + result);

            JsonElement element = JsonParser.parseString(result.toString());

            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("accessToken : " + accessToken);
            System.out.println("refreshToken : " + refreshToken);

            receivedData.put("accessToken", accessToken);
            receivedData.put("refreshToken", refreshToken);


            bufferedReader.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
            receivedData.put("resultCode", "잘못된 URL 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (IOException e) {
            e.printStackTrace();
            receivedData.put("resultCode", "IOException 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (Exception e) {
            e.printStackTrace();
            receivedData.put("resultCode", "Exception 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }

        //받은 토큰으로 카카오 사용자 정보 조회 호출
        String kakaoUserInfoUrl= kakaoRestUserInfoUri + "?client_id=" + kakaoRestApiKey + "&redirect_uri=" + kakaoRestRedirectUri + "&grant_type=authorization_code&code=" + param.get("code");

        try {
            URL url = new URL(kakaoUserInfoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            System.out.println("response body : " + result);
            logger.info("카카오 사용자 정보 받은 response body : " + result);

            JsonElement element = JsonParser.parseString(result.toString());
            JsonElement kakao_account = element.getAsJsonObject().get("kakao_account");
            JsonElement profile = kakao_account.getAsJsonObject().get("profile");

            //logger.info("kakao_account"+ (String) kakao_account.getAsString());
            //logger.info("kakao_account2"+kakao_account.getAsJsonObject().get("email").getAsString());
            //element = element.getAsJsonObject().get("nickname");

            if(null != profile.getAsJsonObject().get("nickName")) {
                nickname = profile.getAsJsonObject().get("nickName").getAsString();
                logger.info("엘레1" + nickname);
            }
            if(null != element.getAsJsonObject().get("kakao_account")) {
                email = kakao_account.getAsJsonObject().get("email").getAsString();
                logger.info("엘레2"+email);
            }

            System.out.println("nickname : " + nickname);
            System.out.println("email : " + email);

            receivedData.put("nickname", nickname);
            receivedData.put("email", email);


            bufferedReader.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
            receivedData.put("resultCode", "잘못된 URL 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (IOException e) {
            e.printStackTrace();
            receivedData.put("resultCode", "IOException 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (Exception e){
            e.printStackTrace();
            receivedData.put("resultCode", "Exception 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }

        logger.info("receivedData ++ " + receivedData);

        //회원 조회하여 존재 않을 시 회원가입 등록 진행
        receivedData.put("ID", receivedData.get("email"));
        receivedData.put("AUTH_TYPE", "kakao");
        HashMap<String, Object> member;
        member = memberDAO.findMemberByID(receivedData);

        logger.info("member + " + member);

        //회원 데이터 없을 경우 회원 등록
        if(MapUtils.isEmpty(member)){
            logger.info("카카오 회원가입 진행 : " + receivedData);
            //회원가입 진행
            receivedData.put("ID", receivedData.get("email"));
            receivedData.put("AUTH", refreshToken);

            member = joinMember(receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "카카오 회원가입 성공");
        }else{
            logger.info("카카오 기존 회원, 회원가입 미진행 : " + receivedData);

            //TODO auth 토큰 업데이트 해야?
            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "카카오 기존 회원, 회원가입 미진행");
        }

        return receivedData;


    }

    //네이버 토큰 받기
    public HashMap<String, Object> receiveNaverToken(HashMap<String, Object> param){
        logger.debug("****receiveNaverToken 네이버 토큰 받기 시작****  param : " + param.get("code"));
        HashMap receivedData = new HashMap();

        logger.info("naverClientSecret :"+naverClientSecret);

        String naverTokenUrl = naverRestApiTokenUrl + "?client_id=" + naverRestApiKey + "&client_secret=" + naverClientSecret + "&grant_type=authorization_code&code=" + param.get("code") + "&state=" + param.get("state");

        String accessToken;
        String refreshToken;
        String email="";

        try {
            URL url = new URL(naverTokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            System.out.println("response body : " + result);
            logger.info("토큰 받은 response body : " + result);

            JsonElement element = JsonParser.parseString(result.toString());

            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("accessToken : " + accessToken);
            System.out.println("refreshToken : " + refreshToken);

            receivedData.put("accessToken", accessToken);
            receivedData.put("refreshToken", refreshToken);


            bufferedReader.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
            logger.info(e.getMessage());
            receivedData.put("resultCode", "잘못된 URL 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            receivedData.put("resultCode", "IOException 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            receivedData.put("resultCode", "Exception 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }

        //받은 토큰으로 네이버 사용자 정보 조회 호출
        String naverUserInfoUrl= "https://openapi.naver.com/v1/nid/me";

        try {
            URL url = new URL(naverUserInfoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            System.out.println("response body : " + result);
            logger.info("네이버 사용자 정보 받은 response body : " + result);

            JsonElement element = JsonParser.parseString(result.toString());
            JsonElement response = element.getAsJsonObject().get("response");

            //logger.info("kakao_account"+ (String) kakao_account.getAsString());
            //logger.info("kakao_account2"+kakao_account.getAsJsonObject().get("email").getAsString());
            //element = element.getAsJsonObject().get("nickname");

            if(null != response.getAsJsonObject().get("email")) {
                email = response.getAsJsonObject().get("email").getAsString();
                logger.info("email :" + email);
            }

            receivedData.put("email", email);

            bufferedReader.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
            receivedData.put("resultCode", "잘못된 URL 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (IOException e) {
            e.printStackTrace();
            receivedData.put("resultCode", "IOException 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }
        catch (Exception e){
            e.printStackTrace();
            receivedData.put("resultCode", "Exception 오류");
            receivedData.put("resultMsg", e.getMessage());

            return receivedData;
        }

        logger.info("receivedData ++ " + receivedData);

        //회원 조회하여 존재 않을 시 회원가입 등록 진행
        receivedData.put("AUTH_TYPE", "naver");
        receivedData.put("ID", receivedData.get("email"));

        HashMap<String, Object> member;
        member = memberDAO.findMemberByID(receivedData);

        logger.info("member + " + member);

        //회원 데이터 없을 경우 회원 등록
        if(MapUtils.isEmpty(member)){
            logger.info("네이버 회원가입 진행 : " + receivedData);
            //회원가입 진행
            receivedData.put("ID", receivedData.get("email"));
            receivedData.put("AUTH", refreshToken);

            member = joinMember(receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "네이버 회원가입 성공");
        }else{
            logger.info("네이버 기존 회원, 회원가입 미진행 : " + receivedData);

            //TODO auth 토큰 업데이트 해야?
            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "네이버 기존 회원, 회원가입 미진행");

            //로그인 호출
        }



        return receivedData;


    }

    public HashMap login(HashMap param){
        logger.debug("*********login 로그인 시작****************" + param);
        HashMap result = new HashMap();
        
        //TODO 일반 로그인 시에만 암호저장하므로 복호화해서 비교 필요
        if("normal".equals(param.get("AUTH_TYPE"))) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

            HashMap<String, Object> member;
            member = memberDAO.findMemberByID(param);

            //일반 로그인 시 기존 입력한 패스워드 전송 필요
            if (bCryptPasswordEncoder.matches((String) param.get("PASSWORD"), (String) member.get("PASSWORD"))) {
                httpSession.setAttribute("sessionID", member.get("ID"));
            } else {
                logger.info("*****nomal 로그인 실패 : 비밀번호 불일치*****" + param);
                result.put("resultCode", "-1");
                result.put("resultMsg", "normal 로그인 실패 : 비밀번호 불일치");

                return result;

            }

        }

        //httpSession = httpServletRequest.getSession();
        //httpSession.setAttribute("sessionID","AUTH");

        //일반 로그인 or 토큰 만료 시.....새 토큰 재발급 필요
        if(param.get("AUTH") == null){
            param.put("AUTH", JwtTokenProvider.getRandomToken());
        }

        String token = Jwts.builder()
                .setSubject((String) param.get("AUTH"))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .compact();

        logger.info("token : " + token);

        //TODO 로그인 시 MAp 에 회원정보와 access token 가도록
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setHeader("Bearar", (String) param.get("access_token"));
        httpServletResponse.setCharacterEncoding("UTF-8");



        return result;
    }
}