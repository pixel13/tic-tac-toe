package com.github.pixel13.tictactoe.config;

import com.github.pixel13.tictactoe.security.Authenticator;
import com.github.pixel13.tictactoe.security.TokenAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private Authenticator authenticator;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilter(new TokenAuthorizationFilter(authenticationManager(), authenticator))
        .authorizeRequests()
        .antMatchers("/", "/graphql", "/subscriptions", "/playground", "/vendor/playground/**/*").permitAll()
        .anyRequest().authenticated()
        .and()
        .csrf().disable();
  }

}
