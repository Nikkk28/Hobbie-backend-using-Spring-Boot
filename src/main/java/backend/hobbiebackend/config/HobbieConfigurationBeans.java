package backend.hobbiebackend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class HobbieConfigurationBeans {
    @Bean
    public PasswordEncoder createPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public ModelMapper createModelMapper() {
        return new ModelMapper();
    }
}