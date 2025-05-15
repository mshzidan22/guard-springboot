package com.mshzidan.guard.security.token;

import com.mshzidan.guard.security.entites.OtpType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class OtpAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal; // recipient (email or phone)
    private  Object credentials; // OTP code
    private OtpType otpType;  // email or phone
    private  String ipAddress;


    public OtpAuthenticationToken(Object principal, Object credentials, OtpType otpType, String ipAddress) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.otpType = otpType;
        this.ipAddress = ipAddress;
        setAuthenticated(false);
    }
    // For successful authentication
    public OtpAuthenticationToken(Object principal,Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
