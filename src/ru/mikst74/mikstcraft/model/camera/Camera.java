package ru.mikst74.mikstcraft.model.camera;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.main.CommunicationManager;
import ru.mikst74.mikstcraft.model.entity.BaseEntity;
import ru.mikst74.mikstcraft.settings.GameProperties;

import static java.lang.Math.toRadians;

@Getter
public class Camera extends BaseEntity {
    public static final  Matrix4f MATRIX_4_F = new Matrix4f();
    public static final  float    MAX_PITCH  = (float) Math.toRadians(89.5f);
    private static final float    MIN_PITCH  = (float) Math.toRadians(-89.5f);

    // Output field
    private final Matrix4f mvp = new Matrix4f();

    // Current state fields (Input)
    private float fov;
    private float aspect;
    private float far;
    private float near;

    /**
     * Сдвиг "точки глаз" относительно position
     */
    @Setter
    protected Vector3f offset             = new Vector3f();
    protected Vector3f positionWithOffset = new Vector3f();

    // Temp fields for calculations
//    private final Quaternionf tmpq = new Quaternionf();
    private final Matrix4f   pMat = new Matrix4f();
    private final Matrix4x3f vMat = new Matrix4x3f();


    public Camera(CommunicationManager communicationManager, float aspect) {
        super(communicationManager);
        this.fov    = GameProperties.FOV_DEGREES;
        this.far    = GameProperties.FAR;
        this.near   = GameProperties.NEAR;
        this.aspect = aspect;
        afterMove();
        afterRotate();
    }


    private void updateCameraMatrices() {
//        MATRIX_4_F
//                .rotateX((float) Math.toRadians(yaw))
//                .rotateY((float) Math.toRadians(pitch))
//                .translate(-pos.x, -pos.y, -pos.z, mvp);

        /**
         * A quaternion (\(w,x,y,z\)) is a 4D vector that represents a single rotation around a specific axis by a specific angle.
         * To fully orient an object in 3D space, you generally need to account for rotation around all three axes:
         *      X-axis (Pitch): Tilting up/down.
         *      Y-axis (Yaw): Turning left/right.
         *      Z-axis (Roll): Tilting side-to-side.
         * Without the \(y\)-angle, an object cannot turn horizontally (turn left or right), which is essential for most 3D applications.
         */

        vMat.rotation(tmpq); // rotate and store matrix vMat

        // это для камеры  снаружи
        vMat.translate((float) -(positionWithOffset.x),
                (float) -(positionWithOffset.y),
                (float) -(positionWithOffset.z));


//        vMat.translate((float) -(position.x + offset.x),
//                (float) -(position.y+ offset.y),
//                (float) -(position.z+ offset.z));
//        vMat.translate((float) -(position.x - floor(position.x) + offset.x),
//                (float) -(position.y - floor(position.y)+ offset.y),
//                (float) -(position.z - floor(position.z)+ offset.z));
        pMat.setPerspective((float) toRadians(fov), aspect, near, far, false);
        pMat.mulPerspectiveAffine(vMat, mvp);
    }

    @Override
    protected void afterRotate() {
        // Constrain the pitch so you can't flip the camera upside down
        if (angle.y > MAX_PITCH) {
            angle.y = MAX_PITCH;
        }
        if (angle.y < MIN_PITCH) {
            angle.y = MIN_PITCH;
        }

        super.afterRotate();
        updateCameraMatrices();
    }

    @Override
    public void afterMove() {
        super.afterMove();
        // это для режима Камера снаружи, вижу игрока
//        getDirection().mul(offset.x, positionWithOffset);


        // это для режима Камера из глаз, вижу игрока
        positionWithOffset.set(offset);
//        position.add(offset, positionWithOffset);

    }

    public void onChangeHandler() {
        updateCameraMatrices();
    }
}
