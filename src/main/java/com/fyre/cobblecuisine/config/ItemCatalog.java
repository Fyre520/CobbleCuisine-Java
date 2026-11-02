package com.fyre.cobblecuisine.config;

import com.fyre.cobblecuisine.item.food.BeanItem;
import com.fyre.cobblecuisine.item.food.FancyShakeItem;
import com.fyre.cobblecuisine.item.food.MalasadaItem;
import com.fyre.cobblecuisine.item.food.PokePuffItem;
import com.fyre.cobblecuisine.item.food.ShakeItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

final class ItemCatalog {
	private static final int ICON_CYCLE_MILLIS = 1000;

	private final List<Candidate> candidates = new ArrayList<>();
	private final Map<String, Candidate> byKey = new HashMap<>();

	private ItemCatalog() {
	}

	record Candidate(String value, String name, boolean tag, List<ItemStack> icons, List<String> words) {
	}

	private record Scored(Candidate candidate, int score) {
	}

	static ItemCatalog build() {
		ItemCatalog catalog = new ItemCatalog();
		if (MinecraftClient.getInstance().world != null) {
			Registries.ITEM.streamTags().forEach(tag -> {
				List<ItemStack> icons = new ArrayList<>();
				for (RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(tag)) {
					if (feedable(entry.value())) icons.add(new ItemStack(entry.value()));
				}
				if (!icons.isEmpty()) catalog.addTag(tag.id(), icons);
			});
		} else {
			Map<Identifier, List<String>> tags = scannedTags();
			for (Identifier id : tags.keySet()) {
				List<ItemStack> icons = new ArrayList<>();
				for (Identifier itemId : resolve(id, tags, new LinkedHashSet<>())) {
					Item item = Registries.ITEM.get(itemId);
					if (feedable(item)) icons.add(new ItemStack(item));
				}
				if (!icons.isEmpty()) catalog.addTag(id, icons);
			}
		}
		for (Identifier id : Registries.ITEM.getIds()) {
			Item item = Registries.ITEM.get(id);
			if (!feedable(item)) continue;
			ItemStack stack = new ItemStack(item);
			String name = stack.getName().getString();
			catalog.add(new Candidate(id.toString(), name, false, List.of(stack), words(name + " " + id)));
		}
		return catalog;
	}

	private static boolean feedable(Item item) {
		return item instanceof BeanItem || item instanceof PokePuffItem || item instanceof MalasadaItem
				|| item instanceof ShakeItem || item instanceof FancyShakeItem;
	}

	private static Set<Identifier> resolve(Identifier tag, Map<Identifier, List<String>> tags, Set<Identifier> visiting) {
		Set<Identifier> items = new LinkedHashSet<>();
		if (!visiting.add(tag)) return items;
		for (String value : tags.getOrDefault(tag, List.of())) {
			if (value.startsWith("#")) {
				Identifier nested = Identifier.tryParse(value.substring(1));
				if (nested != null) items.addAll(resolve(nested, tags, visiting));
			} else {
				Identifier item = Identifier.tryParse(value);
				if (item != null) items.add(item);
			}
		}
		visiting.remove(tag);
		return items;
	}

	static ItemStack icon(List<ItemStack> icons) {
		if (icons.isEmpty()) return new ItemStack(Items.NAME_TAG);
		return icons.get((int) ((System.currentTimeMillis() / ICON_CYCLE_MILLIS) % icons.size()));
	}

	static List<String> clean(List<String> values) {
		Set<String> seen = new LinkedHashSet<>();
		if (values == null) return new ArrayList<>();
		for (String value : values) {
			String trimmed = value == null ? "" : value.trim();
			if (!trimmed.isEmpty()) seen.add(trimmed);
		}
		return new ArrayList<>(seen);
	}

	boolean known(String value) {
		return byKey.containsKey(key(value));
	}

	List<ItemStack> icons(String value) {
		Candidate candidate = byKey.get(key(value));
		return candidate == null ? List.of() : candidate.icons();
	}

