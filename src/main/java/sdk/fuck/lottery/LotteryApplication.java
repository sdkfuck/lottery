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

@SpringBootApplication
public class LotteryApplication extends SpringBootServletInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotteryApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(LotteryApplication.class, args);
    try {
      System.setProperty("java.awt.headless", "false");
      Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    } catch (Exception ignore) {
    }
    LOGGER.info("(♥◠‿◠)ﾉﾞ  启动成功   ლ(´ڡ`ლ)ﾞ  \n启动时间：{}\n", LocalDateTime.now());
  }

  // 打 WAR 包的配置
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(LotteryApplication.class);
  }

}
