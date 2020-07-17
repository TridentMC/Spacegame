package com.tridevmc.spacegame.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Camera {
    private final Vector3f _pos = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f _front = new Vector3f(0.0f, 0.0f, 1.0f);
    private final Vector3f _up = new Vector3f(0.0f, 1.0f, 0.0f);

    private double _yaw = 0.0;
    private double _pitch = 0.0;

    private double _mlastX = 400.0;
    private double _mlastY = 300.0;

    private Matrix4f lastView;
    private Matrix4f lastProj;

    private static final double _SENSITIVITY = 0.05;

    public static final int FOV = 90;

    public void resetMouse() {
        _mlastX = 400;
        _mlastY = 300;
    }

    public void updateCameraRotation(double x, double y) {
        double xOffset = x - _mlastX;
        double yOffset = y - _mlastY;
        _mlastX = x;
        _mlastY = y;

        xOffset *= _SENSITIVITY;
        yOffset *= _SENSITIVITY;

        _yaw += xOffset;
        _pitch -= yOffset;

        if (_pitch > 89.0)
            _pitch = 89.0;
        if (_pitch < -89.0)
            _pitch = -89.0;

        Vector3f f = new Vector3f();

        f.x = (float)(Math.cos(Math.toRadians(_pitch)) * Math.cos(Math.toRadians(_yaw)));
        f.y = (float)(Math.sin(Math.toRadians(_pitch)));
        f.z = (float)(Math.cos(Math.toRadians(_pitch)) * Math.sin(Math.toRadians(_yaw)));
        _front = f.normalize();
    }

    public void updatePosition(double delta, boolean[]  keyStates) {
        float speed = (float)delta * 16.0f;
        if (keyStates[GLFW.GLFW_KEY_W])
            _pos.add(new Vector3f(_front).mul(speed));
        if (keyStates[GLFW.GLFW_KEY_S])
            _pos.sub(new Vector3f(_front).mul(speed));
        if (keyStates[GLFW.GLFW_KEY_A])
            _pos.sub(new Vector3f(_front).cross(_up).normalize().mul(speed));
        if (keyStates[GLFW.GLFW_KEY_D])
            _pos.add(new Vector3f(_front).cross(_up).normalize().mul(speed));
    }

    public ViewProjection generateViewProjection(float renderDistance) {
        lastView = new Matrix4f()
                .lookAt(_pos,
                        new Vector3f(_pos.x + _front.x, _pos.y + _front.y, _pos.z + _front.z),
                        _up);
        lastProj = new Matrix4f()
                .perspective((float)Math.toRadians(FOV),800.0f/600.0f,1.0f,renderDistance);
        return new ViewProjection(lastView, lastProj);
    }

    public Vector3f getPos() {
        return _pos;
    }

    public Vector3f getFront() {
        return _front;
    }

    public Vector3f getUp() {
        return _up;
    }

    public double getPitch() {
        return _pitch;
    }

    public double getYaw() {
        return _yaw;
    }
}
