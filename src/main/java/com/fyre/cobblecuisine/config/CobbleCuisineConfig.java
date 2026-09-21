package com.fyre.cobblecuisine.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fyre.cobblecuisine.CobbleCuisine.LOGGER;

public class CobbleCuisineConfig {
	public final static int CONFIG_VERSION_INTERNAL = 5;

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cobblecuisine.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static volatile CobbleCuisineConfigData data = new CobbleCuisineConfigData();

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			if (save(copyData())) {
				LOGGER.info("CobbleCuisine >> No config found on disk, wrote current defaults/settings!");
			}
			return;
		}

		CobbleCuisineConfigData candidate;
		boolean needsSave;
		try (Reader in = Files.newBufferedReader(CONFIG_PATH)) {
			JsonReader reader = new JsonReader(in);
			reader.setLenient(true);
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			JsonObject original = json.deepCopy();
			JsonObject defaultJson = GSON.toJsonTree(new CobbleCuisineConfigData()).getAsJsonObject();
			defaultJson.addProperty("CONFIG_VERSION_INTERNAL", CONFIG_VERSION_INTERNAL);

			int loadedVersion = json.has("CONFIG_VERSION_INTERNAL") && !json.get("CONFIG_VERSION_INTERNAL").isJsonNull()
					? json.get("CONFIG_VERSION_INTERNAL").getAsInt() : 0;
			if (loadedVersion < CONFIG_VERSION_INTERNAL) {
				LOGGER.info("CobbleCuisine >> Config will migrate from version {} to {}!", loadedVersion, CONFIG_VERSION_INTERNAL);
				CobbleCuisineConfigMigration.migrate(json, loadedVersion, CONFIG_VERSION_INTERNAL);
			}

			JsonElement upgraded = upgrade(json, defaultJson);
			candidate = GSON.fromJson(upgraded, CobbleCuisineConfigData.class);
			boolean sanitized = validate(candidate);
			needsSave = sanitized || !upgraded.equals(original);
		} catch (Exception e) {
			LOGGER.error("CobbleCuisine >> Failed to load config; keeping current settings and leaving the file unchanged!", e);
			return;
		}

		// A rewrite failure does not prevent using an otherwise valid loaded configuration.
		if (needsSave) write(candidate);
		data = candidate;
		LOGGER.info("CobbleCuisine >> Loaded validated config in memory!");
	}

	static CobbleCuisineConfigData copyData() {
		return GSON.fromJson(GSON.toJsonTree(data), CobbleCuisineConfigData.class);
	}

	static boolean save(CobbleCuisineConfigData candidate) {
		try {
			validate(candidate);
		} catch (Exception e) {
			LOGGER.error("CobbleCuisine >> Invalid config; current settings and file were not changed!", e);
			return false;
		}
		if (!write(candidate)) return false;
		data = candidate;
		return true;
	}

	private static boolean validate(CobbleCuisineConfigData candidate) {
		boolean changed = CobbleCuisineConfigSanitizer.sanitize(candidate);
		if (changed) LOGGER.info("CobbleCuisine >> Invalid config values were sanitized!");
		return changed;
	}

	private static boolean write(CobbleCuisineConfigData candidate) {
		Path temporary = null;
		try {
			JsonObject json = GSON.toJsonTree(candidate).getAsJsonObject();
			json.addProperty("CONFIG_VERSION_INTERNAL", CONFIG_VERSION_INTERNAL);
			// Serialize before touching disk, then replace only with a complete file.
			String serialized = GSON.toJson(json);
			Files.createDirectories(CONFIG_PATH.getParent());
			temporary = Files.createTempFile(CONFIG_PATH.getParent(), "cobblecuisine-", ".tmp");
			Files.writeString(temporary, serialized);
			try {
				Files.move(temporary, CONFIG_PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				LOGGER.warn("CobbleCuisine >> Atomic config replacement unavailable; using complete-file replacement.");
				Files.move(temporary, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("CobbleCuisine >> Failed to save config to disk!", e);
			return false;
		} finally {
			if (temporary != null) {
				try {
					Files.deleteIfExists(temporary);
				} catch (IOException e) {
					LOGGER.warn("CobbleCuisine >> Could not remove temporary config file {}", temporary, e);
				}
			}
		}
	}

	private static JsonElement upgrade(JsonElement original, JsonElement defaults) {
		if (original == null || original.isJsonNull()) return defaults.deepCopy();
		if (defaults.isJsonObject()) {
			if (!original.isJsonObject()) throw new JsonParseException("Expected a config section object");
			JsonObject origObj = original.getAsJsonObject();
			JsonObject defObj = defaults.getAsJsonObject();

			List<String> keys = new ArrayList<>(origObj.keySet());
			for (String key : keys) {
				if (!defObj.has(key)) origObj.remove(key);
			}
			for (Map.Entry<String, JsonElement> entry : defObj.entrySet()) {
				String key = entry.getKey();
				origObj.add(key, upgrade(origObj.get(key), entry.getValue()));
			}
			return origObj;
		}
		if (!original.isJsonPrimitive()) throw new JsonParseException("Expected a numeric config value");
		return original;
	}
}