	List<Candidate> search(String query, int limit) {
		String[] parts = query.toLowerCase(Locale.ROOT).trim().split("[\\s:_/#]+");
		List<Scored> scored = new ArrayList<>();
		for (Candidate candidate : candidates) {
			int score = score(parts, candidate);
			if (score > 0) scored.add(new Scored(candidate, score));
		}
		scored.sort(Comparator.comparingInt((Scored s) -> -s.score())
				.thenComparing(s -> !s.candidate().tag())
				.thenComparingInt(s -> s.candidate().name().length())
				.thenComparing(s -> s.candidate().name()));
		return scored.stream().limit(limit).map(Scored::candidate).toList();
	}

	private void addTag(Identifier id, List<ItemStack> icons) {
		if (id.getPath().startsWith("recipe_filters/")) return;
		String value = "#" + id;
		add(new Candidate(value, value, true, icons, words(id.toString())));
	}

	private void add(Candidate candidate) {
		if (byKey.putIfAbsent(key(candidate.value()), candidate) == null) candidates.add(candidate);
	}

	private static int score(String[] parts, Candidate candidate) {
		int total = 0;
		for (String part : parts) {
			if (part.isEmpty()) continue;
			int best = 0;
			for (String word : candidate.words()) best = Math.max(best, wordScore(part, word));
			if (best == 0) return 0;
			total += best;
		}
		return total;
	}

	private static int wordScore(String part, String word) {
		if (word.equals(part)) return 4;
		if (word.startsWith(part)) return 3;
		if (word.contains(part)) return 2;
		if (part.length() < 3) return 0;
		String head = word.substring(0, Math.min(word.length(), part.length()));
		int allowed = part.length() >= 5 ? 2 : 1;
		return distance(part, head) <= allowed ? 1 : 0;
	}

	private static int distance(String a, String b) {
		int[] previous = new int[b.length() + 1];
		int[] current = new int[b.length() + 1];
		for (int j = 0; j <= b.length(); j++) previous[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			current[0] = i;
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
			}
			int[] swap = previous;
			previous = current;
			current = swap;
		}
		return previous[b.length()];
	}

	private static List<String> words(String text) {
		List<String> words = new ArrayList<>();
		for (String word : text.toLowerCase(Locale.ROOT).split("[\\s:_/#.'-]+")) {
			if (!word.isEmpty()) words.add(word);
		}
		return words;
	}

	private static String key(String value) {
		String trimmed = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
		boolean tag = trimmed.startsWith("#");
		Identifier id = Identifier.tryParse(tag ? trimmed.substring(1) : trimmed);
		if (id == null) return trimmed;
		return (tag ? "#" : "") + id;
	}

	private static Map<Identifier, List<String>> scannedTags() {
		Map<Identifier, List<String>> tags = new HashMap<>();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			for (Path root : mod.getRootPaths()) {
				Path data = root.resolve("data");
				if (!Files.isDirectory(data)) continue;
				try (Stream<Path> namespaces = Files.list(data)) {
					for (Path namespace : namespaces.toList()) {
						Path tagDir = namespace.resolve("tags").resolve("item");
						if (!Files.isDirectory(tagDir)) continue;
						try (Stream<Path> files = Files.walk(tagDir)) {
							for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
								String path = tagDir.relativize(file).toString().replace('\\', '/');
								Identifier id = Identifier.tryParse(namespace.getFileName().toString().replace("/", "")
										+ ":" + path.substring(0, path.length() - ".json".length()));
								if (id != null) tags.computeIfAbsent(id, key -> new ArrayList<>()).addAll(values(file));
							}
						}
					}
				} catch (IOException ignored) {
				}
			}
		}
		return tags;
	}

	private static List<String> values(Path file) {
		List<String> values = new ArrayList<>();
		try (Reader in = Files.newBufferedReader(file)) {
			JsonElement json = JsonParser.parseReader(in);
			if (!json.isJsonObject() || !json.getAsJsonObject().has("values")) return values;
			for (JsonElement value : json.getAsJsonObject().getAsJsonArray("values")) {
				if (value.isJsonPrimitive()) values.add(value.getAsString());
				else if (value.isJsonObject() && value.getAsJsonObject().has("id")) values.add(value.getAsJsonObject().get("id").getAsString());
			}
		} catch (Exception ignored) {
		}
		return values;
	}
}
