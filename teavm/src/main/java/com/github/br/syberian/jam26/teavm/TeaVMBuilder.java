package com.github.br.syberian.jam26.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.teavm.backend.wasm.WasmDebugInfoLevel;
import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
public class TeaVMBuilder {
    /**
     * A single point to configure most debug vs. release settings.
     * This defaults to false in new projects; set this to false when you want to release.
     * If this is true, the output will not be obfuscated, and debug information will usually be produced.
     * You can still set obfuscation to false in a release if you want the source to be at least a little legible.
     * This works well when the targetType is set to JAVASCRIPT, but you can still set the targetType to WEBASSEMBLY_GC
     * while this is true in order to test that higher-performance target before releasing.
     */
    private static final boolean DEBUG = true;

    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();

        teaBuildConfiguration.htmlWidth = 540;
        teaBuildConfiguration.htmlHeight = 960;

        // Register any extra classpath assets here:
        // teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/github/br/libgdx/jam36/asset.extension");

        // If you need to match specific classes based on the package and class name,
        // you can use the reflectionListener to do fine-grained matching on the String fullClassName.
//        teaBuildConfiguration.reflectionListener = fullClassName -> {
//            if(fullClassName.startsWith("where.your.reflective.code.is") && fullClassName.endsWith("YourSuffix"))
//                return true;
//            return false;
//        };
        teaBuildConfiguration.reflectionListener = fullClassName -> {
            // Разрешаем рефлексию для TenPatch и всех связанных классов
            if (fullClassName.startsWith("com.ray3k.tenpatch")) {
                return true;
            }
            // Если есть другие классы, требующие рефлексии (например, Stripe)
            if (fullClassName.startsWith("com.ray3k.stripe")) {
                return true;
            }
            if (fullClassName.startsWith("com.github.br.syberian.jam26")) {
                return true;
            }

            return false;
        };

        // You can also register any classes or packages that require reflection here:
        // TenPatch классы
        TeaReflectionSupplier.addReflectionClass("com.ray3k.tenpatch.TenPatchDrawable");
        TeaReflectionSupplier.addReflectionClass("com.ray3k.tenpatch.TenPatch");

        // Stripe классы (если используете)
        TeaReflectionSupplier.addReflectionClass("com.ray3k.stripe.ScrollPane");
        TeaReflectionSupplier.addReflectionClass("com.ray3k.stripe.PopupTable");
        TeaReflectionSupplier.addReflectionClass("com.ray3k.stripe.DraggableList");

        // Все пакеты целиком (если хотите включить всё)
        TeaReflectionSupplier.addReflectionClass("com.ray3k.tenpatch");
        TeaReflectionSupplier.addReflectionClass("com.ray3k.stripe");

        TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.scenes.scene2d.ui.Label$LabelStyle");
        TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.scenes.scene2d.ui.TextButton$TextButtonStyle");

        TeaReflectionSupplier.addReflectionClass("com.github.tommyettinger.textra.Styles$LabelStyle");
        TeaReflectionSupplier.addReflectionClass("com.github.br.libgdx.common.structure.screen.ui.TypingImageTextButton$TypingImageTextButtonStyle");

        TeaReflectionSupplier.addReflectionClass("com.crashinvaders.vfx.gwt.GwtVfxGlExtension");
        //TeaReflectionSupplier.addReflectionClass("workaround.ktx.tiled.Ktx2AtlasTmxMapLoader");

        teaBuildConfiguration.targetType = TeaVMTargetType.JAVASCRIPT;
        // You can choose to use the WebAssembly (WASM) GC target instead, which tends to perform better, but isn't
        // as easy to debug. It might be a good idea to alternate target types during development if you plan on using
        // WASM at release time.
//        teaBuildConfiguration.targetType = TeaVMTargetType.WEBASSEMBLY_GC;

        // Used by older TeaVM versions.
//        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);

        // The next two lines are used by gdx-teavm 1.3.1 and newer (and libGDX 1.14.0 and newer).
        TeaBuilder.config(teaBuildConfiguration);
        TeaVMTool tool = new TeaVMTool();

