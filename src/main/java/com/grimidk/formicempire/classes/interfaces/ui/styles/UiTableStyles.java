package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.grimidk.formicempire.classes.interfaces.ui.util.UiNumberFormat;

/** Shared JTable layout helpers — column sizing and dialog defaults. */
public final class UiTableStyles {
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_MIN = 48;
    private static final int DEFAULT_MAX = 320;

    private UiTableStyles() {
    }

    public static void applyDialogTable(JTable table) {
        if (table == null) {
            return;
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setForeground(AssetStyles.FONT_COLOR);
        table.setBackground(AssetStyles.BACKGROUND_COLOR);
        table.setGridColor(AssetStyles.BACKGROUND_SECONDARY);
        table.setSelectionBackground(AssetStyles.SELECTION_BACKGROUND);
        table.setSelectionForeground(AssetStyles.FONT_COLOR);
        styleHeader(table);
        applyDefaultNumberRenderers(table);
    }

    /** Fixed column widths; use {@link #layoutColumnsForViewport} after data updates. */
    public static void applyScrollableDialogTable(JTable table) {
        applyDialogTable(table);
        if (table != null) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }

    private static void applyDefaultNumberRenderers(JTable table) {
        DefaultTableCellRenderer renderer = createNumericCellRenderer();
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(Long.class, renderer);
        table.setDefaultRenderer(Float.class, renderer);
        table.setDefaultRenderer(Double.class, renderer);
        table.setDefaultRenderer(Short.class, renderer);
        table.setDefaultRenderer(Byte.class, renderer);
    }

    public static DefaultTableCellRenderer createNumericCellRenderer() {
        DefaultTableCellRenderer renderer = new TooltipCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Number number) {
                    setText(UiNumberFormat.format(number));
                } else {
                    super.setValue(value);
                }
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    public static DefaultTableCellRenderer createTextCellRenderer(int alignment) {
        DefaultTableCellRenderer renderer = new TooltipCellRenderer();
        renderer.setHorizontalAlignment(alignment);
        return renderer;
    }

    public static DefaultTableCellRenderer createHeaderRenderer(int horizontalAlignment) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(horizontalAlignment);
        renderer.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        renderer.setForeground(AssetStyles.FONT_COLOR_HEADER);
        renderer.setFont(AssetStyles.FONT_BOLD);
        renderer.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return renderer;
    }

