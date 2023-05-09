package blankspace.blankspaceprj.service;

import blankspace.blankspaceprj.dao.MemberDAO;
import blankspace.blankspaceprj.dto.MemberVO;
import blankspace.blankspaceprj.exception.CustomException;
import blankspace.blankspaceprj.jwt.JwtTokenProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.thymeleaf.util.MapUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MailService mailService;

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

    @Value("${google.rest.api.token.url}")
    String googleRestApiTokenUrl;

    @Value("${google.rest.client.id}")
    String googleRestClientId;

    @Value("${google.rest.client.secret}")
    String googleRestClientSecret;

    @Value("${google.rest.redirect.uri}")
    String googleRestRedirectUri;


    @Value("${jwt.secret.key}")
    String jwtSecretKey;


    private final Logger logger = LoggerFactory.getLogger(getClass());


    public ArrayList<HashMap<String, Object>> findAll(){
        logger.info("****findAll start****");
        return memberDAO.findAll();
    }

    //회원가입 서비스
    public HashMap<String, Object> joinMember(HashMap<String, Object> param){
        logger.info("****joinMember start****  param : " + param.toString());

        if(!MapUtils.containsKey(param, "ID")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 ID가 입력되지 않았습니다.");
            return param;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            param.put("resultCode", "-1");
            param.put("resultMsg", "인증타입이 입력되지 않았습니다.");
            return param;
        }

        if("normal".equals(param.get("AUTH_TYPE"))) {
            if (!MapUtils.containsKey(param, "PASSWORD")) {
                param.put("resultCode", "-1");
                param.put("resultMsg", "PASSWORD가 입력되지 않았습니다.");
                return param;
            }
        }
        
        if(MapUtils.containsKey(param, "EMAIL")){
            if(!isValidEmail((String) param.get("EMAIL"))){
                param.put("resultCode", "-1");
                param.put("resultMsg", "이메일 형식이 올바르지 않습니다.");
                return param;
            }
        }


        //TODO 앱에서 접근일 경우 앱용 secret key 확인 후 회원가입 필요
        String userAgent = httpServletRequest.getHeader("User-Agent").toUpperCase();

        if (userAgent.contains("ANDROID") || userAgent.contains("TABLET") || userAgent.contains("IPAD") || userAgent.contains("MOBILE") || userAgent.contains("IPHONE")) {
            logger.info("APP_JOIN_SECRET_KEY" + param.get("APP_JOIN_SECRET_KEY"));

            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if(bCryptPasswordEncoder.matches("저장해놓을 암호 키", (String)param.get("APP_JOIN_SECRET_KEY"))){
                logger.info("앱에서 회원가입 진행 시 key 인증 성공");
            }else{
                logger.info("****앱에서 회원가입 진행 시 key 인증 실패 ");
                param.put("resultCode", "-1");
                param.put("resultMsg", "앱에서 회원가입 진행 시 key 인증 실패" + param.get("APP_SECRET_KEY"));
                return param;
            }
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
            logger.info("****joinMember 등록 완료****  param : " );

            param.put("resultCode", "0");
            param.put("resultMsg", "회원 등록 완료. 회원 ID :" + param.get("ID"));
            return param;
        }else {
            logger.info("****회원가입 실패****  param : " + param.get("ID"));

            param.put("resultCode", "-1");
            param.put("resultMsg", "회원 등록 실패. 회원 ID :" + param.get("ID"));
            return param;
        }


    }

    //카카오 토큰 받기
    @Transactional
    public HashMap<String, Object> receiveKakaoToken(HashMap<String, Object> param){
        logger.info("****receiveKakaoToken 카카오 토큰 받기 시작****  param : " + param.get("code"));
        HashMap receivedData = new HashMap();

        String kakaoTokenUrl = kakaoRestApiTokenUrl + "?client_id=" + kakaoRestApiKey + "&redirect_uri=" + kakaoRestRedirectUri + "&grant_type=authorization_code&code=" + param.get("code");

        String accessToken;
        String refreshToken;
        String nickname="";
        String email="";

        try {
            URL url = new URL(kakaoTokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            logger.info("****카카오 responseCode : " + responseCode);
            logger.info("****카카오 responseMsg : " + conn.getResponseMessage());

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
        String kakaoUserInfoUrl= kakaoRestUserInfoUri + "?client_id=" + kakaoRestApiKey + "&redirect_uri=" + kakaoRestRedirectUri + "&grant_type=authorization_code&code=" + accessToken;

        try {
            URL url = new URL(kakaoUserInfoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            logger.info("****카카오 사용자 responseCode : " + responseCode);
            logger.info("****카카오 사용자 responseMsg : " + conn.getResponseMessage());

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
            receivedData.put("EMAIL", receivedData.get("email"));
            receivedData.put("AUTH", refreshToken);

            member = joinMember(receivedData);

            receivedData.put("resultCode", member.get("resultCode"));
            receivedData.put("resultMsg", "카카오 " + member.get("resultMsg"));
        }else{
            logger.info("카카오 기존 회원, 회원가입 미진행, 로그인 호출 : " + receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "카카오 기존 회원, 회원가입 미진행");

            login(receivedData);
        }

        return receivedData;


    }

    //네이버 토큰 받기
    @Transactional
    public HashMap<String, Object> receiveNaverToken(HashMap<String, Object> param){
        logger.info("****receiveNaverToken 네이버 토큰 받기 시작****  param : " + param.get("code"));
        HashMap receivedData = new HashMap();

        logger.info("naverClientSecret :"+naverClientSecret);

        String naverTokenUrl = naverRestApiTokenUrl + "?client_id=" + naverRestApiKey + "&client_secret=" + naverClientSecret + "&grant_type=authorization_code&code=" + param.get("code") + "&state=" + param.get("state");

        String accessToken;
        String refreshToken;
        String email="";

        try {
            URL url = new URL(naverTokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            logger.info("****네이버 responseCode : " + responseCode);
            logger.info("****네이버 responseMsg : " + conn.getResponseMessage());
            

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

            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();

            logger.info("****네이버 사용자 responseCode : " + responseCode);
            logger.info("****네이버 사용자 responseMsg : " + conn.getResponseMessage());

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
            receivedData.put("EMAIL", receivedData.get("email"));
            receivedData.put("AUTH", refreshToken);

            member = joinMember(receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "네이버 회원가입 성공");
        }else{
            logger.info("네이버 기존 회원, 회원가입 미진행 : " + receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "네이버 기존 회원, 회원가입 미진행");

            login(receivedData);
        }



        return receivedData;


    }

    //구글 토큰으로 사용자 정보 받는 서비스
    @Transactional
    public HashMap<String, Object> receiveGoogleToken(HashMap<String, Object> param){
        logger.info("****receiveGoogleToken 구글 토큰 받기 시작****  param : " + param.get("code"));
        HashMap receivedData = new HashMap();
        HttpURLConnection conn = null;

        String googleTokenUrl = googleRestApiTokenUrl + "?client_id=" + googleRestClientId + "&client_secret=" + googleRestClientSecret + "&grant_type=authorization_code&code=" + param.get("code") + "&redirect_uri=" + googleRestRedirectUri;
        //String googleTokenUrl = googleRestApiTokenUrl;

        String accessToken;
        String refreshToken;
        String email="";

        try {
            URL url = new URL(googleTokenUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(7000);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);
            //conn.setRequestProperty("Content-Length","0");
//            conn.setRequestProperty("client_id", googleRestClientId);
//            conn.setRequestProperty("client_secret", googleRestClientSecret);
//            conn.setRequestProperty("grant_type", "authorization_code");
//            conn.setRequestProperty("code", (String) param.get("code"));
//            conn.setRequestProperty("redirect_uri", googleRestRedirectUri);

            //리퀘스트 body 아무것도 안보내면 411 ERROR 발생... 빈값 json 전송으로 해결
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("{}", "");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            bw.write(jsonObject.toString());
            bw.flush();
            bw.close();

            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.


            int responseCode = conn.getResponseCode();
            logger.info("****구글  responseCode : " + responseCode);
            logger.info("****구글  responseMsg : " + conn.getResponseMessage());


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

            System.out.println("accessToken : " + accessToken);

            receivedData.put("accessToken", accessToken);


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
        }finally {
            conn.disconnect();
        }

        //받은 토큰으로 구글 사용자 정보 조회 호출
        //String googleUserInfoUrl= "https://oauth2.googleapis.com/tokeninfo";
        String googleUserInfoUrl= "https://www.googleapis.com/oauth2/v2/userinfo";

        try {
            URL url = new URL(googleUserInfoUrl);
            HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();

            conn2.setRequestMethod("GET");
            conn2.setConnectTimeout(7000);
            conn2.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn2.setRequestProperty("Authorization", "Bearer " + accessToken);
            // setDoOutput()은 OutputStream으로 POST 데이터를 넘겨 주겠다는 옵션이다.
            // POST 요청을 수행하려면 setDoOutput()을 true로 설정한다.
            conn2.setDoOutput(true);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("{}", "");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn2.getOutputStream()));
            bw.write(jsonObject.toString());
            bw.flush();
            bw.close();

            int responseCode = conn2.getResponseCode();
            logger.info("****구글 사용자 responseCode : " + responseCode);
            logger.info("****구글 사용자 responseMsg : " + conn2.getResponseMessage());

            // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            System.out.println("response body : " + result);
            logger.info("구글 사용자 정보 받은 response body : " + result);

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
        }finally {
            conn.disconnect();
        }

        logger.info("receivedData ++ " + receivedData);

        //회원 조회하여 존재 않을 시 회원가입 등록 진행
        receivedData.put("AUTH_TYPE", "google");
        receivedData.put("ID", receivedData.get("email"));

        HashMap<String, Object> member;
        member = memberDAO.findMemberByID(receivedData);

        logger.info("member + " + member);

        //회원 데이터 없을 경우 회원 등록
        if(MapUtils.isEmpty(member)){
            logger.info("구글 회원가입 진행 : " + receivedData);
            //회원가입 진행
            receivedData.put("ID", receivedData.get("email"));
            receivedData.put("AUTH", receivedData.get("token"));

            member = joinMember(receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "구글 회원가입 성공");
        }else{
            logger.info("구글 기존 회원, 회원가입 미진행 : " + receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "구글 기존 회원, 회원가입 미진행");

            login(receivedData);
        }



        return receivedData;


    }

    public HashMap login(HashMap param){
        logger.info("*********login 로그인 시작****************" + param);
        HashMap result = new HashMap();
        HashMap<String, Object> member = new HashMap();;
        
        //TODO 일반 로그인 시에만 암호저장하므로 복호화해서 비교 필요
        if("normal".equals(param.get("AUTH_TYPE"))) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

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

        //일반 로그인 or 토큰 만료 시.....새 토큰 재발급 필요
        //if(param.get("AUTH") == null){
            //param.put("AUTH", JwtTokenProvider.getRandomToken());
            String token = jwtTokenProvider.createToken(param.get("ID").toString());
            param.put("AUTH", token);

        //}

        logger.info("token : " + param.get("AUTH"));


        //TODO 로그인 시 MAp 에 회원정보와 access token 가도록
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setHeader("Bearar", (String) param.get("AUTH"));
        httpServletResponse.setCharacterEncoding("UTF-8");

        result.put("resultCode", "0");
        result.put("resultMsg", "로그인 성공");

        return result;
    }

    //EMAIL 입력을 통해 잃어버린 아이디 찾기 서비스
    public HashMap<String, Object> findUserByEmail(HashMap<String, Object> param){
        logger.info("***findUserByEmail start***" + param);
        HashMap<String, Object> result = new HashMap<String, Object>();

        if(!MapUtils.containsKey(param, "EMAIL")){
            logger.info("EMAIL이 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "ID가 입력되지 않았습니다.");

            return result;
        }

        HashMap<String, Object> hashMap = memberDAO.findMemberByEmail(param);

        if(MapUtils.isEmpty(hashMap)){
            result.put("resultCode", "-1");
            result.put("resultMsg", "해당 Email에 대한 ID가 존재하지 않습니다.");
        }else {
            result.put("resultCode", "0");
            result.put("resultMsg", "ID 찾기 성공");
        }
        return result;

    }

    //ID로 회원 조회
    public HashMap<String, Object> findMemberByIDForJoin(HashMap<String, Object> param){
        logger.info("***findMemberByIDForJoin start***" + param);

        HashMap<String, Object> result = new HashMap<>();
        
        if(!MapUtils.containsKey(param, "ID")){
            logger.info("ID가 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "ID가 입력되지 않았습니다.");

            return result;
        }

        HashMap<String, Object> hashMap = memberDAO.findMemberByID(param);

        if(MapUtils.isEmpty(hashMap)){
            result.put("resultCode", "0");
            result.put("resultMsg", "해당 ID로 회원가입이 가능합니다.");
        }else {
            result.put("resultCode", "-1");
            result.put("resultMsg", "해당 ID 는 이미 존재하여 가입할 수 없습니다.");
        }
        return result;

    }

    //EMAIL로 회원 조회
    public HashMap<String, Object> findMemberByEmailForJoin(HashMap<String, Object> param){
        logger.info("***findMemberByEmail start***" + param);

        HashMap<String, Object> result = new HashMap<>();

        if(!MapUtils.containsKey(param, "EMAIL")){
            logger.info("EMAIL이 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "EMAIL이 입력되지 않았습니다.");

            return result;
        }

        HashMap<String, Object> hashMap = memberDAO.findMemberByEmail(param);

        if(MapUtils.isEmpty(hashMap)){
            result.put("resultCode", "0");
            result.put("resultMsg", "해당 EMAIL로 회원가입이 가능합니다.");
        }else {
            result.put("resultCode", "-1");
            result.put("resultMsg", "해당 EMAIL은 이미 존재하여 가입할 수 없습니다.");
        }
        return result;

    }

    //회원정보 수정
    public HashMap<String, Object> updateMemberInfo(HashMap<String, Object> param) {
        logger.info("***********updateMemberInfo start ************ param : " + param);
        HashMap<String, Object> result = new HashMap<>();

        if(!MapUtils.containsKey(param, "ID")){
            logger.info("ID가 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "ID가 입력되지 않았습니다.");

            return result;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            logger.info("AUTH_TYPE이 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "AUTH_TYPE이 입력되지 않았습니다.");

            return result;
        }

        if(MapUtils.containsKey(param, "PASSWORD")){
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            param.put("PASSWORD", bCryptPasswordEncoder.encode((String) param.get("PASSWORD")));
        }

        int cnt = memberDAO.updateMember(param);

        if(cnt == 1){
            result.put("resultCode", "0");
            result.put("resultMsg", "회원정보 업데이트 성공");
        }else{
            result.put("resultCode", "-1");
            result.put("resultMsg", "회원정보 업데이트 실패");
        }

        return result;
    }

    //이메일 형식 검증
    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    //이메일 발송 서비스
    @Transactional
    public HashMap<String, Object> sendMail(HashMap<String, Object> param) {
        HashMap<String, Object> result = new HashMap<>();
        HashMap<String, Object> updateMemberMap = new HashMap<>();

        if(!MapUtils.containsKey(param, "ID")){
            logger.info("ID가 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "ID가 입력되지 않았습니다.");

            return result;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            logger.info("AUTH_TYPE이 입력되지 않았습니다. param : " + param);
            result.put("resultCode", "-1");
            result.put("resultMsg", "AUTH_TYPE이 입력되지 않았습니다.");

            return result;
        }

        //랜덤 문자열 생성
        String randomNum = createCode();

        //비밀번호 찾기
        if ("Y".equals(param.get("PASSWORD_FORGOT"))) {
            if(!MapUtils.containsKey(param, "ID")){
                logger.info("ID가 입력되지 않았습니다. param : " + param);
                result.put("resultCode", "-1");
                result.put("resultMsg", "ID가 입력되지 않았습니다.");

                return result;
            }

            //비밀번호 찾기 시 유저 비번 업데이트 후 메일 발송;
            HashMap<String, Object> member = memberDAO.findMemberByID(param);

            //TODO 회원 조회해서 등록된 이메일로 발송 필요
            updateMemberMap.put("ID", member.get("ID"));
            updateMemberMap.put("AUTH_TYPE", member.get("AUTH_TYPE"));
            updateMemberMap.put("PASSWORD_FORGOT", param.get("PASSWORD_FORGOT"));
            updateMemberMap.put("EMAIL", member.get("EMAIL"));
            updateMemberMap.put("EMAIL_AUTH_CODE", randomNum);
            
            //암호화된 난수 비밀번호 세팅
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            updateMemberMap.put("PASSWORD", bCryptPasswordEncoder.encode(randomNum));
            
            //회원 업데이트 호출
            int cnt = memberDAO.updateMember(updateMemberMap);

            if(cnt == 1){
                logger.info("비밀번호 변경 성공 : " + updateMemberMap);
            }else {
                logger.info("비밀번호 변경 실패 : " + updateMemberMap);
                result.put("resultCode", "-1");
                result.put("resultMsg", "비밀번호 변경 중 오류가 발생했습니다. 관리자에게 문의하시기 바랍니다.");

                return result;
            }
        }else if ("Y".equals(param.get("EMAIL_MODIFY"))){
            //이메일 변경 시
            if(!MapUtils.containsKey(param, "EMAIL")){
                logger.info("EMAIL이 입력되지 않았습니다. param : " + param);
                result.put("resultCode", "-1");
                result.put("resultMsg", "EMAIL이 입력되지 않았습니다.");

                return result;
            }

            //메일 인증 시
            updateMemberMap.put("ID", param.get("ID"));
            updateMemberMap.put("AUTH_TYPE", param.get("AUTH_TYPE"));
            updateMemberMap.put("EMAIL_MODIFY", param.get("EMAIL_MODIFY"));
            updateMemberMap.put("EMAIL", param.get("EMAIL"));
            updateMemberMap.put("EMAIL_AUTH_CODE", randomNum);

            //회원 업데이트 호출
            int cnt = memberDAO.updateMember(updateMemberMap);

            if(cnt == 1){
                logger.info("이메일 변경 성공 : " + updateMemberMap);
            }else {
                logger.info("이메일 변경 실패 : " + updateMemberMap);
                result.put("resultCode", "-1");
                result.put("resultMsg", "이메일 변경 중 오류가 발생했습니다. 관리자에게 문의하시기 바랍니다.");

                return result;
            }
        }

        boolean mailResult = mailService.mailSend(updateMemberMap);

        if(mailResult ==true) {
            result.put("resultCode", "0");
            result.put("resultMsg", "MemberServiceImpl sendMail 메일 발송 완료");
        }else {
            result.put("resultCode", "-1");
            result.put("resultMsg", "MemberServiceImpl sendMail 메일 발송 실패");
        }
        
        return result;
    }

    //메일 발송용 랜덤 숫자 생성
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }
}