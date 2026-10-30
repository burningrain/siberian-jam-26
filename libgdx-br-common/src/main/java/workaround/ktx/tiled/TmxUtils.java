package workaround.ktx.tiled;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public class TmxUtils {

    public static String getTmx(String tmx) {
//        if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
//            return "tiled-packed/" + tmx;
//        }
        return "tiled-packed-ktx/" + tmx;
    }

}
