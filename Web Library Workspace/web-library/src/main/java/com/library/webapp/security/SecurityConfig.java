package com.library.webapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

	@Bean 
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean 
	public InMemoryUserDetailsManager userDetailsService() {
	
		UserDetails admin = User.withUsername("admin")
				.password(passwordEncoder().encode("adminPass"))
				.roles("ADMIN")
				.build();
		
		return new InMemoryUserDetailsManager(admin);
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
		http
		 .authorizeHttpRequests((authz) -> authz
				  	.requestMatchers(mvc.pattern("/sfondo.jpg"), mvc.pattern("/css/**")).permitAll()
				  	.requestMatchers(mvc.pattern("/api/**")).permitAll() //accesso senza autenticazione per il RestController
				  	.requestMatchers(mvc.pattern("/")).permitAll()
				  	.requestMatchers(mvc.pattern("/login/**")).permitAll() //login è permessa a tutti
	                .requestMatchers(mvc.pattern("/library/update/**")).hasRole("ADMIN")
	                .requestMatchers(mvc.pattern("/library/insert/**")).hasRole("ADMIN")
	                .requestMatchers(mvc.pattern("/library/delete/**")).hasRole("ADMIN")
	                .requestMatchers(mvc.pattern("/library/author/update/**")).hasRole("ADMIN")
	                .requestMatchers(mvc.pattern("/library/**")).permitAll()
	                .requestMatchers(mvc.pattern("/library/author/**")).permitAll()
	               .anyRequest().authenticated()
	            )
		 .formLogin((formLogin) ->
			formLogin
				.usernameParameter("userId")
				.passwordParameter("password")
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.failureUrl("/login?error=true")
		)
		 .logout((logout) ->
			logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")
		);
		return http.build();
	}
	
	@Bean
	MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
		return new MvcRequestMatcher.Builder(introspector);
	}
}
