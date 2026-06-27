package com.ecommerce.product.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.Result;
import com.ecommerce.entity.Announcement;
import com.ecommerce.entity.Banner;
import com.ecommerce.mapper.AnnouncementMapper;
import com.ecommerce.mapper.BannerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Tag(name = "Admin - Site Management")
@RestController
@RequestMapping("/api/admin/site")
@RequiredArgsConstructor
public class AdminSiteController {

    private final BannerMapper bannerMapper;
    private final AnnouncementMapper announcementMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ==================== Banner ====================

    @Operation(summary = "Banner list")
    @GetMapping("/banners")
    public Result<?> banners() {
        return Result.success(bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSortOrder)));
    }

    @Operation(summary = "Add banner")
    @PostMapping("/banner")
    @PreAuthorize("hasAuthority('content:manage')")
    public Result<?> addBanner(@RequestBody Banner banner) {
        if (banner.getTitle() == null || banner.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }
        if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
            return Result.error(400, "图片URL不能为空");
        }
        banner.setId(null);
        banner.setCreateTime(null);
        banner.setUpdateTime(null);
        banner.setDeleted(null);
        try {
            bannerMapper.insert(banner);
        } catch (Exception e) {
            return Result.error("保存轮播图失败，请检查参数");
        }
        return Result.success();
    }

    @Operation(summary = "Update banner")
    @PutMapping("/banner/{id}")
    @PreAuthorize("hasAuthority('content:manage')")
    public Result<?> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        if (banner.getTitle() != null && banner.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }
        banner.setId(id);
        banner.setCreateTime(null);
        banner.setUpdateTime(null);
        banner.setDeleted(null);
        try {
            bannerMapper.updateById(banner);
        } catch (Exception e) {
            return Result.error("更新轮播图失败，请检查参数");
        }
        return Result.success();
    }

    @Operation(summary = "Delete banner")
    @DeleteMapping("/banner/{id}")
    @PreAuthorize("hasAuthority('content:manage')")
    public Result<?> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }

    // ==================== Announcement ====================

    @Operation(summary = "Announcement list")
    @GetMapping("/announcements")
    public Result<?> announcements() {
        return Result.success(announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime)));
    }

    @Operation(summary = "Add announcement")
    @PostMapping("/announcement")
    @PreAuthorize("hasAuthority('content:manage')")
    public Result<?> addAnnouncement(@RequestBody Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }
        announcement.setId(null);
        announcement.setCreateTime(null);
        announcement.setUpdateTime(null);
        announcement.setDeleted(null);
        try {
            announcementMapper.insert(announcement);
        } catch (Exception e) {
            return Result.error("保存公告失败，请检查参数");
        }
        return Result.success();
    }

    @Operation(summary = "Update announcement")
    @PutMapping("/announcement/{id}")
    @PreAuthorize("hasAuthority('content:manage')")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        if (announcement.getTitle() != null && announcement.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }
        announcement.setId(id);
        announcement.setCreateTime(null);
        announcement.setUpdateTime(null);
        announcement.setDeleted(null);
        try {
            announcementMapper.updateById(announcement);
        } catch (Exception e) {
            return Result.error("更新公告失败，请检查参数");
        }
        return Result.success();
    }

    @Operation(summary = "Delete announcement")
    @DeleteMapping("/announcement/{id}")
    @PreAuthorize("hasAuthority('content:manage')")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }

    // ==================== Upload ====================

    @Operation(summary = "Upload file")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!uploadPath.isAbsolute()) {
            uploadPath = Paths.get(System.getProperty("user.dir")).resolve(uploadPath);
        }
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + ext;
        Path targetPath = uploadPath.resolve(filename);
        file.transferTo(targetPath.toFile());
        return Result.success("/uploads/" + filename);
    }
}