    public static void applyColumnAlignment(JTable table, int columnIndex, int alignment) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        TableCellRenderer existing = column.getCellRenderer();
        if (existing instanceof DefaultTableCellRenderer textRenderer) {
            textRenderer.setHorizontalAlignment(alignment);
        } else if (existing == null) {
            column.setCellRenderer(createTextCellRenderer(alignment));
        }
        column.setHeaderRenderer(createHeaderRenderer(alignment));
    }

    public static void applyHeaderAlignment(JTable table, int columnIndex, int alignment) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setHeaderRenderer(createHeaderRenderer(alignment));
    }

    public static JScrollPane wrapScrollableTable(JTable table) {
        applyScrollableDialogTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    public static void relayoutTableInScrollPane(JScrollPane scrollPane, boolean[] growableColumns) {
        if (scrollPane == null) {
            return;
        }
        Component view = scrollPane.getViewport().getView();
        if (!(view instanceof JTable table)) {
            return;
        }
        int width = scrollPane.getViewport().getWidth();
        if (width <= 0) {
            width = AssetStyles.DEFAULT_DIALOG_SIZE.width - 56;
        }
        layoutColumnsForViewport(table, width, growableColumns);
    }

    public static void styleHeader(JTable table) {
        if (table.getTableHeader() == null) {
            return;
        }
        table.getTableHeader().setBackground(AssetStyles.BACKGROUND_SECONDARY);
        table.getTableHeader().setForeground(AssetStyles.FONT_COLOR);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(AssetStyles.FONT_BOLD);
    }

    public static void fitColumns(JTable table) {
        fitColumns(table, DEFAULT_PADDING, DEFAULT_MIN, DEFAULT_MAX);
    }

    public static void fitColumns(JTable table, int padding, int minWidth, int maxWidth) {
        if (table == null || table.getColumnCount() == 0) {
            return;
        }
        for (int col = 0; col < table.getColumnCount(); col++) {
            fitColumn(table, col, minWidth, maxWidth, padding);
        }
    }

    public static void fitColumn(JTable table, int columnIndex, int minWidth, int maxWidth) {
        fitColumn(table, columnIndex, minWidth, maxWidth, DEFAULT_PADDING);
    }

    public static void fitColumn(JTable table, int columnIndex, int minWidth, int maxWidth, int padding) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        int width = measureColumnContentWidth(table, columnIndex, padding, minWidth, maxWidth);
        applyColumnWidth(table, columnIndex, width, minWidth);
    }

    /**
     * Sizes columns from header + cell content, then expands growable columns to fill the viewport.
     */
    public static void layoutColumnsForViewport(JTable table, int viewportWidth, boolean[] growableColumns) {
        if (table == null || table.getColumnCount() == 0 || viewportWidth <= 0) {
            return;
        }
        int columnCount = table.getColumnCount();
        int[] widths = new int[columnCount];
        int total = 0;
        for (int col = 0; col < columnCount; col++) {
            int min = col == 0 && table.getColumnClass(col) == javax.swing.Icon.class ? 40 : 56;
            int max = col == columnCount - 1 ? 720 : 280;
            widths[col] = measureColumnContentWidth(table, col, DEFAULT_PADDING, min, max);
            total += widths[col];
        }

        int target = Math.max(total, viewportWidth - 2);
        if (total < target) {
            int extra = target - total;
            int growWeight = 0;
            for (int col = 0; col < columnCount; col++) {
                if (growableColumns != null && col < growableColumns.length && growableColumns[col]) {
                    growWeight += Math.max(widths[col], 96);
                }
            }
            if (growWeight > 0) {
                int distributed = 0;
                for (int col = 0; col < columnCount; col++) {
                    if (growableColumns != null && col < growableColumns.length && growableColumns[col]) {
                        int weight = Math.max(widths[col], 96);
                        int add = (int) ((long) extra * weight / growWeight);
                        widths[col] += add;
                        distributed += add;
                    }
                }
                if (distributed < extra) {
                    widths[columnCount - 1] += extra - distributed;
                }
            } else {
                int perColumn = extra / columnCount;
                for (int col = 0; col < columnCount; col++) {
                    widths[col] += perColumn;
                }
                widths[columnCount - 1] += extra - (perColumn * columnCount);
            }
        }

        for (int col = 0; col < columnCount; col++) {
            int min = col == 0 && table.getColumnClass(col) == javax.swing.Icon.class ? 40 : 56;
            applyColumnWidth(table, col, widths[col], min);
        }
    }

    private static int measureColumnContentWidth(JTable table, int columnIndex, int padding, int minWidth, int maxWidth) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        int width = padding;
        int alignment = resolveColumnAlignment(table, columnIndex);
        if (column.getHeaderRenderer() == null) {
            column.setHeaderRenderer(createHeaderRenderer(alignment));
        }

        if (table.getTableHeader() != null) {
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component header = headerRenderer.getTableCellRendererComponent(
                    table, column.getHeaderValue(), false, false, -1, columnIndex);
            width = Math.max(width, header.getPreferredSize().width + padding);
        }

        for (int row = 0; row < table.getRowCount(); row++) {
            Object value = table.getValueAt(row, columnIndex);
            TableCellRenderer renderer = table.getCellRenderer(row, columnIndex);
            Component cell = renderer.getTableCellRendererComponent(
                    table, value, false, false, row, columnIndex);
            width = Math.max(width, cell.getPreferredSize().width + padding);
        }

        return Math.max(minWidth, Math.min(maxWidth, width));
    }

    private static void applyColumnWidth(JTable table, int columnIndex, int width, int minWidth) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setMinWidth(Math.min(width, minWidth));
        column.setPreferredWidth(width);
        column.setWidth(width);
        column.setMaxWidth(Integer.MAX_VALUE);
    }

    private static int resolveColumnAlignment(JTable table, int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        TableCellRenderer renderer = column.getCellRenderer();
        if (renderer instanceof DefaultTableCellRenderer textRenderer) {
            return textRenderer.getHorizontalAlignment();
        }
        Class<?> columnClass = table.getColumnClass(columnIndex);
        if (Number.class.isAssignableFrom(columnClass)
                || columnClass == Integer.class
                || columnClass == Long.class
                || columnClass == Float.class
                || columnClass == Double.class) {
            return SwingConstants.RIGHT;
        }
        if (columnClass == javax.swing.Icon.class) {
            return SwingConstants.CENTER;
        }
        return SwingConstants.LEFT;
    }

    /** Anchors a popup below a table cell — used by row action menus. */
    public static void showCellPopupMenu(JPopupMenu menu, JTable table, int row, int column) {
        if (menu == null || table == null || row < 0 || column < 0 || !table.isShowing()) {
            return;
        }
        Rectangle rect = table.getCellRect(row, column, true);
        menu.show(table, rect.x, rect.y + rect.height);
    }

    /** Shows a popup on a component when no table anchor is available. */
    public static void showComponentPopupMenu(JPopupMenu menu, Component invoker) {
        if (menu == null || invoker == null || !invoker.isShowing()) {
            return;
        }
        menu.show(invoker, Math.max(0, invoker.getWidth() / 2), Math.max(0, invoker.getHeight() / 2));
    }

  /** Shows full cell text on hover when the column is too narrow. */
    public static class TooltipCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            updateTruncationTooltip(table, column);
            return this;
        }

        protected void updateTruncationTooltip(JTable table, int column) {
            String text = getText();
            if (text == null || text.isEmpty()) {
                setToolTipText(null);
                return;
            }
            int columnWidth = table.getColumnModel().getColumn(column).getWidth();
            FontMetrics metrics = getFontMetrics(getFont());
            int available = columnWidth - getInsets().left - getInsets().right - 4;
            if (available > 0 && metrics.stringWidth(text) > available) {
                setToolTipText(text);
            } else {
                setToolTipText(null);
            }
        }
    }
}
