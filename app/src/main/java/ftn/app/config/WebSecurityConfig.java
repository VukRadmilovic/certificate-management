package ftn.app.config;

import ftn.app.auth.RestAuthenticationEntryPoint;
import ftn.app.auth.TokenAuthenticationFilter;
import ftn.app.service.CustomOAuth2UserService;
import ftn.app.service.interfaces.IUserService;
import ftn.app.util.CustomOAuth2User;
import ftn.app.util.TokenUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

	private final IUserService userService;
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	private final TokenUtils tokenUtils;
	private final CustomOAuth2UserService oAuth2UserService;

	public WebSecurityConfig(IUserService userService, RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                             TokenUtils tokenUtils, CustomOAuth2UserService oAuth2UserService){
		this.tokenUtils = tokenUtils;
		this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
		this.userService = userService;
		this.oAuth2UserService = oAuth2UserService;
	}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

 	@Bean
 	public DaoAuthenticationProvider authenticationProvider() {
 	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
 	    authProvider.setUserDetailsService(userService);
 	    authProvider.setPasswordEncoder(passwordEncoder());
 	    return authProvider;
 	}
 

 	@Bean
 	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
 	    return authConfig.getAuthenticationManager();
 	}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint);
    	http.authorizeRequests()
				.antMatchers("/api/user/login").permitAll()
				.antMatchers("/oauth/token").permitAll()
				.antMatchers("/api/user/login/sendEmail").permitAll()
				.antMatchers("/api/user/login/sendMessage").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/register/wEmail").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/register/wMessage").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/register").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/passwordReset/sendEmail").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/passwordReset/sendMessage").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/passwordReset").permitAll()
				.antMatchers(HttpMethod.POST, "/api/user/loginWithGoogle").permitAll()
				.anyRequest().authenticated().and()
				.cors().and().oauth2Login().permitAll().and()
				.addFilterBefore(new TokenAuthenticationFilter(tokenUtils,  userService), BasicAuthenticationFilter.class);
		http.csrf().disable();
		http.headers().frameOptions().disable();

        http.authenticationProvider(authenticationProvider());

		http.oauth2Login().loginPage("/login").userInfoEndpoint().userService(oAuth2UserService)
				.and().successHandler(new AuthenticationSuccessHandler() {
					@Override
					public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
														Authentication authentication) throws IOException, ServletException {
						CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

						userService.processOAuthPostLogin(oauthUser.getEmail());

						response.sendRedirect("/login/sendEmail");
					}
				});
       
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
    	return (web) -> web.ignoring().antMatchers(HttpMethod.POST, "/auth/login","/socket/**")
    			.antMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
    			"/**/*.html", "/**/*.css", "/**/*.js","/socket/**");
    }

}
