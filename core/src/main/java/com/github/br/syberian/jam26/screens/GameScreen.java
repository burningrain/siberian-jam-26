package com.github.br.syberian.jam26.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.github.br.libgdx.common.structure.screen.AbstractGameScreen;

import box2dLight.ConeLight;
import box2dLight.RayHandler;

public class GameScreen extends AbstractGameScreen {

    // В физических мирах Box2D 1 единица = 1 метр. Экран делаем 16x9 метров.
    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;

    private OrthographicCamera camera;
    private World world;
    private Box2DDebugRenderer debugRenderer;

    // Поля Box2D-Lights
    private RayHandler rayHandler;
    private ConeLight cameraLight;

    // Ссылки на физические тела
    private Body playerBody;
    private Body cameraBody;

    // Переменные для логики камеры наблюдения
    private float cameraRotationTime = 0;
    private boolean isPlayerSpotted = false;
    private final Vector2 cameraPos = new Vector2(2f, 7f); // Позиция камеры в левом верхнем углу

    @Override
    public void show() {
        camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        // 1. Создаем физический мир без гравитации (вид сверху)
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        // 2. Инициализируем менеджер света
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.1f, 0.1f, 0.1f, 0.7f); // Темнота вокруг

        // 3. Создаем Игрока (Динамическое тело)
        BodyDef playerDef = new BodyDef();
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.position.set(8f, 2f); // Старт внизу по центру
        playerBody = world.createBody(playerDef);

        PolygonShape playerShape = new PolygonShape();
        playerShape.setAsBox(0.4f, 0.4f); // Квадрат 0.8x0.8 метра

        FixtureDef playerFixture = new FixtureDef();
        playerFixture.shape = playerShape;
        playerBody.createFixture(playerFixture);
        playerShape.dispose();

        // 4. Создаем Шкаф/Коробку (Статическое тело, преграждающее свет)
        BodyDef boxDef = new BodyDef();
        boxDef.type = BodyDef.BodyType.StaticBody;
        boxDef.position.set(7f, 5f); // Ровно посредине между камерой и игроком
        Body boxBody = world.createBody(boxDef);

        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(0.8f, 0.4f); // Продолговатый шкаф
        boxBody.createFixture(boxShape, 0f);
        boxShape.dispose();

        // 5. Создаем "тело" камеры для удобства (Статическое)
        BodyDef camBodyDef = new BodyDef();
        camBodyDef.type = BodyDef.BodyType.StaticBody;
        camBodyDef.position.set(cameraPos);
        cameraBody = world.createBody(camBodyDef);

        // 6. Создаем Конус Света от камеры
        // Параметры: rayHandler, количество лучей (качество), цвет, дистанция луча, x, y, направление луча, угол конуса
        cameraLight = new ConeLight(rayHandler, 120, Color.YELLOW, 8f, cameraPos.x, cameraPos.y, -45f, 35f);
        cameraLight.setSoft(true); // Мягкие края тени
    }

    @Override
    public void render(float delta) {
        // Очистка экрана черным цветом
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- ЛОГИКА УПРАВЛЕНИЯ ИГРОКОМ ---
        float speed = 4f;
        Vector2 velocity = new Vector2(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) velocity.y = speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) velocity.y = -speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) velocity.x = -speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocity.x = speed;
        playerBody.setLinearVelocity(velocity);

        // --- АВТОМАТИЧЕСКИЙ ПОВОРОТ КАМЕРЫ (Регулировка угла) ---
        cameraRotationTime += delta;
        // Камера качается влево-вправо от -70 до -20 градусов
        float currentDirection = -45f + MathUtils.sin(cameraRotationTime * 1.5f) * 25f;
        cameraLight.setDirection(currentDirection); //

        // Физический шаг мира
        world.step(1 / 60f, 6, 2);

        // --- ПРОВЕРКА ВИДИМОСТИ (STEALTH LOGIC) ---
        checkCameraVision();

        // Меняем цвет конуса в зависимости от обнаружения
        if (isPlayerSpotted) {
            cameraLight.setColor(Color.RED);
        } else {
            cameraLight.setColor(Color.YELLOW);
        }

        // --- ОТРИСОВКА ---
        // Рисуем физические коробки (в реальной игре тут будет SpriteBatch и спрайты)
        debugRenderer.render(world, camera.combined);

        // Обновляем матрицы света и рендерим конус поверх геометрии мира
        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();
    }

    private float closestFraction;
    private Body sightedBody;

    private void checkCameraVision() {
        isPlayerSpotted = false;
        Vector2 playerPos = playerBody.getPosition();

        // --- НАДЕЖНАЯ ПРОВЕРКА ПОПАДАНИЯ В КОНУС СВЕТА ---

        // 1. Считаем расстояние от камеры до игрока
        float distanceToPlayer = cameraPos.dst(playerPos);

        // Если игрок в пределах длины луча камеры
        if (distanceToPlayer <= cameraLight.getDistance()) {

            // 2. Считаем угол от камеры к игроку (в градусах)
            Vector2 toPlayer = new Vector2(playerPos).sub(cameraPos);
            float angleToPlayer = toPlayer.angleDeg(); // Угол от 0 до 360

            // Получаем текущее направление камеры (приводим к диапазону 0-360)
            float cameraAngle = (cameraLight.getDirection() % 360 + 360) % 360;

            // Считаем минимальную разницу между углами
            float angleDiff = Math.abs(angleToPlayer - cameraAngle);
            if (angleDiff > 180) {
                angleDiff = 360 - angleDiff;
            }

            // Получаем половину угла раскрытия конуса (например, если конус 35 градусов, то в стороны по 17.5)
            float halfCone = cameraLight.getConeDegree();

            // Если угол входит в конус, запускаем RayCast для проверки стен
            if (angleDiff <= halfCone) {

                closestFraction = 1f;
                sightedBody = null;

                world.rayCast(new RayCastCallback() {
                    @Override
                    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                        if (fraction < closestFraction) {
                            closestFraction = fraction;
                            sightedBody = fixture.getBody();
                        }
                        return 1;
                    }
                }, cameraPos, playerPos);

                if (sightedBody == playerBody) {
                    isPlayerSpotted = true;
                    System.out.println("КАМЕРА ОБНАРУЖИЛА ИГРОКА!");
                }
            }
        }
    }


    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        rayHandler.dispose(); // Не забываем чистить за собой ресурсы света!
    }

}
