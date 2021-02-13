package com.curiouslearning.referralnetwork.web.referral;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan(
    basePackages = {
        "com.curiouslearning.referralnetwork.web.referral.api",
        "com.curiouslearning.referralnetwork.spec.spring.api",
        "org.openapitools.configuration"
    })
public class ReferralApplication implements CommandLineRunner {

  @Override
  public void run(String... arg0) throws Exception {
    if (arg0.length > 0 && arg0[0].equals("exitcode")) {
      throw new ExitException();
    }
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(ReferralApplication.class, args);
  }

  static class ExitException extends RuntimeException implements ExitCodeGenerator {
    private static final long serialVersionUID = 1L;

    @Override
    public int getExitCode() {
      return 10;
    }

  }

  @Bean
  public WebMvcConfigurer webConfigurer() {
    return new WebMvcConfigurer() {
            /*@Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("Content-Type");
            }*/
    };
  }


}