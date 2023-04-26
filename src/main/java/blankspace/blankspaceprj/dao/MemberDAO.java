package blankspace.blankspaceprj.dao;


import blankspace.blankspaceprj.dto.MemberVO;
import blankspace.blankspaceprj.jwt.Member;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Mapper
@Repository
public interface MemberDAO {

    //회원 목록 조회
    ArrayList<HashMap<String, Object>> findAll();

    //ID로 해당 회원 찾기
    HashMap<String, Object> findMemberByID(HashMap<String, Object> param) ;

    //EMAIL로 해당 회원 찾기
    HashMap<String, Object> findMemberByEmail(HashMap<String, Object> param) ;

    //필터 체크 시 ID로 회원 조회
    Optional<Member> findMemberByIDreturnVO(HashMap<String, Object> param);
    
    //회원 등록
    int joinMember(HashMap<String, Object> param);
}