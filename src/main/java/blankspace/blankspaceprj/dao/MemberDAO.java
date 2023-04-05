package blankspace.blankspaceprj.dao;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
@Repository
public interface MemberDAO {

    ArrayList<HashMap<String, Object>> findAll();
    ArrayList<HashMap<String, Object>> joinMember();
}