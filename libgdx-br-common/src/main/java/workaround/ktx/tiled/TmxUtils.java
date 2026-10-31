package workaround.ktx.tiled;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public class TmxUtils {

    public static String getTmx(String tmx, boolean isUseKtx2) {
        if (isUseKtx2) {
            return "tiled-packed-ktx/" + tmx;
        }

        return "tiled-packed/" + tmx;
    }

}
