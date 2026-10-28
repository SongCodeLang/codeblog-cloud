package site.codeblog.banner.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 启动 Banner 自动配置
 * <p>
 * 通过 {@code codeblog.banner.enabled=false} 关闭。
 *
 * @author codeblog
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "codeblog.banner", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BannerAutoConfiguration {

    @Bean
    public BannerPrinter bannerPrinter() {
        return new BannerPrinter();
    }

    /**
     * Banner 打印器
     */
    public static class BannerPrinter implements ApplicationRunner, EnvironmentAware {

        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void run(ApplicationArguments args) {
            String appName = environment.getProperty("spring.application.name", "unknown");
            String port = environment.getProperty("server.port", "8080");
            String profile = String.join(",", environment.getActiveProfiles());

            String banner = """

                    ____          _            ____ _                 _         _
                   / ___|   _ ___| |_ ___ _ __/ ___| | ___  ___ _ __ (_)_ __ __| |
                  | |  | | | / __| __/ _ \\ '__| |   | |/ _ \\/ _ \\ '_ \\| | '__/ _` |
                  | |__| |_| \\__ \\ ||  __/ |  | |___| |  __/  __/ | | | | | | (_| |
                   \\____\\__, |___/\\__\\___|_|   \\____|_|\\___|\\___|_| |_|_|_|  \\__,_|
                          |___/
                    """;
            System.out.println(banner);
            System.out.println("  :: CodeBlog Cloud ::  应用启动成功!");
            System.out.println("  应用名称: " + appName);
            System.out.println("  服务端口: " + port);
            System.out.println("  激活配置: " + (profile.isEmpty() ? "default" : profile));
            System.out.println();
        }
    }

}
