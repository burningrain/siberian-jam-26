package workaround.ktx.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.BaseTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;

public class Ktx2AtlasTmxMapLoader extends BaseTmxMapLoader<BaseTiledMapLoader.Parameters> {

    protected interface AtlasResolver extends ImageResolver {

        TextureAtlas atlas();

        record DirectAtlasResolver(TextureAtlas atlas) implements AtlasResolver {
            @Override
            public TextureRegion getImage(String name) {
                // check for imagelayer and strip if needed
                String regionName = parseRegionName(name);
                return patchAndCreateRegion(atlas.findRegion(regionName));
            }
        }

        class AssetManagerAtlasResolver implements AtlasResolver {
            private final AssetManager assetManager;
            private final String atlasName;

            public AssetManagerAtlasResolver(AssetManager assetManager, String atlasName) {
                this.assetManager = assetManager;
                this.atlasName = atlasName;
            }

            @Override
            public TextureAtlas atlas() {
                return assetManager.get(atlasName, TextureAtlas.class);
            }

            @Override
            public TextureRegion getImage(String name) {
                // check for imagelayer and strip if needed
                String regionName = parseRegionName(name);
                return patchAndCreateRegion(atlas().findRegion(regionName));
            }
        }
    }

    protected Array<Texture> trackedTextures = new Array<Texture>();

    protected AtlasResolver atlasResolver;

    public Ktx2AtlasTmxMapLoader() {
        super(new InternalFileHandleResolver());
    }

    public Ktx2AtlasTmxMapLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public TiledMap load(String fileName) {
        return load(fileName, new Parameters());
    }

