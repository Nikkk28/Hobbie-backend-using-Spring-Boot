package backend.hobbiebackend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;


@Configuration
public class HobbieConfigurationBeans {
    @Bean
    public PasswordEncoder createPasswordEncoder() {
        return new Pbkdf2PasswordEncoder("mySecretKey", 185000, 256, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    }

    @Bean
    public ModelMapper createModelMapper() {
        return new ModelMapper();
    }
}