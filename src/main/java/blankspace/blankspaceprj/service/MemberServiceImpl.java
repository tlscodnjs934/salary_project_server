package blankspace.blankspaceprj.service;

import blankspace.blankspaceprj.dao.MemberDAO;
import blankspace.blankspaceprj.dto.MemberVO;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.thymeleaf.util.MapUtils;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


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
    private JavaMailSender javaMailSender ;



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
            return param;
        }

        if(!MapUtils.containsKey(param, "AUTH_TYPE")){
            param.put("resultCode", "1");
            param.put("resultMsg", "인증타입이 입력되지 않았습니다.");
            return param;
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
                param.put("resultCode", "1");
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
            logger.info("카카오 기존 회원, 회원가입 미진행, 로그인 호출 : " + receivedData);

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "카카오 기존 회원, 회원가입 미진행");

            login(receivedData);
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

            receivedData.put("resultCode", "0");
            receivedData.put("resultMsg", "네이버 기존 회원, 회원가입 미진행");

            login(receivedData);
        }



        return receivedData;


    }

    public HashMap login(HashMap param){
        logger.debug("*********login 로그인 시작****************" + param);
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

    //아이디 찾기 서비스
    public HashMap<String, Object> findUserByEmail(HashMap<String, Object> param){
        logger.info("***findUserByEmail start***" + param);

        HashMap<String, Object> result;

        result = memberDAO.findMemberByID(param);

        if(MapUtils.isEmpty(result)){
            result.put("resultCode", "-1");
            result.put("resultMsg", "해당 Email에 대한 ID가 존재하지 않습니다.");
        }else {
            result.put("resultCode", "0");
            result.put("resultMsg", "ID 찾기 성공");
        }
        return result;

    }

    //이메일 발송 서비스
    public HashMap<String, Object> sendMail(HashMap param) {
        HashMap<String, Object> result = new HashMap<>();
        //난수 생성
        String randomNum = createCode();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        if ("password".equals(param.get("type"))) {
            //비밀번호 찾기 시 유저 비번 업데이트 후 메일 발송;
        }else if (true){
            //메일 인증 시 메일 발송만
        }

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo((String) param.get("EMAIL")); // 메일 수신자 TODO : 사용자 계정 이메일로
            mimeMessageHelper.setSubject("asd"); // 메일 제목
            mimeMessageHelper.setText("끌린더에서 발송한 코드는 ", true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage);

            logger.info("메일 발송 Success");

            result.put("resultCode", "0");
            result.put("resultMsg", "메일 발송 성공");

            return result;

        } catch (Exception e) {
            logger.info("메일 발송  fail");

            result.put("resultCode", "-1");
            result.put("resultMsg", "메일 발송 실패 : " + e.getMessage());

            return result;

        }
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