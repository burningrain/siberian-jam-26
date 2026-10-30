package workaround.ktx.tiled;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;

public class SafeTiledKtx2TextureData implements TextureData {

    public static final Pixmap PIXMAP = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    private final TextureData originalData;

    public SafeTiledKtx2TextureData(TextureData originalData) {
        this.originalData = originalData;
    }

    // МЕТОД ХАК: Возвращаем формат, чтобы checkTransparencySupport() прошел проверку!
    @Override
    public Pixmap.Format getFormat() {
        return Pixmap.Format.RGBA8888;
    }

    // Все остальные методы просто проксируем в оригинальный Ktx2TextureData
    @Override
    public TextureDataType getType() {
        return originalData.getType();
    }

    @Override
    public boolean isPrepared() {
        return originalData.isPrepared();
    }

    @Override
    public void prepare() {
        originalData.prepare();
    }

    @Override
    public void consumeCustomData(int target) {
        originalData.consumeCustomData(target);
    }

    @Override
    public int getWidth() {
        return originalData.getWidth();
    }

    @Override
    public int getHeight() {
        return originalData.getHeight();
    }

    @Override
    public boolean useMipMaps() {
        return originalData.useMipMaps();
    }

    @Override
    public boolean isManaged() {
        return originalData.isManaged();
    }

    @Override
    public Pixmap consumePixmap() {
        return PIXMAP;
    }

    @Override
    public boolean disposePixmap() {
        return true;
    }

}
