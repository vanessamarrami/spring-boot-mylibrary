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

	/* Nel Db non ci devono essere i dati sensibili in chiaro. Quindi criptiamo i dati utilizzando
	 * BCryptPasswordEncoder. Il bean attiva il BCryptPasswordEncoder. */
	@Bean 
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	/* Importante: come dice il nome, è il servizio che ci permetterà di specificare quelli che sono
	 * l'utenti che potranno accedere alla nostra web app. 
	 * Rappresenta la fase di autenticazione dove inseriremo tutte le user ID e pass degli utenti.
	 * In questa FASE di configurazione della sicurezza i dati verranno inseriti in memoria, quindi
	 * verrano hard coded all'interno della classe, specificando manualmente i dettagli degli utenti.
	 * Creiamo due utenti: 
	 * - user: utente che può solo accedere al catalogo. Solo lettura.   
	 * - admin : può anche operare sulle risorse del catalogo. */
	@Bean 
	public InMemoryUserDetailsManager userDetailsService() {
	
		UserDetails admin = User.withUsername("admin")
				.password(passwordEncoder().encode("adminPass"))
				.roles("ADMIN")
				.build();
		
		return new InMemoryUserDetailsManager(admin);
	}
	
	/*Specifichiamo le autorizzazioni che gli utenti possono avere, quindi conseguentemente quali pagine, 
	 * quali elementi della nostra webapp. 
	 * Gli URL devono essere dal più specifico al più generico es: prima /library/update/* per admin e 
	 * poi "/library/*" per l'user.
	 * 	Da quelle più restrittivi a quelli meno restrittivi */
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
