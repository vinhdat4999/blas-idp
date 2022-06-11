package com.blas.blasidp.jwt;

import com.blas.blascommon.core.model.AuthUser;
import com.blas.blasidp.service.impl.AuthUserServiceImpl;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthUserServiceImpl authUserServiceImpl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserServiceImpl.getAuthUserByUsername(username);
        if (authUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        String userRole = authUser.getRole().getRoleName();
        List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
        GrantedAuthority authority = new SimpleGrantedAuthority(userRole);
        grantList.add(authority);
        boolean userIsActive = authUser.isIsActive();
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        return new org.springframework.security.core.userdetails.User(authUser.getUsername(),
                authUser.getPassword(), userIsActive,
                accountNonExpired, credentialsNonExpired, accountNonExpired, grantList);
    }
}