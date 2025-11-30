package com.chaosthedude.explorerscompass.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.OverlaySide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;

public class ExplorersCompassConfig {

    private static Path configFilePath;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static boolean allowTeleport = true;
    public static boolean displayCoordinates = true;
    public static int maxRadius = 10000;
    public static int maxSamples = 100000;
    public static List<String> structureBlacklist = new ArrayList<String>();

    public static boolean displayWithChatOpen = true;
    public static boolean translateStructureNames = true;
    public static int overlayLineOffset = 1;
    public static OverlaySide overlaySide = OverlaySide.LEFT;

    public static Map<String, BlockPos> structureOverrides = new HashMap<>();

    public static void load() {
        if (getFilePath().toFile().exists()) {
            try (var reader = Files.newBufferedReader(getFilePath())) {
                Data data;

                try {
                    data = gson.fromJson(reader, Data.class);
                } catch (Exception e) {
                    ExplorersCompass.LOGGER.error("Failed to load config file. Using default", e);
                    data = new Data(new Data.Common(), new Data.Client(), new Data.CustomWorldStructures());
                }

                allowTeleport = data.common.allowTeleport;
                displayCoordinates = data.common.displayCoordinates;
                maxRadius = data.common.maxRadius;
                maxSamples = data.common.maxSamples;
                structureBlacklist = data.common.structureBlacklist;

                displayWithChatOpen = data.client.displayWithChatOpen;
                translateStructureNames = data.client.translateStructureNames;
                overlayLineOffset = data.client.overlayLineOffset;
                overlaySide = data.client.overlaySide;

                structureOverrides = data.custom.structureOverrides;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        save();
    }

    public static void save() {
        try {
            Writer writer = Files.newBufferedWriter(getFilePath());
            Data data = new Data(
              new Data.Common(allowTeleport, displayCoordinates, maxRadius, maxSamples, structureBlacklist),
              new Data.Client(displayWithChatOpen, translateStructureNames, overlayLineOffset, overlaySide),
              new Data.CustomWorldStructures(structureOverrides)
            );
            gson.toJson(data, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getFilePath() {
        if (configFilePath == null) {
            configFilePath = FabricLoader.getInstance().getConfigDir().resolve(ExplorersCompass.MODID + ".json");
        }
        return configFilePath;
    }

    private static class Data {
        private Common common;
        private Client client;
        private CustomWorldStructures custom;

        public Data(Common common, Client client, CustomWorldStructures custom) {
            this.common = common;
            this.client = client;
            this.custom = custom;
        }

        private static class Common {
            private final String allowTeleportComment = "Allows a player to teleport to a located structure when in creative mode, opped, or in cheat mode.";
            private final boolean allowTeleport;

            private final String displayCoordinatesComment = "Allows players to view the precise coordinates and distance of a located structure on the HUD, rather than relying on the direction the compass is pointing.";
            private final boolean displayCoordinates;

            private final String maxRadiusComment = "The maximum radius that will be searched for a structure. Raising this value will increase search accuracy but will potentially make the process more resource intensive.";
            private final int maxRadius;

            private final String maxSamplesComment = "The maximum number of samples to be taken when searching for a structure.";
            private final int maxSamples;

            private final String structureBlacklistComment = "A list of structures that the compass will not display in the GUI and will not be able to search for. The wildcard character * can be used to match any number of characters, and ? can be used to match one character. Ex (ignore backslashes): [\"minecraft:stronghold\", \"minecraft:endcity\", \"minecraft:*village*\"]";
            private final List<String> structureBlacklist;

            private Common() {
                allowTeleport = true;
                displayCoordinates = true;
                maxRadius = 10000;
                maxSamples = 100000;
                structureBlacklist = new ArrayList<String>();
            }

            private Common(boolean allowTeleport, boolean displayCoordinates, int maxRadius, int maxSamples, List<String> structureBlacklist) {
                this.allowTeleport = allowTeleport;
                this.displayCoordinates = displayCoordinates;
                this.maxRadius = maxRadius;
                this.maxSamples = maxSamples;
                this.structureBlacklist = structureBlacklist;
            }
        }

        private static class Client {
            private final String displayWithChatOpenComment = "Displays compass information even while chat is open.";
            private final boolean displayWithChatOpen;

            private final String translateStructureNamesComment = "Attempts to translate structure names before fixing the unlocalized names. Translations may not be available for all structures.";
            private final boolean translateStructureNames;

            private final String overlayLineOffsetComment = "The line offset for information rendered on the HUD.";
            private final int overlayLineOffset;

            private final String overlaySideComment = "The side for information rendered on the HUD. Ex: LEFT, RIGHT";
            private final OverlaySide overlaySide;

            private Client() {
                displayWithChatOpen = true;
                translateStructureNames = true;
                overlayLineOffset = 1;
                overlaySide = OverlaySide.LEFT;
            }

            private Client(boolean displayWithChatOpen, boolean translateStructureNames, int overlayLineOffset, OverlaySide overlaySide) {
                this.displayWithChatOpen = displayWithChatOpen;
                this.translateStructureNames = translateStructureNames;
                this.overlayLineOffset = overlayLineOffset;
                this.overlaySide = overlaySide;
            }
        }

        private static class CustomWorldStructures {
            private final String structureOverridesComment = "Ты бля нахуй заполни блять и если оно тут будет то эти структуры будут браться, а не искаться.";
            private final Map<String, BlockPos> structureOverrides;

            private CustomWorldStructures() {
                this.structureOverrides = new HashMap<>();
                fill();
            }

            private CustomWorldStructures(Map<String, BlockPos> structureOverrides) {
                this.structureOverrides = structureOverrides;
                fill();
            }

            private void fill() {
                if (this.structureOverrides.isEmpty()) {
                    this.structureOverrides.put("example:structure", new BlockPos(0, 0, 0));
                }
            }
        }
    }

}
