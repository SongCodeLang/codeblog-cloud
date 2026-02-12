package site.codeblog.file.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import site.codeblog.file.core.FileException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具类
 *
 * @author codeblog
 */
@Slf4j
public class FileUtils {

    private FileUtils() {
    }

    /**
     * 非法文件名字符正则
     */
    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * 文件大小解析正则
     */
    private static final Pattern FILE_SIZE_PATTERN = Pattern.compile("^([\\d.]+)\\s*(B|KB|MB|GB|TB)$", Pattern.CASE_INSENSITIVE);

    /**
     * 图片扩展名集合
     */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif");

    /**
     * 文档扩展名集合
     */
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "csv", "rtf", "odt", "ods");

    /**
     * 视频扩展名集合
     */
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v", "rmvb", "3gp");

    /**
     * 音频扩展名集合
     */
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "ape");

    // ==================== A. 文件名操作 ====================

    /**
     * 获取文件扩展名（不含点）
     *
     * @param fileName 文件名或路径
     * @return 扩展名，如 "jpg"；无扩展名返回空串
     */
    public static String getExtension(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        return StrUtil.nullToEmpty(FileUtil.extName(fileName));
    }

    /**
     * 获取文件扩展名（含点）
     *
     * @param fileName 文件名或路径
     * @return 扩展名，如 ".jpg"；无扩展名返回空串
     */
    public static String getExtensionWithDot(String fileName) {
        String ext = getExtension(fileName);
        return ext.isEmpty() ? "" : "." + ext;
    }

    /**
     * 获取不含扩展名的文件名
     *
     * @param fileName 文件名
     * @return 不含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        return FileUtil.mainName(fileName);
    }

    /**
     * 生成唯一文件名（UUID + 原始扩展名）
     *
     * @param originalFileName 原始文件名
     * @return 唯一文件名，如 "a1b2c3d4e5f6.jpg"
     */
    public static String generateUniqueFileName(String originalFileName) {
        String ext = getExtensionWithDot(originalFileName);
        return IdUtil.simpleUUID() + ext;
    }

    /**
     * 生成带前缀的唯一文件名
     *
     * @param prefix           文件名前缀
     * @param originalFileName 原始文件名
     * @return 唯一文件名，如 "avatar_a1b2c3d4e5f6.jpg"
     */
    public static String generateUniqueFileNameWithPrefix(String prefix, String originalFileName) {
        String ext = getExtensionWithDot(originalFileName);
        return prefix + "_" + IdUtil.simpleUUID() + ext;
    }

    /**
     * 清理文件名中的非法字符
     *
     * @param fileName 待清理的文件名
     * @return 清理后的安全文件名
     */
    public static String sanitizeFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        return INVALID_FILENAME_PATTERN.matcher(fileName.trim()).replaceAll("_");
    }

    /**
     * 验证文件名是否合法（不包含非法字符）
     *
     * @param fileName 文件名
     * @return 是否合法
     */
    public static boolean isValidFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return false;
        }
        return !INVALID_FILENAME_PATTERN.matcher(fileName).find();
    }

    // ==================== B. 文件大小操作 ====================

    /**
     * 将字节数格式化为人类可读的文件大小
     *
     * @param sizeInBytes 字节大小
     * @return 可读格式，如 "1.5 MB"
     */
    public static String formatFileSize(long sizeInBytes) {
        return FileUtil.readableFileSize(sizeInBytes);
    }

    /**
     * 解析可读文件大小字符串为字节数
     * <p>支持格式: "10B", "10KB", "1.5MB", "2GB", "1TB"</p>
     *
     * @param sizeStr 大小字符串
     * @return 字节数
     * @throws FileException 格式错误时抛出异常
     */
    public static long parseFileSize(String sizeStr) {
        if (StrUtil.isBlank(sizeStr)) {
            throw FileException.of("文件大小字符串不能为空");
        }
        Matcher matcher = FILE_SIZE_PATTERN.matcher(sizeStr.trim());
        if (!matcher.matches()) {
            throw FileException.of("无法解析文件大小: " + sizeStr);
        }
        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();
        long multiplier = switch (unit) {
            case "B" -> 1L;
            case "KB" -> 1024L;
            case "MB" -> 1024L * 1024;
            case "GB" -> 1024L * 1024 * 1024;
            case "TB" -> 1024L * 1024 * 1024 * 1024;
            default -> throw FileException.of("不支持的文件大小单位: " + unit);
        };
        return (long) (value * multiplier);
    }

    // ==================== C. 文件类型操作 ====================

    /**
     * 根据文件名获取 MIME 类型
     *
     * @param fileName 文件名或路径
     * @return MIME 类型，未知类型返回 "application/octet-stream"
     */
    public static String getMimeType(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "application/octet-stream";
        }
        String mimeType = FileUtil.getMimeType(fileName);
        return StrUtil.isBlank(mimeType) ? "application/octet-stream" : mimeType;
    }

    /**
     * 判断文件是否为图片类型
     *
     * @param fileName 文件名
     * @return 是否为图片
     */
    public static boolean isImage(String fileName) {
        return IMAGE_EXTENSIONS.contains(getExtension(fileName).toLowerCase());
    }

    /**
     * 判断文件是否为文档类型
     *
     * @param fileName 文件名
     * @return 是否为文档
     */
    public static boolean isDocument(String fileName) {
        return DOCUMENT_EXTENSIONS.contains(getExtension(fileName).toLowerCase());
    }

    /**
     * 判断文件是否为视频类型
     *
     * @param fileName 文件名
     * @return 是否为视频
     */
    public static boolean isVideo(String fileName) {
        return VIDEO_EXTENSIONS.contains(getExtension(fileName).toLowerCase());
    }

    /**
     * 判断文件是否为音频类型
     *
     * @param fileName 文件名
     * @return 是否为音频
     */
    public static boolean isAudio(String fileName) {
        return AUDIO_EXTENSIONS.contains(getExtension(fileName).toLowerCase());
    }

    /**
     * 验证文件扩展名是否在白名单中（大小写不敏感）
     *
     * @param fileName          文件名
     * @param allowedExtensions 允许的扩展名数组
     * @return 是否允许
     */
    public static boolean isAllowedExtension(String fileName, String[] allowedExtensions) {
        if (allowedExtensions == null || allowedExtensions.length == 0) {
            return true;
        }
        String ext = getExtension(fileName).toLowerCase();
        return Arrays.stream(allowedExtensions)
                .anyMatch(allowed -> allowed.equalsIgnoreCase(ext));
    }

    // ==================== D. 文件验证 ====================

    /**
     * 验证文件大小是否在限制范围内
     *
     * @param sizeInBytes    文件大小（字节）
     * @param maxSizeInBytes 最大限制（字节）
     * @throws FileException 超限时抛出异常
     */
    public static void validateFileSize(long sizeInBytes, long maxSizeInBytes) {
        if (sizeInBytes > maxSizeInBytes) {
            throw FileException.of("文件大小超出限制，最大允许 " + formatFileSize(maxSizeInBytes));
        }
    }

    /**
     * 验证文件扩展名是否合法
     *
     * @param fileName          文件名
     * @param allowedExtensions 允许的扩展名数组
     * @throws FileException 不合法时抛出异常
     */
    public static void validateFileExtension(String fileName, String[] allowedExtensions) {
        if (!isAllowedExtension(fileName, allowedExtensions)) {
            throw FileException.of("不支持的文件类型: " + getExtension(fileName)
                    + "，允许的类型: " + String.join(", ", allowedExtensions));
        }
    }

    /**
     * 验证上传文件不为空
     *
     * @param file 上传的文件
     * @throws FileException 文件为空时抛出异常
     */
    public static void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw FileException.of("文件不能为空");
        }
    }

    /**
     * 综合验证上传文件（非空 + 大小 + 扩展名）
     *
     * @param file              上传的文件
     * @param maxSizeInBytes    最大文件大小（字节）
     * @param allowedExtensions 允许的扩展名数组
     * @throws FileException 验证失败时抛出异常
     */
    public static void validateFile(MultipartFile file, long maxSizeInBytes, String[] allowedExtensions) {
        validateFileNotEmpty(file);
        validateFileSize(file.getSize(), maxSizeInBytes);
        validateFileExtension(file.getOriginalFilename(), allowedExtensions);
    }

    // ==================== E. 路径生成 ====================

    /**
     * 生成基于当前日期的目录路径
     *
     * @return 日期路径，如 "2026/02/12/"
     */
    public static String generateDateBasedPath() {
        return DateUtil.format(new Date(), "yyyy/MM/dd") + "/";
    }

    /**
     * 生成完整上传路径（基础目录 + 日期目录 + 唯一文件名）
     *
     * @param baseDir  基础目录
     * @param fileName 原始文件名
     * @return 完整上传路径
     */
    public static String generateUploadPath(String baseDir, String fileName) {
        String dir = baseDir.endsWith("/") || baseDir.endsWith("\\") ? baseDir : baseDir + "/";
        return dir + generateDateBasedPath() + generateUniqueFileName(fileName);
    }

    // ==================== F. 文件上传下载 ====================

    /**
     * 上传文件到指定路径
     *
     * @param file       上传的文件
     * @param targetPath 目标路径（完整路径含文件名）
     * @throws FileException 上传失败时抛出异常
     */
    public static void uploadFile(MultipartFile file, String targetPath) {
        try {
            File dest = new File(targetPath);
            // 自动创建父目录
            FileUtil.mkParentDirs(dest);
            file.transferTo(dest);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw FileException.of("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件到 HTTP 响应流
     *
     * @param filePath 文件路径
     * @param response HTTP 响应对象
     * @throws FileException 下载失败时抛出异常
     */
    public static void downloadFile(String filePath, HttpServletResponse response) {
        File file = new File(filePath);
        downloadFileWithName(filePath, file.getName(), response);
    }

    /**
     * 下载文件并自定义下载文件名
     *
     * @param filePath         文件路径
     * @param downloadFileName 下载时的文件名
     * @param response         HTTP 响应对象
     * @throws FileException 下载失败时抛出异常
     */
    public static void downloadFileWithName(String filePath, String downloadFileName, HttpServletResponse response) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw FileException.of("文件不存在: " + filePath);
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = response.getOutputStream()) {
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
            response.setContentLengthLong(file.length());
            // 写入响应流
            IoUtil.copy(bis, os);
            os.flush();
        } catch (FileException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw FileException.of("文件下载失败: " + e.getMessage());
        }
    }

    // ==================== G. 文件 I/O 操作 ====================

    /**
     * 写入字节数据到文件
     *
     * @param data     字节数据
     * @param filePath 文件路径
     * @throws FileException 写入失败时抛出异常
     */
    public static void writeBytes(byte[] data, String filePath) {
        try {
            FileUtil.writeBytes(data, filePath);
        } catch (Exception e) {
            log.error("写入文件失败", e);
            throw FileException.of("写入文件失败: " + e.getMessage());
        }
    }

    /**
     * 读取文件为字节数组
     *
     * @param filePath 文件路径
     * @return 文件字节内容
     * @throws FileException 读取失败时抛出异常
     */
    public static byte[] readBytes(String filePath) {
        try {
            return FileUtil.readBytes(filePath);
        } catch (Exception e) {
            log.error("读取文件失败", e);
            throw FileException.of("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 复制文件
     *
     * @param srcPath  源文件路径
     * @param destPath 目标路径
     * @throws FileException 复制失败时抛出异常
     */
    public static void copyFile(String srcPath, String destPath) {
        try {
            FileUtil.copy(srcPath, destPath, true);
        } catch (Exception e) {
            log.error("复制文件失败", e);
            throw FileException.of("复制文件失败: " + e.getMessage());
        }
    }

    /**
     * 安全删除文件（文件不存在时返回 true）
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        try {
            return FileUtil.del(filePath);
        } catch (Exception e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public static boolean fileExists(String filePath) {
        return FileUtil.exist(filePath);
    }
}
