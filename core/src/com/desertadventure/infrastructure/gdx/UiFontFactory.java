package com.desertadventure.infrastructure.gdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardRepository;
import com.desertadventure.combat.card.data.GdxCardRepositoryLoader;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;
import java.util.LinkedHashSet;
import java.util.Set;

/** Builds a HUD {@link BitmapFont} with glyphs needed for Traditional Chinese UI text. */
public final class UiFontFactory {
    private static final String FONT_PATH = "fonts/NotoSansCJKtc-Regular.otf";
    private static final int FONT_SIZE_PX = 15;

    private static final String EXTRA_UI_TEXT =
            "冷卻：剩餘 回合可出牌Stage /—";

    private UiFontFactory() {
    }

    public static BitmapFont createHudFont() {
        FileHandle fontFile = AssetFiles.internal(FONT_PATH);
        if (!fontFile.exists()) {
            throw new IllegalStateException("UI font missing: " + FONT_PATH);
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        try {
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = FONT_SIZE_PX;
            parameter.characters = buildCharacterSet();
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }
    }

    private static String buildCharacterSet() {
        Set<Integer> codePoints = new LinkedHashSet<>();
        addText(codePoints, FreeTypeFontGenerator.DEFAULT_CHARS);
        addText(codePoints, EXTRA_UI_TEXT);
        for (PositiveStatusType type : PositiveStatusType.values()) {
            addText(codePoints, type.getDisplayLabel());
        }
        for (NegativeStatusType type : NegativeStatusType.values()) {
            addText(codePoints, type.getDisplayLabel());
        }
        CardRepository repository = GdxCardRepositoryLoader.loadDefault();
        for (CardDef def : repository.getAll().values()) {
            if (def.name != null) {
                addText(codePoints, def.name);
            }
            if (def.description != null) {
                addText(codePoints, def.description);
            }
        }
        StringBuilder out = new StringBuilder();
        for (int cp : codePoints) {
            out.appendCodePoint(cp);
        }
        return out.toString();
    }

    private static void addText(Set<Integer> codePoints, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            codePoints.add(cp);
            i += Character.charCount(cp);
        }
    }
}
