package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JTable;
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

    private static void applyDefaultNumberRenderers(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
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
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(Long.class, renderer);
        table.setDefaultRenderer(Float.class, renderer);
        table.setDefaultRenderer(Double.class, renderer);
        table.setDefaultRenderer(Short.class, renderer);
        table.setDefaultRenderer(Byte.class, renderer);
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

    public static void fitColumn(JTable table, int columnIndex, int minWidth, int maxWidth, int padding) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        int width = padding;

        if (table.getTableHeader() != null) {
            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
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

        width = Math.max(minWidth, Math.min(maxWidth, width));
        column.setPreferredWidth(width);
        column.setMinWidth(Math.min(width, minWidth));
    }
}
