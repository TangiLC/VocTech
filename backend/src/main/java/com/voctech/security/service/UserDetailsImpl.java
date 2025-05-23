package com.voctech.security.service;

import com.voctech.model.User;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
//@RequiredArgsConstructor
@Builder
public class UserDetailsImpl implements UserDetails {

  private final Long id;
  private final String username;
  private final String password;
  private final String role;
  private String email;
  private final Collection<? extends GrantedAuthority> authorities;

  /**
   * Factory to build from our JPA User entity
   */
  public static UserDetailsImpl fromUser(User user) {
    // Convert the single String role into a GrantedAuthority
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
      "ROLE_" + user.getRole()
    );
    return UserDetailsImpl
      .builder()
      .id(user.getId())
      .username(user.getUsername())
      .password(user.getPassword())
      .role(user.getRole())
      .email(user.getEmail())
      .authorities(List.of(authority))
      .build();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public String getRole() {
    return role;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }
}
