package com.example.projekt.config;


import com.example.projekt.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // Umożliwia @PreAuthorize, @Secured
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    @Autowired // Wstrzyknięcie niestandardowego handlera
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        // ROLE_ADMIN dziedziczy po ROLE_MANAGER, który dziedziczy po ROLE_EMPLOYEE
        // Oznacza to, że ADMIN ma uprawnienia MANAGERA i EMPLOYEE, a MANAGER ma uprawnienia EMPLOYEE
        String hierarchy = "ROLE_ADMIN > ROLE_PROJECT_MANAGER \n ROLE_PROJECT_MANAGER > ROLE_EMPLOYEE";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    // Ten bean jest potrzebny, aby RoleHierarchy działało z @PreAuthorize i w Thymeleaf
    @Bean
    public DefaultWebSecurityExpressionHandler customWebSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/login", "/register", "/error", "/verify-2fa", "/perform_verify_2fa").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Dostęp tylko dla admina
                        .requestMatchers(HttpMethod.POST, "/projects").hasRole("PROJECT_MANAGER")
                        .requestMatchers("/projects/new").hasRole("PROJECT_MANAGER")
                        .requestMatchers("/projects/edit/**").hasRole("PROJECT_MANAGER")
                        .requestMatchers(HttpMethod.POST,"/projects/delete/**").hasRole("PROJECT_MANAGER")
                        .requestMatchers("/projects/tasks/new").hasAnyRole("PROJECT_MANAGER", "EMPLOYEE")
                        .requestMatchers("/projects/tasks/edit/**").hasAnyRole("PROJECT_MANAGER", "EMPLOYEE")
                        // Lepszym podejściem będzie użycie @PreAuthorize w kontrolerach dla bardziej granularnej kontroli
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/login", "/register", "/error").permitAll()
                        .anyRequest().authenticated() // Wszystkie inne żądania wymagają uwierzytelnienia
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        // .defaultSuccessUrl("/projects", true) // Usuń to, bo handler przejmuje kontrolę
                        .successHandler(customAuthenticationSuccessHandler) // Użyj niestandardowego success handler
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider()); // Dodanie naszego dostawcy autentykacji

        return http.build();
    }

}