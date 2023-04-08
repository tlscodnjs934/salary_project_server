package blankspace.blankspaceprj.service;

import blankspace.blankspaceprj.dao.MemberDAO;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.MapUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class MemberServiceImpl {

    @Autowired
    MemberDAO memberDAO;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ArrayList<HashMap<String, Object>> findAll(){
        logger.debug("****findAll start****");
        return memberDAO.findAll();
    }

    public HashMap<String, Object> joinMember(HashMap<String, Object> param){
        logger.debug("****joinMember start****  param : " + param.toString());
        //1.기존 회원 존재하는지 조회
        HashMap<String, Object> member;
        member = memberDAO.findMemberByID(param);

        if(!MapUtils.isEmpty(member)){
            logger.debug("****이미 가입된 회원입니다. 회원 ID : " + member.get("ID"));
            param.put("resultCode", "1");
            param.put("resultMsg", "이미 가입된 회원입니다. 회원 ID :" + member.get("ID"));
            return param;
        }


        //2.비번 암호화
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
            return param;
        }

    }

    //카카오 토큰 받기
    public HashMap<String, Object> receiveKakaoToken(HashMap<String, Object> param){
        logger.debug("****receiveKakaoToken 카카오 토큰 받기 시작****  param : " + param.get("code"));
        HashMap receivedData = new HashMap();

        String kakaoTokenUrl= "https://kauth.kakao.com/oauth/token?client_id=0a57a2699657f4e2b2e2b760f8e0dc51&redirect_uri=http://127.0.0.1:8080/api/member/receiveKakaoCode&grant_type=authorization_code&code="+param.get("code");

        String accessToken;
        String refreshToken;

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

            JsonElement element = JsonParser.parseString(result.toString());

            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("accessToken : " + accessToken);
            System.out.println("refreshToken : " + refreshToken);

            receivedData.put("accessToken", accessToken);
            receivedData.put("refreshToken", refreshToken);


            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //로그인 호출 필요

        return receivedData;




    }
}