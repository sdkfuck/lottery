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
 * 彩票应用程序的主类。
 */
@SpringBootApplication
public class LotteryApplication extends SpringBootServletInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotteryApplication.class);

  /**
   * 启动 Spring Boot 应用程序的主方法。
   *
   * @param args 命令行参数。
   */
  public static void main(String[] args) {
    SpringApplication.run(LotteryApplication.class, args);
    try {
      // 设置系统属性以允许 AWT 使用
      System.setProperty("java.awt.headless", "false");
      // 使用默认浏览器打开本地应用程序 URL
      Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    } catch (Exception ignore) {
      // 忽略所有异常
    }
    // 记录应用程序启动成功的日志信息
    LOGGER.info("(♥◠‿◠)ﾉﾞ  应用程序启动成功   ლ(´ڡ`ლ)ﾞ  \n启动时间: {}\n", LocalDateTime.now());
  }

  /**
   * 构建 WAR 包的配置。
   *
   * @param application SpringApplicationBuilder 实例。
   * @return 配置了应用程序的 SpringApplicationBuilder。
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(LotteryApplication.class);
  }
}