    public TiledMap load(String fileName, Parameters parameter) {
        FileHandle tmxFile = resolve(fileName);

        this.root = xml.parse(tmxFile);

        final FileHandle atlasFileHandle = getAtlasFileHandle(tmxFile);
        TextureAtlas atlas = new TextureAtlas(atlasFileHandle);
        this.atlasResolver = new AtlasResolver.DirectAtlasResolver(atlas);

        TiledMap map = loadTiledMap(tmxFile, parameter, atlasResolver);
        map.setOwnedResources(new Array<TextureAtlas>(new TextureAtlas[]{atlas}));
        setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
        return map;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle tmxFile, Parameters parameter) {
        FileHandle atlasHandle = getAtlasFileHandle(tmxFile);
        this.atlasResolver = new AtlasResolver.AssetManagerAtlasResolver(manager, atlasHandle.path());
        try {
            this.map = loadTiledMap(tmxFile, parameter, atlasResolver);
        } catch (Exception e) {
            String ex = toString(e);

            Gdx.app.log("ERROR Ktx2AtlasTmxMapLoader: ", ex);
            Gdx.app.log("atlas: ", atlasHandle.path());

            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public TiledMap loadSync(AssetManager manager, String fileName, FileHandle file, Parameters parameter) {
        if (parameter != null) {
            setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
        }

        return map;
    }

    @Override
    protected Array<AssetDescriptor> getDependencyAssetDescriptors(FileHandle tmxFile,
                                                                   TextureLoader.TextureParameter textureParameter) {
        Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();

        // Atlas dependencies
        final FileHandle atlasFileHandle = getAtlasFileHandle(tmxFile);
        if (atlasFileHandle != null) {
            descriptors.add(new AssetDescriptor(atlasFileHandle, TextureAtlas.class));
        }

        return descriptors;
    }

    @Override
    protected void addStaticTiles(
        FileHandle tmxFile,
        ImageResolver imageResolver,
        TiledMapTileSet tileSet,
        XmlReader.Element element,
        Array<XmlReader.Element> tileElements,
        String name,
        int firstgid,
        int tilewidth,
        int tileheight,
        int spacing,
        int margin,
        int offsetX,
        int offsetY,
        String imageSource,
        int imageWidth,
        int imageHeight,
        FileHandle image
    ) {

        TextureAtlas atlas = atlasResolver.atlas();
        String regionsName = name;

        for (Texture texture : atlas.getTextures()) {
            trackedTextures.add(texture);
        }

        MapProperties props = tileSet.getProperties();
        props.put("imagesource", imageSource);
        props.put("imagewidth", imageWidth);
        props.put("imageheight", imageHeight);
        props.put("tilewidth", tilewidth);
        props.put("tileheight", tileheight);
        props.put("margin", margin);
        props.put("spacing", spacing);

        if (imageSource != null && imageSource.length() > 0) {
            int lastgid = firstgid + ((imageWidth / tilewidth) * (imageHeight / tileheight)) - 1;
            for (TextureAtlas.AtlasRegion region : atlas.findRegions(regionsName)) {
                // Handle unused tileIds
                if (region != null) {
                    int tileId = firstgid + region.index;
                    if (tileId >= firstgid && tileId <= lastgid) {
                        addStaticTiledMapTile(tileSet, region, tileId, offsetX, offsetY);
                    }
                }
            }
        }

        // Add tiles with individual image sources
        for (XmlReader.Element tileElement : tileElements) {
            int tileId = firstgid + tileElement.getIntAttribute("id", 0);
            TiledMapTile tile = tileSet.getTile(tileId);
            if (tile == null) {
                XmlReader.Element imageElement = tileElement.getChildByName("image");
                if (imageElement != null) {
                    String regionName = imageElement.getAttribute("source");
                    regionName = regionName.substring(0, regionName.lastIndexOf('.'));
                    TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);
                    if (region == null)
                        throw new GdxRuntimeException("Tileset atlasRegion not found: " + regionName);
                    addStaticTiledMapTile(tileSet, region, tileId, offsetX, offsetY);
                }
            }
        }
    }

    protected FileHandle getAtlasFileHandle(FileHandle tmxFile) {
        XmlReader.Element properties = root.getChildByName("properties");

        String atlasFilePath = null;
        if (properties != null) {
            for (XmlReader.Element property : properties.getChildrenByName("property")) {
                String name = property.getAttribute("name");
                if (name.startsWith("atlas")) {
                    atlasFilePath = property.getAttribute("value");
                    break;
                }
            }
        }
        if (atlasFilePath == null) {
            throw new GdxRuntimeException("The map is missing the 'atlas' property");
        } else {
            final FileHandle fileHandle = getRelativeFileHandle(tmxFile, atlasFilePath);
            if (!fileHandle.exists()) {
                throw new GdxRuntimeException("The 'atlas' file could not be found: '" + atlasFilePath + "'");
            }
            return fileHandle;
        }
    }

    protected void setTextureFilters(Texture.TextureFilter min, Texture.TextureFilter mag) {
        for (Texture texture : trackedTextures) {
            texture.setFilter(min, mag);
        }
        trackedTextures.clear();
    }

    /**
     * Parse incoming region name to check for 'atlas_imagelayer' within the String These are regions representing Image Layers
     * that have been packed into the atlas ImageLayer Image names include the relative assets path, so it must be stripped.
     *
     * @param name Name to check
     * @return The name of the region to pass into an atlas
     */
    static String parseRegionName(String name) {
        if (name.contains("atlas_imagelayer")) {
            // Find the last '/' in the path
            int lastSlash = name.lastIndexOf('/');
            // If we found a slash, return everything after it which should be our region name
            // If no slashes found return entire string
            return (lastSlash >= 0) ? name.substring(lastSlash + 1) : name;
        } else {
            return name;
        }
    }

    private static TextureRegion patchAndCreateRegion(TextureAtlas.AtlasRegion region) {
        Texture texture = region.getTexture();
        TextureData originalData = texture.getTextureData();

        try {
            // Создаем вашу безопасную заглушку
            SafeTiledKtx2TextureData safeData = new SafeTiledKtx2TextureData(originalData);
            texture.setData(safeData);

            // 2. Создаем регион. Теперь checkTransparencySupport() увидит safeData и не упадет
            TextureRegion resultRegion = new TextureRegion(region);
            return resultRegion;
        } catch (Exception e) {
            Gdx.app.log("Ktx2AtlasTmxMapLoader.patchAndCreateRegion", "Ошибка при патчинге: " + e.getMessage());
            throw new GdxRuntimeException(e);
        }
    }

    private static String toString(Exception e) {
        Throwable tmp = e;
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (tmp != null && i < 16) {
            builder.append(tmp.getClass().getSimpleName()).append(" ").append(tmp.getMessage()).append("\n");
            tmp = e.getCause();
            i++;
        }
        return builder.toString();
    }

}

