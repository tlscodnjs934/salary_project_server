package blankspace.blankspaceprj.dao;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
@Repository
public interface MemberDAO {

    //회원 목록 조회
    ArrayList<HashMap<String, Object>> findAll();

    //ID로 해당 회원 찾기
    HashMap<String, Object> findMemberByID(HashMap<String, Object> param);
    
    //회원 등록
    int joinMember(HashMap<String, Object> param);
}