package emu.com.badlogic.gdx.utils.async;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

import emu.com.badlogic.gdx.assets.AssetLoadingTask;
import org.teavm.jso.browser.TimerHandler;
import org.teavm.jso.browser.Window;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AsyncResult<T> implements TimerHandler {
    private AssetLoadingTask loadingTask;
    private boolean isDone;
    private int count;

    AsyncResult(AsyncTask<T> task) {
        loadingTask = (AssetLoadingTask)task;
        Window.setTimeout(this, 0);
    }

    public boolean isDone() {
        count++;
        if(count > 2) {
            tick(); // If executed this block its possible that it was from finishLoading(). Javascript is async only.
        }
        return isDone;
    }

    public T get() {
        return null;
    }

    @Override
    public void onTimer() {
        tick();
    }

    private void tick() {
        isDone = true;
        try {
            loadingTask.call();
        } catch(Exception e) {
            // Проверяем вложенную причину (если это обернутый Exception)
            Throwable tmp = e;
            StringBuilder builder = new StringBuilder();
            int i = 0;
            while(tmp != null && i < 16) {
                builder.append(tmp.getClass().getSimpleName()).append(" ").append(tmp.getMessage()).append("\n");
                tmp = e.getCause();
                i++;
            }

            AssetDescriptor assetDesc = loadingTask.getAssetDesc();
            AssetLoader loader = loadingTask.getLoader();
            builder.append("assetDesc: ").append(assetDesc.toString());
            builder.append("loader: ").append(loader.getClass().getSimpleName());
            // Выводим в лог чистые строки, которые TeaVM не сможет скрыть заглушками
            Gdx.app.log("ERROR", "path: " + builder.toString());
            throw new RuntimeException(e);
        }
    }
}
