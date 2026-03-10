package icu.jogeen.fishbook.service;

import java.util.List;

/**
 * @Author jogeen
 * @Date 14:28 2020/6/24
 * @Description
 */
public interface BookScanner {
     String bookName();
     long getBookSize();
     long getTotalLines();
     List<String> getContentForPage(int page, int pageSize);
     /**
      * 从指定行开始搜索，返回第一个包含关键词的行号（0-based），未找到返回 -1
      */
     int searchNextLine(String keyword, int fromLine);
}
