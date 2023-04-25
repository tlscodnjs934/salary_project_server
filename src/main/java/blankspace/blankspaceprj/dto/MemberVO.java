package blankspace.blankspaceprj.dto;

import org.apache.ibatis.mapping.FetchType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MemberVO
        //implements UserDetails
{
    private String id;
    private String auth;
    private String authType;
    private int SALARY;
    private String NICKNAME;
    private String EMAIL_AUTH_CODE;
    private String PASSWORD;
    private int WORKING_DAY_CNT;
    private String PHONE;
    private String EMAIL;
    private String DAY;
    private String AUTH_STATE;
    private String RGST_DATE;
    private String EMAIL_MODIFY_DATE;
    private String NAME;

    //@ElementCollection(fetch = FetchType.EAGER) //roles 컬렉션
    //@Builder.Default
    private List<String> roles = new ArrayList<>();
//    @Override   //사용자의 권한 목록 리턴
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return this.roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//    }

    //@Override
    public String getPassword() {
        return PASSWORD;
    }

    //@Override
    public String getUsername() {
        return NAME;
    }

    //@Override
    public boolean isAccountNonExpired() {
        return false;
    }

    //@Override
    public boolean isAccountNonLocked() {
        return false;
    }

    ////@Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    //@Override
    public boolean isEnabled() {
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public int getSALARY() {
        return SALARY;
    }

    public void setSALARY(int SALARY) {
        this.SALARY = SALARY;
    }

    public String getNICKNAME() {
        return NICKNAME;
    }

    public void setNICKNAME(String NICKNAME) {
        this.NICKNAME = NICKNAME;
    }

    public String getEMAIL_AUTH_CODE() {
        return EMAIL_AUTH_CODE;
    }

    public void setEMAIL_AUTH_CODE(String EMAIL_AUTH_CODE) {
        this.EMAIL_AUTH_CODE = EMAIL_AUTH_CODE;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public int getWORKING_DAY_CNT() {
        return WORKING_DAY_CNT;
    }

    public void setWORKING_DAY_CNT(int WORKING_DAY_CNT) {
        this.WORKING_DAY_CNT = WORKING_DAY_CNT;
    }

    public String getPHONE() {
        return PHONE;
    }

    public void setPHONE(String PHONE) {
        this.PHONE = PHONE;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public String getDAY() {
        return DAY;
    }

    public void setDAY(String DAY) {
        this.DAY = DAY;
    }

    public String getAUTH_STATE() {
        return AUTH_STATE;
    }

    public void setAUTH_STATE(String AUTH_STATE) {
        this.AUTH_STATE = AUTH_STATE;
    }

    public String getRGST_DATE() {
        return RGST_DATE;
    }

    public void setRGST_DATE(String RGST_DATE) {
        this.RGST_DATE = RGST_DATE;
    }

    public String getEMAIL_MODIFY_DATE() {
        return EMAIL_MODIFY_DATE;
    }

    public void setEMAIL_MODIFY_DATE(String EMAIL_MODIFY_DATE) {
        this.EMAIL_MODIFY_DATE = EMAIL_MODIFY_DATE;
    }
}
