package icu.jogeen.fishbook.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文件编码自动检测工具类
 * 检测顺序：BOM标记 → UTF-8验证 → 回退GBK
 */
public class CharsetDetector {

    private static final int DETECT_BUFFER_SIZE = 8192;

    /**
     * 自动检测文件编码
     * @param filePath 文件路径
     * @return 检测到的编码字符集
     */
    public static Charset detect(String filePath) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            byte[] buf = new byte[DETECT_BUFFER_SIZE];
            int len = bis.read(buf);
            if (len <= 0) {
                return StandardCharsets.UTF_8;
            }

            // 优先检测 BOM 标记
            Charset bomCharset = detectByBOM(buf, len);
            if (bomCharset != null) {
                return bomCharset;
            }

            // 尝试 UTF-8 验证
            if (isValidUTF8(buf, len)) {
                return StandardCharsets.UTF_8;
            }

            // 非 UTF-8，回退到 GBK
            return Charset.forName("GBK");

        } catch (IOException e) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * 通过 BOM（字节序标记）检测编码
     */
    private static Charset detectByBOM(byte[] buf, int len) {
        if (len >= 3 && buf[0] == (byte) 0xEF && buf[1] == (byte) 0xBB && buf[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        }
        if (len >= 2 && buf[0] == (byte) 0xFF && buf[1] == (byte) 0xFE) {
            return StandardCharsets.UTF_16LE;
        }
        if (len >= 2 && buf[0] == (byte) 0xFE && buf[1] == (byte) 0xFF) {
            return StandardCharsets.UTF_16BE;
        }
        return null;
    }

    /**
     * 验证字节序列是否为合法的 UTF-8 编码
     * UTF-8 编码规则:
     *   0xxxxxxx                              - 单字节 (ASCII)
     *   110xxxxx 10xxxxxx                     - 双字节
     *   1110xxxx 10xxxxxx 10xxxxxx            - 三字节 (中文常见)
     *   11110xxx 10xxxxxx 10xxxxxx 10xxxxxx   - 四字节
     */
    private static boolean isValidUTF8(byte[] buf, int len) {
        boolean hasMultiByte = false;
        int i = 0;
        while (i < len) {
            int b = buf[i] & 0xFF;
            int expectedFollowing;

            if (b <= 0x7F) {
                expectedFollowing = 0;
            } else if (b >= 0xC2 && b <= 0xDF) {
                expectedFollowing = 1;
                hasMultiByte = true;
            } else if (b >= 0xE0 && b <= 0xEF) {
                expectedFollowing = 2;
                hasMultiByte = true;
            } else if (b >= 0xF0 && b <= 0xF4) {
                expectedFollowing = 3;
                hasMultiByte = true;
            } else {
                return false;
            }

            // 验证后续字节是否为 10xxxxxx 格式
            for (int j = 1; j <= expectedFollowing; j++) {
                if (i + j >= len) {
                    // 缓冲区末尾截断，视为合法
                    return hasMultiByte;
                }
                if ((buf[i + j] & 0xC0) != 0x80) {
                    return false;
                }
            }
            i += expectedFollowing + 1;
        }
        // 纯 ASCII 也按 UTF-8 处理
        return true;
    }
}
