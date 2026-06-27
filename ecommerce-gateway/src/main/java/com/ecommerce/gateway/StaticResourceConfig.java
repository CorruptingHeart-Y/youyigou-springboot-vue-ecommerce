package com.ecommerce.gateway;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    @Value("${frontend.dist:frontend/dist}")
    private String frontendDist;

    /** Cached at startup — the directory containing this application (JAR or classes). */
    private final Path appHome = findAppHome();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path distDir = resolveDistDir();
        String distBase = toFileUrl(distDir);
        log.info("Serving frontend static resources from: {}", distBase);

        registry.addResourceHandler("/assets/**")
                .addResourceLocations(distBase + "assets/");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations(distBase);

        Path uploadsDir = findUploadsDir();
        String uploadsUrl = toFileUrl(uploadsDir);
        log.info("Serving /uploads/** from: {}", uploadsUrl);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsUrl);
    }

    // ── dist dir resolution ────────────────────────────────────────────

    private Path resolveDistDir() {
        Path p = Paths.get(frontendDist);
        if (p.isAbsolute() && Files.exists(p)) {
            return p.normalize();
        }
        // Try relative to app home first (handles JAR-in-target/ scenarios)
        Path fromAppHome = appHome.resolve(frontendDist);
        if (Files.exists(fromAppHome)) {
            return fromAppHome.normalize();
        }
        // Walk up from app home
        Path candidate = walkUpFind(appHome, frontendDist, 4);
        if (candidate != null) {
            return candidate;
        }
        // Last resort: working directory
        log.warn("frontend/dist not found near app home {}, trying working directory", appHome);
        return Paths.get(frontendDist).toAbsolutePath().normalize();
    }

    // ── uploads dir resolution ─────────────────────────────────────────

    private Path findUploadsDir() {
        // Try relative to working directory (simplest case: launched from project root)
        Path p = Paths.get("uploads").toAbsolutePath().normalize();
        if (Files.isDirectory(p)) {
            log.info("Uploads found at working directory: {}", p);
            return p;
        }

        // Walk up from app home — handles JAR in target/, gradle build/, etc.
        Path found = walkUpFind(appHome, "uploads", 5);
        if (found != null) {
            log.info("Uploads found relative to app home: {}", found);
            return found;
        }

        // Fallback
        p = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
        log.warn("Uploads not found, falling back to: {}", p);
        return p;
    }

    // ── helpers ────────────────────────────────────────────────────────

    /** Walk up from {@code start} up to {@code maxLevels} levels looking for {@code relative}. */
    private static Path walkUpFind(Path start, String relative, int maxLevels) {
        Path current = start.toAbsolutePath().normalize();
        for (int i = 0; i <= maxLevels; i++) {
            Path candidate = current.resolve(relative);
            if (Files.exists(candidate)) {
                return candidate.normalize();
            }
            Path parent = current.getParent();
            if (parent == null || parent.equals(current)) {
                break;
            }
            current = parent;
        }
        return null;
    }

    /** Locate the directory containing the application JAR / classes. */
    private static Path findAppHome() {
        try {
            ApplicationHome home = new ApplicationHome(GatewayApplication.class);
            Path dir = home.getDir().toPath();
            if (Files.isDirectory(dir)) {
                log.info("Application home: {}", dir);
                return dir;
            }
        } catch (Exception e) {
            log.warn("ApplicationHome failed: {}", e.getMessage());
        }
        return Paths.get(System.getProperty("user.dir"));
    }

    /**
     * Converts a Path to a file: URL with trailing slash.
     * Spring's UrlResource.createRelative() strips the last path segment
     * if the base URL doesn't end with '/', so directory URLs MUST end with '/'.
     */
    private static String toFileUrl(Path path) {
        String uri = path.toUri().toString();
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return uri;
    }
}
