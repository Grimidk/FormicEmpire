package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/** Styled checkbox renderer/editor for {@link Boolean} table columns. */
public final class UiTableBooleanStyles {
    private static final TableCellRenderer RENDERER = new BooleanCheckBoxRenderer();
    private static final TableCellEditor EDITOR = new BooleanCheckBoxEditor();

    private UiTableBooleanStyles() {
    }

    public static void applyBooleanColumn(JTable table, int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setCellRenderer(RENDERER);
        column.setCellEditor(EDITOR);
    }

    private static void styleCheckBox(JCheckBox box) {
        box.setText("");
        box.setHorizontalAlignment(SwingConstants.CENTER);
        box.setFocusable(false);
        box.setOpaque(true);
        AssetStyles.styleCheckBox(box);
    }

    private static void applyRowColors(JCheckBox box, JTable table, boolean isSelected) {
        if (isSelected) {
            box.setBackground(table.getSelectionBackground());
            box.setForeground(table.getSelectionForeground());
        } else {
            box.setBackground(table.getBackground());
            box.setForeground(table.getForeground());
        }
    }

    private static final class BooleanCheckBoxRenderer extends DefaultTableCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();

        BooleanCheckBoxRenderer() {
            styleCheckBox(checkBox);
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            applyRowColors(checkBox, table, isSelected);
            checkBox.setSelected(value instanceof Boolean selected && selected);
            return checkBox;
        }
    }

    private static final class BooleanCheckBoxEditor extends DefaultCellEditor {
        private final JCheckBox checkBox;

        BooleanCheckBoxEditor() {
            super(new JCheckBox());
            checkBox = (JCheckBox) getComponent();
            styleCheckBox(checkBox);
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            applyRowColors(checkBox, table, isSelected);
            checkBox.setSelected(value instanceof Boolean selected && selected);
            return checkBox;
        }
    }
}
