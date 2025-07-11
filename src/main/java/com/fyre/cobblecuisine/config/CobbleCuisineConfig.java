package com.fyre.cobblecuisine.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class CobbleCuisineConfig {
	public final static int CONFIG_VERSION_INTERNAL = 3;

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cobblecuisine.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static CobbleCuisineConfigData data = new CobbleCuisineConfigData();

	public static void load() {
		boolean needsSave = false;

		if (!Files.exists(CONFIG_PATH)) {
			save();
			LOGGER.info("CobbleCuisine >> No config found on disk, writing defaults!");
			return;
		}

		try (Reader in = Files.newBufferedReader(CONFIG_PATH)) {
			JsonReader reader = new JsonReader(in);
			reader.setLenient(true);
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			JsonObject defaultJson = GSON.toJsonTree(new CobbleCuisineConfigData()).getAsJsonObject();

			int loadedVersion = json.has("CONFIG_VERSION_INTERNAL") ? json.get("CONFIG_VERSION_INTERNAL").getAsInt() : 0;
			if (loadedVersion < CONFIG_VERSION_INTERNAL) {
				LOGGER.info("CobbleCuisine >> Config will migrate from version {} to {}!", loadedVersion, CONFIG_VERSION_INTERNAL);
				json.addProperty("CONFIG_VERSION_INTERNAL", CONFIG_VERSION_INTERNAL);
				needsSave = true;
			}

			JsonElement upgraded = upgrade(json, defaultJson);
			if (!upgraded.equals(json)) {
				LOGGER.info("CobbleCuisine >> Config will be upgraded!");
				needsSave = true;
			}

			data = GSON.fromJson(upgraded, CobbleCuisineConfigData.class);

		} catch (Exception e) {
			LOGGER.error("CobbleCuisine >> Failed to load config from disk, using defaults!", e);
			return;
		}

		if (needsSave) save();
		LOGGER.info("CobbleCuisine >> Loaded config in memory!");
	}

	static void save() {
		try (Writer out = Files.newBufferedWriter(CONFIG_PATH)) {
			JsonObject json = new JsonObject();
			json.addProperty("CONFIG_VERSION_INTERNAL", CONFIG_VERSION_INTERNAL);
			for (Field field : CobbleCuisineConfigData.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;
				field.setAccessible(true);
				try {
					Object value = field.get(data);
					JsonElement jsonVal = GSON.toJsonTree(value);
					json.add(field.getName(), jsonVal);
				} catch (IllegalAccessException e) {
					LOGGER.error("CobbleCuisine >> Failed to access config field: {}", field.getName(), e);
				}
			}
			GSON.toJson(json, out);
		} catch (Exception e) {
			LOGGER.error("CobbleCuisine >> Failed to save config to disk!", e);
		}
	}

	private static JsonElement upgrade(JsonElement original, JsonElement defaults) {
		if (original.isJsonObject() && defaults.isJsonObject()) {
			JsonObject origObj = original.getAsJsonObject();
			JsonObject defObj = defaults.getAsJsonObject();

			List<String> keys = new ArrayList<>(origObj.keySet());
			for (String key : keys) {
				if (!defObj.has(key)) {
					origObj.remove(key);
				}
			}

			for (Map.Entry<String, JsonElement> entry : defObj.entrySet()) {
				String key = entry.getKey();
				JsonElement defVal = entry.getValue();
				if (origObj.has(key)) {
					origObj.add(key, upgrade(origObj.get(key), defVal));
				} else {
					origObj.add(key, defVal);
				}
			}
			return origObj;
		}
		return original;
	}
}
