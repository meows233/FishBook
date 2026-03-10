package icu.jogeen.fishbook.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.wm.ToolWindow;
import icu.jogeen.fishbook.service.BookScanner;
import icu.jogeen.fishbook.service.BookScannerBuilder;
import icu.jogeen.fishbook.service.PersistentState;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class ReadUI {

    private JPanel contentPanel;
    private JButton btnPre;
    private JButton btnNext;
    private JTextField tfPageNum;
    private JButton btnJump;
    private JTextPane txContetnt;
    private JLabel labTotalPages;
    private JLabel labBookName;
    private JTextField tfSearch;
    private JButton btnSearch;
    private JButton btnBack;

    private Long totalPage;
    private PersistentState persistentState = PersistentState.getInstance();

    // 搜索相关状态
    private int lastMatchedLine = -1;
    private int originalPageNum = -1;
    private String lastSearchKeyword = "";

    private void initBookScanner() {
        BookScanner scanner = BookScannerBuilder.getBookScaner();
        if (scanner == null) {
            scanner = BookScannerBuilder.getBookScaner();
            if (scanner == null) {
                MessageDialogBuilder.yesNo("操作结果", "请先配置图书路径").show();
                return;
            }
        }
        totalPage = scanner.getTotalLines() % persistentState.getPageSize() == 0
                ? scanner.getTotalLines() / persistentState.getPageSize()
                : scanner.getTotalLines() / persistentState.getPageSize() + 1;
        labTotalPages.setText("" + totalPage);
        labBookName.setText(scanner.bookName());
    }

    public ReadUI(Project project, ToolWindow toolWindow) {

        btnPre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turnPage(persistentState.getPageNum() - 1);
            }
        });
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turnPage(persistentState.getPageNum() + 1);
            }
        });
        btnJump.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = tfPageNum.getText();
                int pageNnum = Integer.parseInt(text);
                turnPage(pageNnum);
            }
        });
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doBack();
            }
        });
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doSearch();
                }
            }
        });
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { checkEmpty(); }
            @Override
            public void removeUpdate(DocumentEvent e) { checkEmpty(); }
            @Override
            public void changedUpdate(DocumentEvent e) { checkEmpty(); }
            private void checkEmpty() {
                if (tfSearch.getText().trim().isEmpty()) {
                    clearSearchState();
                }
            }
        });
        txContetnt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                int keyCode = e.getKeyCode();
                if (37 == keyCode) {
                    turnPage(persistentState.getPageNum() - 1);
                } else if (39 == keyCode) {
                    turnPage(persistentState.getPageNum() + 1);
                }
            }
        });

        // 内容区域不可编辑，隐藏光标
        txContetnt.setEditable(false);
        // 初始焦点放在内容区域，避免光标出现在输入框中
        SwingUtilities.invokeLater(() -> txContetnt.requestFocusInWindow());
    }

    /**
     * 增量搜索：每次只查找下一个匹配并立即跳转，
     * 关键词变化时从头开始，否则从上次匹配位置继续
     */
    private void doSearch() {
        String keyword = tfSearch.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        keyword = keyword.trim();

        initBookScanner();
        BookScanner scanner = BookScannerBuilder.getBookScaner();
        if (scanner == null) {
            return;
        }

        int startLine;
        if (!keyword.equals(lastSearchKeyword)) {
            // 仅首次搜索时记录原始位置，后续关键词变化不覆盖
            if (originalPageNum <= 0) {
                originalPageNum = persistentState.getPageNum();
            }
            lastSearchKeyword = keyword;
            lastMatchedLine = -1;
            startLine = (persistentState.getPageNum() - 1) * persistentState.getPageSize();
        } else {
            // 同一关键词，从上次匹配的下一行继续
            startLine = lastMatchedLine + 1;
        }

        int matchLine = scanner.searchNextLine(keyword, startLine);

        // 当前位置往后没找到，尝试从头搜索（循环）
        if (matchLine < 0 && startLine > 0) {
            matchLine = scanner.searchNextLine(keyword, 0);
            if (matchLine >= 0 && matchLine < startLine) {
                JOptionPane.showMessageDialog(contentPanel, "已到达末尾，从头开始搜索");
            } else {
                matchLine = -1;
            }
        }

        if (matchLine < 0) {
            JOptionPane.showMessageDialog(contentPanel, "未找到匹配内容: " + keyword);
            return;
        }

        lastMatchedLine = matchLine;
        int pageSize = persistentState.getPageSize();
        int targetPage = matchLine / pageSize + 1;
        turnPage(targetPage);
    }

    /**
     * 清除搜索状态
     */
    private void clearSearchState() {
        lastMatchedLine = -1;
        originalPageNum = -1;
        lastSearchKeyword = "";
    }

    /**
     * 回退到首次搜索前的位置
     */
    private void doBack() {
        if (originalPageNum > 0) {
            turnPage(originalPageNum);
            clearSearchState();
        }
    }

    public void turnPage(int i) {
        initBookScanner();
        if (i < 0 || i > totalPage) {
            return;
        }
        persistentState.setPageNum(i);
        List<String> contentForPage = BookScannerBuilder.getBookScaner()
                .getContentForPage(persistentState.getPageNum(), persistentState.getPageSize());
        txContetnt.setText("");
        StringBuilder sb = new StringBuilder();
        contentForPage.forEach(s -> {
            sb.append(s + "\r\n");
            txContetnt.setText(sb.toString());
        });
        tfPageNum.setText(i + "");
    }

    public JPanel getJcontent() {
        return contentPanel;
    }

}
