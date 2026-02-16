package com.progbe.domain.user.type;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

// 사용자 권한
public enum Role {
    USER, ADMIN;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
