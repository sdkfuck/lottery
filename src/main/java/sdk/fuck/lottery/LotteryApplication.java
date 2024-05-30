package sdk.fuck.lottery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.awt.*;
import java.net.URI;
import java.time.LocalDateTime;

/**
 * Main class for the Lottery Application.
 */
@SpringBootApplication
public class LotteryApplication extends SpringBootServletInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotteryApplication.class);

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(LotteryApplication.class, args);
    try {
      System.setProperty("java.awt.headless", "false");
      Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    } catch (Exception ignore) {
    }
    LOGGER.info("(♥◠‿◠)ﾉﾞ  Application started successfully   ლ(´ڡ`ლ)ﾞ  \nStartup time: {}\n", LocalDateTime.now());
  }

  /**
   * Configuration for building WAR package.
   *
   * @param application SpringApplicationBuilder instance.
   * @return SpringApplicationBuilder configured for the application.
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(LotteryApplication.class);
  }
}

