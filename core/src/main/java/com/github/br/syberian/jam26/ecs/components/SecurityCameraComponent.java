package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import box2dLight.ConeLight;

public class SecurityCameraComponent extends Component {
    // --- НАСТРОЙКИ ---
    public float cameraRange = 8f;
    public float fovDegrees = 45f;
    public float baseDirectionDegrees = 270f;
    public float patrolSpeed = 1.5f;
    public float patrolArc = 30f;

    // --- ДИНАМИЧЕСКИЙ ОФФСЕТ (Заполняется в фабрике) ---
    // Смещение точки испускания луча в локальных МЕТРАХ Box2D относительно центра камеры
    public final Vector2 localLightOffset = new Vector2(0f, 0f);

    // --- ССЫЛКА НА ОБЪЕКТ СВЕТА ---
    public ConeLight cameraLight;

    // --- СОСТОЯНИЕ И БУФЕРЫ ---
    public float cameraRotationTime = 0f;
    public float viewDirectionDegrees = 270f;
    public boolean isPlayerSpotted = false;
    public final Vector2 worldLightPosMetres = new Vector2();
    public final Vector2 toPlayer = new Vector2();
    public final Vector2 rayTarget = new Vector2();
    public float closestFraction;
    public Body sightedBody;
}
