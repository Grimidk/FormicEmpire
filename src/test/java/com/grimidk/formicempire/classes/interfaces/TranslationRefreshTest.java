package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.StatsDialog;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.UpgradeDialog;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.WorldGenVisualizerDialog;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.ColonyPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationRefreshTest {

    @AfterEach
    void restoreEnglishLanguage() {
        LanguageStrings.setLanguage("en");
    }

    @Test
    void colonyPanelRefreshesWarStatusAndTooltipsOnLanguageSwitch() throws Exception {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Solari", true, GameConstants.SPECIES_OMNI);
        Colony colony = createColonyFixture(world, dynasty);
        Engine engine = new Engine();
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            LanguageStrings.setLanguage("en");
            ColonyPanel panel = new ColonyPanel();
            panel.setEngine(engine);

            LanguageStrings.setLanguage("es");
            panel.refreshTranslations();

            List<JLabel> emptyLabelsBeforeBind = collectComponents(panel, JLabel.class);
            assertTrue(emptyLabelsBeforeBind.stream()
                    .anyMatch(l -> LanguageStrings.get(LanguageStrings.PANEL_NO_COLONY).equals(l.getText())));

            panel.updateDayData(colony);

            List<JLabel> labels = collectComponents(panel, JLabel.class);
            assertTrue(labels.stream().anyMatch(l -> "En paz".equals(l.getText())));
            assertFalse(labels.stream().anyMatch(l -> "At peace".equals(l.getText())));
            assertTrue(labels.stream().anyMatch(l ->
                    LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER_DESC).equals(l.getToolTipText())));

            LanguageStrings.setLanguage("fr");
            panel.refreshTranslations();

            labels = collectComponents(panel, JLabel.class);
            assertTrue(labels.stream().anyMatch(l -> "En paix".equals(l.getText())));

            dynasty.addDiplomaticModifierKey(99, GameConstants.DIPLO_MODIFIER_WAR.getNameKey());
            panel.updateDayData(colony);

            labels = collectComponents(panel, JLabel.class);
            assertTrue(labels.stream().anyMatch(l -> "En guerre".equals(l.getText())));
        });
    }

    @Test
    void statsDialogRefreshesTabsHeadersAndSeasonWeatherLabels() throws Exception {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Solari", true, GameConstants.SPECIES_OMNI);
        Colony colony = createColonyFixture(world, dynasty);
        Engine engine = new Engine();
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            LanguageStrings.setLanguage("en");
            StatsDialog dialog = new StatsDialog(null, colony, engine);
            try {
                JTabbedPane tabs = collectComponents(dialog, JTabbedPane.class).get(0);
                List<JTable> tables = collectComponents(dialog, JTable.class);
                JTable generalTable = tables.get(0);
                JTable dynastyTable = tables.get(1);
                Object originalGeneralCol2Renderer = generalTable.getColumnModel().getColumn(2).getCellRenderer();
                Object originalDynastyCol0Renderer = dynastyTable.getColumnModel().getColumn(0).getCellRenderer();

                LanguageStrings.setLanguage("es");
                dialog.refreshTranslations();

                JCheckBox dynastyToggle = collectComponents(dialog, JCheckBox.class).get(0);
                assertEquals(LanguageStrings.get(LanguageStrings.STATS_DYNASTY_MODE), dynastyToggle.getText());
                assertEquals(LanguageStrings.get(LanguageStrings.STATS_TAB_GENERAL), tabs.getTitleAt(0));
                assertEquals(LanguageStrings.get(LanguageStrings.STATS_TAB_DYNASTY), tabs.getTitleAt(1));
                assertEquals(LanguageStrings.get(LanguageStrings.STATS_TAB_MORTALITY), tabs.getTitleAt(7));

                assertEquals(LanguageStrings.get(LanguageStrings.COL_CATEGORY),
                        generalTable.getColumnModel().getColumn(0).getHeaderValue());
                assertEquals(LanguageStrings.get(LanguageStrings.COL_PROPERTY),
                        generalTable.getColumnModel().getColumn(1).getHeaderValue());
                assertEquals(LanguageStrings.get(LanguageStrings.COL_VALUE),
                        generalTable.getColumnModel().getColumn(2).getHeaderValue());
                assertEquals(LanguageStrings.get(LanguageStrings.COL_SCOPE),
                        dynastyTable.getColumnModel().getColumn(1).getHeaderValue());

                assertEquals(originalGeneralCol2Renderer, generalTable.getColumnModel().getColumn(2).getCellRenderer());
                assertEquals(originalDynastyCol0Renderer, dynastyTable.getColumnModel().getColumn(0).getCellRenderer());

                List<String> properties = new ArrayList<>();
                for (int r = 0; r < generalTable.getRowCount(); r++) {
                    Object prop = generalTable.getValueAt(r, 1);
                    if (prop != null) {
                        properties.add(prop.toString());
                    }
                }
                assertTrue(properties.contains(LanguageStrings.get(LanguageStrings.STAT_SEASON)));
                assertTrue(properties.contains(LanguageStrings.get(LanguageStrings.STAT_WEATHER)));
                assertFalse(properties.contains(LanguageStrings.get(LanguageStrings.SEASON_SPRING)));
                assertFalse(properties.contains(LanguageStrings.get(LanguageStrings.PANEL_WORLD)));
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void alertPanelRefreshesEmptyStateOnLanguageSwitch() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LanguageStrings.setLanguage("en");
            AlertPanel panel = new AlertPanel();
            panel.updateAlerts(List.of());

            LanguageStrings.setLanguage("fr");
            panel.refreshTranslations();

            List<JLabel> labels = collectComponents(panel, JLabel.class);
            assertTrue(labels.stream()
                    .anyMatch(l -> LanguageStrings.get(LanguageStrings.PANEL_NO_ALERTS).equals(l.getText())));
        });
    }

    @Test
    void worldGenVisualizerLocalizesAllCanonicalSteps() {
        LanguageStrings.setLanguage("es");
        String first = WorldGenVisualizerDialog.formatStepProgress(1, "Initialize World");
        String last = WorldGenVisualizerDialog.formatStepProgress(16, "Finalize World");

        assertNotNull(first);
        assertFalse(first.contains("Step 1/16: Initialize World"));
        assertTrue(first.contains(LanguageStrings.get(LanguageStrings.WORLDGEN_STEP_INIT)));
        assertFalse(last.contains("Step 16/16: Finalize World"));
        assertTrue(last.contains(LanguageStrings.get(LanguageStrings.WORLDGEN_STEP_FINALIZE)));
    }

    @Test
    void upgradeDialogRefreshesAssimilationCompletionInNonEnglishLocale() throws Exception {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Solari", true, GameConstants.SPECIES_FIRE);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
        dynasty.setCurrentAssimilation(GameUnlocks.ASSIMILATION_OMNI);
        Colony colony = createColonyFixture(world, dynasty);
        Engine engine = new Engine();
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            LanguageStrings.setLanguage("es");
            UpgradeDialog dialog = new UpgradeDialog(null, colony, engine);
            dialog.setModalityType(Dialog.ModalityType.MODELESS);
            try {
                dialog.showDialog(UpgradeDialog.TAB_ASSIMILATION);
                List<AbstractButton> cancelButtonsBefore = collectComponents(dialog, AbstractButton.class);
                assertTrue(cancelButtonsBefore.stream()
                        .anyMatch(b -> LanguageStrings.get(LanguageStrings.UI_CANCEL).equals(b.getText())));

                dynasty.setCurrentAssimilation(null);
                dialog.liveUpdate();

                List<JLabel> labelsAfter = collectComponents(dialog, JLabel.class);
                assertTrue(labelsAfter.stream()
                        .anyMatch(l -> LanguageStrings.get(LanguageStrings.ASSIMILATION_NO_GENOMES).equals(l.getText())));
            } finally {
                dialog.dispose();
            }
        });
    }

    private static Colony createColonyFixture(World world, Dynasty dynasty) {
        Colony colony = new Colony(1, "Prime", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        dynasty.setCapital(colony);
        ColonyStarterService.shared().initializeNewColony(colony);

        Hex hex = new Hex();
        hex.setQ(0);
        hex.setR(0);
        hex.setBiome(GameConstants.BIOME_PLAINS);
        hex.setColony(colony);
        world.setHexes(new ArrayList<>(List.of(hex)));
        world.changeActiveHex(hex);
        world.getDynastys().add(dynasty);
        return colony;
    }

    private static <T extends Component> List<T> collectComponents(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component comp : root.getComponents()) {
            if (type.isInstance(comp)) {
                found.add(type.cast(comp));
            }
            if (comp instanceof Container nested) {
                found.addAll(collectComponents(nested, type));
            }
        }
        return found;
    }
}
