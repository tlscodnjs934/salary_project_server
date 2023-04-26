package blankspace.blankspaceprj.jwt;

import blankspace.blankspaceprj.dao.MemberDAO;
import blankspace.blankspaceprj.dto.MemberVO;
import blankspace.blankspaceprj.service.MemberServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class UserUserDetailsService implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    MemberDAO memberDAO;

    public UserUserDetailsService() {
        //this.userService = userService;
        //this.userDetailsService = userDetailsService;

    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        logger.info("loadUserByUsername : " + id);
        HashMap param = new HashMap();
        param.put("ID", id);
        logger.info("loadUserByUsername memberDAO.findMemberByIDreturnVO(id) : " + memberDAO.findMemberByIDreturnVO(param));
        try {
            return (UserDetails) memberDAO.findMemberByIDreturnVO(param)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
//        return memberRepository.findByEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}