        tool.setMainClass(TeaVMLauncher.class.getName());
        // For many (or most) applications, using a high optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change ADVANCED to SIMPLE .
        // Using SIMPLE makes debugging easier, also, so it is used when DEBUG is enabled.
        tool.setOptimizationLevel(DEBUG ? TeaVMOptimizationLevel.SIMPLE : TeaVMOptimizationLevel.ADVANCED);
        // The line below will make the generated code hard to read (and smaller) in releases and easier to follow
        // when DEBUG is true. Setting DEBUG to false should always be done before a release, anyway.
        tool.setObfuscated(!DEBUG);

        // If DEBUG is set to true, these lines allow step-debugging JVM languages from the browser,
        // setting breakpoints in Java code and stopping in the appropriate place in generated browser code.
        // This may work reasonably well when targeting WEBASSEMBLY_GC, but it usually works better with JAVASCRIPT .
        if (DEBUG) {
            tool.setDebugInformationGenerated(true);
            tool.setSourceMapsFileGenerated(true);
            tool.setWasmDebugInfoLevel(WasmDebugInfoLevel.FULL);
            tool.setSourceFilePolicy(TeaVMSourceFilePolicy.COPY);
            tool.addSourceFileProvider(new DirectorySourceFileProvider(new File("../core/src/main/java/")));
        }

        TeaBuilder.build(tool);
//
//        // Выделяем под Direct-буферы (где лежат наши массивы текстур) фиксированные 256 МБ
//        tool.setMinDirectBuffersSize(64 * 1024 * 1024);   // 64 МБ минимум
//        tool.setMaxDirectBuffersSize(256 * 1024 * 1024); // 256 МБ максимум (ВМЕСТО Integer.MAX_VALUE!)
//
//        // 2. На всякий случай увеличиваем кучу самой Java внутри WebAssembly (до 512 МБ)
//        tool.setMaxHeapSize(1024 * 1024 * 1024);
//        tool.setMinHeapSize(512 * 1024 * 1024);
//
//        // 1. ЗАПУСКАЕМ СБОРКУ ИГРЫ (генерацию app.wasm, скриптов и дефолтного index.html)
//        TeaBuilder.build(tool);
//
//        // 2. АВТОМАТИЗАЦИЯ ПОДКЛЮЧЕНИЯ НАТИВОВ BASIS UNIVERSAL И ШАБЛОНА HTML
//        System.out.println("TeaVMBuilder: Запуск процесса подмены шаблона и копирования ресурсов...");
//        File distDir = new File("build/dist/webapp");
//
//        // Описываем пути к исходным файлам библиотеки и нашему шаблону
//        File sourceJs = new File("libs/web/gdx-basis-universal.js");
//        File sourceWasm = new File("libs/web/gdx-basis-universal.wasm");
//        File sourceTemplateHtml = new File("libs/web/index.html"); // Путь к вашему созданному шаблону из Шага 1
//
//        // Проверяем физическое наличие всех файлов
//        if (!sourceJs.exists() || !sourceWasm.exists() || !sourceTemplateHtml.exists()) {
//            System.err.println("TeaVMBuilder ERROR: Не найдены необходимые файлы в папке libs/web/!" + sourceJs.getCanonicalPath());
//            System.err.println("Убедитесь, что gdx-basis-universal.js, gdx-basis-universal.wasm и index.html лежат там." + sourceTemplateHtml.getCanonicalPath());
//            return;
//        }
//
//        // Копируем нативные файлы транскодера в директорию сборки
//        Files.copy(sourceJs.toPath(), new File(distDir, "gdx-basis-universal.js").toPath(), StandardCopyOption.REPLACE_EXISTING);
//        Files.copy(sourceWasm.toPath(), new File(distDir, "gdx-basis-universal.wasm").toPath(), StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("TeaVMBuilder: Нативные файлы Basis (.js / .wasm) успешно скопированы.");
//
//        // Полностью перезаписываем сгенерированный index.html нашим кастомным шаблоном
//        File targetIndexHtml = new File(distDir, "index.html");
//        Files.copy(sourceTemplateHtml.toPath(), targetIndexHtml.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("TeaVMBuilder: Дефолтный index.html успешно заменен на ваш кастомный шаблон!");
    }
}
