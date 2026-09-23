import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * One-off generator for the five redox upgrade item textures.
 *
 * <p>Style rule: the plate is Sophisticated Backpacks' own upgrade template, not something redrawn.
 * {@link #BASE_PLATE} is the exact pixel grid of {@code sophisticatedbackpacks:textures/item/upgrade_base.png};
 * {@link #ADVANCED_PLATE} is the exact grid of {@code advanced_magnet_upgrade.png}, whose own magnet emblem pixels
 * are mapped back to the plain plate colour in {@link #ADVANCED_COLORS} - giving the base mod's real
 * "advanced = gold stitching" look. Only the small dark emblem in the middle is ours, so the five icons sit in the
 * base mod's set without sticking out.
 *
 * <pre>java tools/TextureGen.java &lt;output-dir&gt;</pre>
 */
public final class TextureGen {
	// --- Sophisticated Backpacks house palette (sampled straight out of the mod's own textures) ---
	private static final int TRANSPARENT = 0x00000000;
	private static final int STITCH_DARK = 0xFF542716;   // dark stitching of a standard upgrade
	private static final int STITCH_GOLD = 0xFFFFD83E;   // gold stitching of an "advanced" upgrade
	private static final int STUD_GREY = 0xFFC1C1C1;
	private static final int RIM_BROWN = 0xFF9E492A;
	private static final int PLATE_COPPER = 0xFFC65C35;
	private static final int EMBLEM = 0xFF3D1C10;        // the base mod draws every emblem in this colour

	/** Verbatim pixels of {@code sophisticatedbackpacks:textures/item/upgrade_base.png}. */
	private static final String[] BASE_PLATE = {
			"AAAAAAAAAAAAAAAA",
			"ABBCBBCBBCBBCBBA",
			"ABDDDDDDDDDDDDBA",
			"ACDEEEEEEEEEEDCA",
			"ABDEEEEEEEEEEDBA",
			"ABDEEEEEEEEEEDBA",
			"ACDEEEEEEEEEEDCA",
			"ABDEEEEEEEEEEDBA",
			"ABDEEEEEEEEEEDBA",
			"ACDEEEEEEEEEEDCA",
			"ABDEEEEEEEEEEDBA",
			"ABDEEEEEEEEEEDBA",
			"ACDEEEEEEEEEEDCA",
			"ABDDDDDDDDDDDDBA",
			"ABBCBBCBBCBBCBBA",
			"AAAAAAAAAAAAAAAA",
	};

	/**
	 * Verbatim pixels of {@code advanced_magnet_upgrade.png}. {@code G} below is that texture's magnet emblem;
	 * {@link #ADVANCED_COLORS} maps it back to the plate colour so the plate is left clean for our own emblem.
	 */
	private static final String[] ADVANCED_PLATE = {
			"AAAAAAAAAAAAAAAA",
			"ABBBCCDCCDCCBBBA",
			"ABEEEEEEEEEEEEBA",
			"ABEFFFFFFFFFFEBA",
			"ACEFFGGGGGFGFECA",
			"ACEFGGGGGGFGFECA",
			"ADEFGGFFFFFFFEDA",
			"ACEFGGFFFFFFFECA",
			"ACEFGGFFFFFFFECA",
			"ADEFGGFFFFFFFEDA",
			"ACEFGGGGGGFGFECA",
			"ACEFFGGGGGFGFECA",
			"ABEFFFFFFFFFFEBA",
			"ABEEEEEEEEEEEEBA",
			"ABBBCCDCCDCCBBBA",
			"AAAAAAAAAAAAAAAA",
	};

	private static final Map<Character, Integer> BASE_COLORS = Map.of(
			'A', TRANSPARENT, 'B', STITCH_DARK, 'C', STUD_GREY, 'D', RIM_BROWN, 'E', PLATE_COPPER);
	private static final Map<Character, Integer> ADVANCED_COLORS = Map.of(
			'A', TRANSPARENT, 'B', STITCH_GOLD, 'C', STITCH_DARK, 'D', STUD_GREY, 'E', RIM_BROWN,
			'F', PLATE_COPPER, 'G', PLATE_COPPER);

	// --- emblems, drawn inside the 10x10 inner plate (x/y between 3 and 12) ---

	/** Oxidation: a drop of moisture - what actually weathers copper in the first place. */
	private static final int[][] DROP = {
			{7, 4}, {8, 4}, {7, 5}, {8, 5}, {6, 6}, {7, 6}, {8, 6}, {9, 6},
			{5, 7}, {6, 7}, {7, 7}, {8, 7}, {9, 7}, {10, 7},
			{5, 8}, {6, 8}, {7, 8}, {8, 8}, {9, 8}, {10, 8},
			{6, 9}, {7, 9}, {8, 9}, {9, 9}, {7, 10}, {8, 10},
	};

	/** Reduction: a hatchet - the vanilla way of scraping oxidation back off copper. */
	private static final int[][] HATCHET = {
			{7, 3}, {8, 3}, {9, 3},
			{6, 4}, {7, 4}, {8, 4}, {9, 4}, {10, 4},
			{5, 5}, {6, 5}, {7, 5}, {8, 5}, {9, 5}, {10, 5},
			{5, 6}, {6, 6}, {7, 6}, {8, 6}, {9, 6},
			{6, 7}, {7, 7}, {8, 7},
			{6, 8}, {7, 8},
			{5, 9}, {6, 9},
			{5, 10}, {6, 10},
	};

	/** Waxing: a honeycomb cell, its hollow centre punched back to the plate colour. */
	private static final int[][] HONEYCOMB = {
			{7, 4}, {8, 4}, {6, 5}, {7, 5}, {8, 5}, {9, 5},
			{5, 6}, {6, 6}, {7, 6}, {8, 6}, {9, 6}, {10, 6},
			{5, 7}, {6, 7}, {7, 7}, {8, 7}, {9, 7}, {10, 7},
			{6, 8}, {7, 8}, {8, 8}, {9, 8}, {7, 9}, {8, 9},
	};
	private static final int[][] HONEYCOMB_CELL = {{7, 7}, {8, 7}, {7, 8}, {8, 8}};

	private TextureGen() {
	}

	public static void main(String[] args) throws IOException {
		File outDir = new File(args[0]);
		if (!outDir.isDirectory() && !outDir.mkdirs()) {
			throw new IOException("Cannot create output directory " + outDir);
		}

		write(outDir, "oxidation_upgrade", false, DROP, null);
		write(outDir, "advanced_oxidation_upgrade", true, DROP, null);
		write(outDir, "reduction_upgrade", false, HATCHET, null);
		write(outDir, "advanced_reduction_upgrade", true, HATCHET, null);
		write(outDir, "waxing_upgrade", false, HONEYCOMB, HONEYCOMB_CELL);

		System.out.println("Wrote 5 textures to " + outDir.getAbsolutePath());
	}

	private static void write(File outDir, String name, boolean advanced, int[][] emblem, int[][] cutout) throws IOException {
		String[] plate = advanced ? ADVANCED_PLATE : BASE_PLATE;
		Map<Character, Integer> colors = advanced ? ADVANCED_COLORS : BASE_COLORS;
		checkGrid(name, plate);

		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				image.setRGB(x, y, colors.getOrDefault(plate[y].charAt(x), TRANSPARENT));
			}
		}
		stamp(image, emblem, EMBLEM);
		if (cutout != null) {
			stamp(image, cutout, PLATE_COPPER);
		}
		ImageIO.write(image, "png", new File(outDir, name + ".png"));
	}

	private static void checkGrid(String name, String[] plate) {
		if (plate.length != 16) {
			throw new IllegalStateException(name + ": plate must have 16 rows, has " + plate.length);
		}
		for (int y = 0; y < plate.length; y++) {
			if (plate[y].length() != 16) {
				throw new IllegalStateException(name + ": plate row " + y + " must be 16 wide, is " + plate[y].length());
			}
		}
	}

	private static void stamp(BufferedImage image, int[][] pixels, int color) {
		for (int[] pixel : pixels) {
			image.setRGB(pixel[0], pixel[1], color);
		}
	}
}
