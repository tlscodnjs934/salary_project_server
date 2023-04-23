package blankspace.blankspaceprj.jwt;

import blankspace.blankspaceprj.dao.MemberDAO;
import blankspace.blankspaceprj.dto.MemberVO;
import blankspace.blankspaceprj.service.MemberServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserUserDetailsService implements UserDetailsService {

    //private final MemberRepository memberRepository;
    @Autowired
    MemberDAO memberDAO;

    public UserUserDetailsService() {
        //this.userService = userService;
        //this.userDetailsService = userDetailsService;

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return (UserDetails) memberDAO.findMemberByEmail(email);
    }
}