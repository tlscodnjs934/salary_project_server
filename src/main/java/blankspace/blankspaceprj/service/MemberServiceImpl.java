package blankspace.blankspaceprj.service;

import blankspace.blankspaceprj.dao.MemberDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
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

    public ArrayList<HashMap<String, Object>> joinMember(){
        logger.debug("****joinMember start****");

        logger.debug("****joinMember end****");

        return memberDAO.findAll();
    }
}