package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleManagementFarmerLayerVisibleTest {

    @Test
    void workerAndQueenTabsShowFarmerAndLayerWhenStarterUnlocked() throws Exception {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.getStarterService().initializeDynasty(dynasty);
        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);

        assertTrue(colony.hasUpgrade(GameUnlocks.ROLE_FARMER));
        assertTrue(colony.hasUpgrade(GameUnlocks.ROLE_LAYER));

        List<String> workerLabels = new ArrayList<>();
        List<String> queenLabels = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            RoleManagementDialog dialog = new RoleManagementDialog(null, colony, null);
            JTabbedPane tabs = findTabbedPane(dialog);
            JPanel worker = (JPanel) tabs.getComponentAt(0);
            collectRoleLabels(worker, workerLabels);
            for (int i = 0; i < tabs.getTabCount(); i++) {
                if (tabs.getTitleAt(i).equals(GameConstants.TYPE_QUEEN.getName())) {
                    collectRoleLabels((JPanel) tabs.getComponentAt(i), queenLabels);
                }
            }
            dialog.dispose();
        });

        assertTrue(workerLabels.stream().anyMatch(s -> s.contains(GameConstants.ROLE_FARMER.getName())),
                "Worker tab labels=" + workerLabels);
        assertTrue(queenLabels.stream().anyMatch(s -> s.contains(GameConstants.ROLE_LAYER.getName())),
                "Queen tab labels=" + queenLabels);
    }

    private static JTabbedPane findTabbedPane(Component root) {
        if (root instanceof JTabbedPane tabs) {
            return tabs;
        }
        if (root instanceof java.awt.Container c) {
            for (Component child : c.getComponents()) {
                JTabbedPane found = findTabbedPane(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void collectRoleLabels(Component root, List<String> out) {
        if (root instanceof JLabel label && label.getText() != null && label.getText().endsWith(":")) {
            out.add(label.getText());
        }
        if (root instanceof java.awt.Container c) {
            for (Component child : c.getComponents()) {
                collectRoleLabels(child, out);
            }
        }
    }
}
